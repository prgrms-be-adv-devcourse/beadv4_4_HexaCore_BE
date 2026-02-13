package com.back.user.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;

import org.springframework.util.backoff.BackOff;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private static final int MAX_RETRIES = 5;

    /**
     * Producer: JacksonJsonSerializer로 Envelope 자체 발행(JSON bytes)
     * Consumer: StringDeserializer로 JSON 문자열 수신 -> Listener에서 ObjectMapper로 Envelope<T> 매핑
     *
     * 컨슘 재시도 초과 시:
     * - DLT 발행 X
     * - DB에 실패 로그 저장(Recoverer)
     * - 해당 레코드는 스킵(오프셋 진행)되도록 처리
     */

    // =========================
    // String ConsumerFactory / ContainerFactory
    // =========================

    @Bean
    public ConsumerFactory<String, String> stringValueConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean(name = "stringKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> stringKafkaListenerContainerFactory(
            ConsumerFactory<String, String> stringValueConsumerFactory,
            DefaultErrorHandler commonErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(stringValueConsumerFactory);

        // 수동 커밋(Listener에서 ack.acknowledge())
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);

        // 공통 에러 핸들러: 재시도 + 재시도 초과 시 DB 로깅 후 스킵
        factory.setCommonErrorHandler(commonErrorHandler);

        return factory;
    }

    // =========================
    // ErrorHandler: retry 후 DB 로깅 (DLT 발행 X)
    // =========================

    @Bean
    public DefaultErrorHandler commonErrorHandler(
            KafkaEventAuditLogService auditLogService,
            JsonMapper jsonMapper
    ) {
        BackOff backOff = exponentialBackOff();

        DefaultErrorHandler handler = new DefaultErrorHandler((ConsumerRecord<?, ?> record, Exception ex) -> {
            String payload = safeValueAsString(record.value());

            // 1) payload에서 header(eventId/eventType) 파싱 시도 (실패해도 무시)
            UUID eventId = null;
            String eventType = null;
            try {
                if (payload != null && !payload.isBlank()) {
                    JsonNode root = jsonMapper.readTree(payload);
                    JsonNode header = root.path("header");
                    if (!header.isMissingNode()) {
                        String id = header.path("eventId").asText(null);
                        String type = header.path("eventType").asText(null);

                        if (id != null && !id.isBlank()) {
                            eventId = UUID.fromString(id);
                        }
                        if (type != null && !type.isBlank()) {
                            eventType = type;
                        }
                    }
                }
            } catch (Exception ignore) {
                // 파싱 실패해도 로그 저장은 계속 진행
            }

            // 2) stacktrace 문자열
            String stack = stackTraceToString(ex);

            // 3) 폭발 방지 트렁케이트
            Throwable rc = rootCause(ex);
            String exceptionClass = rc.getClass().getName();
            String safePayload = truncate(payload, 50_000);        // 50KB
            String safeStack = truncate(stack, 50_000);            // 50KB
            String safeErrMsg = truncate(safeMsg(rc), 2_000);      // 컬럼(2000) 맞춤

            // 4) DB 저장 (여기서 실패해도 컨슈머 멈추면 안 됨)
            try {
                auditLogService.saveLog(new KafkaConsumeFailLogCommand(
                        eventId,
                        eventType,

                        groupId,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.timestamp(),

                        safePayload,
                        exceptionClass,
                        safeErrMsg,
                        safeStack,

                        LocalDateTime.now()
                ));
            } catch (Exception ignore) {
                // best-effort: 로그 저장 실패로 컨슈머를 망치지 않음
            }
        }, backOff);

        /**
         * 의미 없는 예외는 재시도 없이 바로 recoverer로 보냄(=DB 저장 후 스킵)
         * - JsonProcessingException: Envelope 역직렬화 실패(독성 메시지) -> 재시도해도 대부분 안 됨
         * - IllegalArgumentException: 검증 실패류
         */
        handler.addNotRetryableExceptions(
                JsonProcessingException.class,
                IllegalArgumentException.class,
                DeserializationException.class
        );

        handler.setCommitRecovered(true);
        handler.setAckAfterHandle(false);

        return handler;
    }

    private BackOff exponentialBackOff() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(MAX_RETRIES);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);
        return backOff;
    }

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);

        return props;
    }

    // ===== util =====

    private String safeMsg(Throwable t) {
        String msg = t.getMessage();
        return (msg == null) ? "" : msg;
    }

    private String safeValueAsString(Object value) {
        if (value == null) return null;
        if (value instanceof String s) return s;
        if (value instanceof byte[] bytes) return new String(bytes, StandardCharsets.UTF_8);
        return String.valueOf(value);
    }

    private static String stackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

    private Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}


