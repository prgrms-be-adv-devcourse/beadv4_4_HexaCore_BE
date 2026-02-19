package com.back.chat.adapter.in.kafka;

import com.back.chat.adapter.out.audit.AuditLogService;
import com.back.chat.adapter.out.audit.KafkaConsumeFailLogCommand;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(commonErrorHandler);

        return factory;
    }

    @Bean
    public DefaultErrorHandler commonErrorHandler(
            AuditLogService auditLogService,
            JsonMapper jsonMapper
    ) {
        BackOff backOff = exponentialBackOff();

        DefaultErrorHandler handler = new DefaultErrorHandler((ConsumerRecord<?, ?> record, Exception ex) -> {

            String payload = safeValueAsString(record.value());

            UUID eventId = null;
            String eventType = null;

            // header best-effort 파싱
            try {
                if (payload != null && !payload.isBlank()) {
                    JsonNode root = jsonMapper.readTree(payload);
                    JsonNode header = root.path("header");
                    if (!header.isMissingNode()) {
                        eventId = safeUuid(header.path("eventId").asText(null));
                        String type = header.path("eventType").asText(null);
                        if (type != null && !type.isBlank()) eventType = type;
                    }
                }
            } catch (Exception ignore) { }

            Throwable rc = rootCause(ex);
            String errorClass = (rc == null) ? ex.getClass().getName() : rc.getClass().getName();
            String errorMessage = truncate(safeMsg(rc == null ? ex : rc), 2_000);

            String stacktrace = truncate(stackTraceToString(ex), 50_000);
            String payloadJson = truncate(payload, 50_000);

            try {
                KafkaConsumeFailLogCommand cmd = KafkaConsumeFailLogCommand.of(
                        eventId,
                        eventType,
                        groupId,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        record.timestamp(),
                        ex,
                        payloadJson,
                        stacktrace,
                        LocalDateTime.now()
                );

                auditLogService.saveLog(cmd);
            } catch (Exception ignore) { }
        }, backOff);

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

    private static UUID safeUuid(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isBlank()) return null;
        try { return UUID.fromString(v); }
        catch (IllegalArgumentException e) { return null; }
    }

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String msg = t.getMessage();
        return (msg == null) ? "" : msg;
    }

    private static String safeValueAsString(Object value) {
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

    private static String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

    private static Throwable rootCause(Throwable t) {
        if (t == null) return null;
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}

