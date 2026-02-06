package com.back.user.kafka;

import com.back.common.chat.ChatDeadLetterPayload;
import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;


import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * 토픽별 DTO 자동 역직렬화
     * - ConsumerFactory/ContainerFactory를 2개로 분리
     * - 각 factory에 VALUE_DEFAULT_TYPE을 각각 다른 DTO로 고정
     * - Listener에서 containerFactory를 지정해서 사용
     *
     *  컨슘은 DLT 발행은 하지 않음
     * - retry 초과 시 -> 컨테이너 에러 처리
     */

    // =========================
    // 1) BLINDED 이벤트 컨슈머
    // =========================

    @Bean
    public ConsumerFactory<String, ChatMessageBlindedKafkaEvent> chatMessageBlindedConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ChatMessageBlindedKafkaEvent.class.getName());

        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.back.common.chat");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(ChatMessageBlindedKafkaEvent.class, false)
        );
    }

    @Bean(name = "chatMessageBlindedKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ChatMessageBlindedKafkaEvent>
    chatMessageBlindedKafkaListenerContainerFactory(
            ConsumerFactory<String, ChatMessageBlindedKafkaEvent> chatMessageBlindedConsumerFactory,
            DefaultErrorHandler commonErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ChatMessageBlindedKafkaEvent>();
        factory.setConsumerFactory(chatMessageBlindedConsumerFactory);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(commonErrorHandler);
        return factory;
    }

    // =========================
    // 2) DeadLetterPayload 컨슈머
    // =========================

    @Bean
    public ConsumerFactory<String, ChatDeadLetterPayload> chatDeadLetterConsumerFactory() {
        Map<String, Object> props = baseConsumerProps();
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        props.put(JacksonJsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, ChatDeadLetterPayload.class.getName());
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "com.back.common.chat");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(ChatDeadLetterPayload.class, false)
        );
    }

    @Bean(name = "chatDeadLetterKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, ChatDeadLetterPayload>
    chatDeadLetterKafkaListenerContainerFactory(
            ConsumerFactory<String, ChatDeadLetterPayload> chatDeadLetterConsumerFactory,
            DefaultErrorHandler commonErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, ChatDeadLetterPayload>();
        factory.setConsumerFactory(chatDeadLetterConsumerFactory);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(commonErrorHandler);
        return factory;
    }

    // =========================
    // Common Error Handler (재시도, DLT 발행 x)
    // =========================

    @Bean
    public DefaultErrorHandler commonErrorHandler() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(5);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);

        // 재시도 초과 시 컨테이너 에러 처리에 맡김
        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);

        // 의미 없는 예외는 바로 실패 처리(재시도 X)
        handler.addNotRetryableExceptions(
                JsonProcessingException.class,
                IllegalArgumentException.class
        );

        return handler;
    }

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);

        return props;
    }
}

