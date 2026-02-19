package com.back.cash.config;

import com.back.cash.adapter.in.listener.exception.PayoutMessageParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import org.apache.kafka.common.TopicPartition;

import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${custom.kafka.topic.settlement-payout-requested-dlt}")
    private String dltTopicName;

    @Bean
    public DefaultErrorHandler cashPayoutErrorHandler() {
        // DLT 전용 KafkaTemplate (StringSerializer → 원본 메시지 그대로 보존)
        KafkaTemplate<String, String> dltKafkaTemplate = new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
                ))
        );

        // yml에 지정한 DLT 토픽명으로 발행
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(dltKafkaTemplate,
                        (record, ex) -> new TopicPartition(dltTopicName, record.partition())) {
                    @Override
                    public void accept(ConsumerRecord<?, ?> record,
                                       org.apache.kafka.clients.consumer.Consumer<?, ?> consumer,
                                       Exception exception) {
                        log.error("[DLT] 재시도 소진, DLT로 전송. topic={}, offset={}, value={}",
                                record.topic(), record.offset(), record.value(), exception);
                        super.accept(record, consumer, exception);
                    }
                };

        // 재시도: 1초 → 2초 → 4초 (최대 3번)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // 재시도 로깅
        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("[RETRY] 재시도 {}/3. topic={}, offset={}, cause={}",
                        deliveryAttempt, record.topic(), record.offset(), ex.getMessage())
        );

        // 재시도해도 의미 없는 예외 → 바로 DLT로
        handler.addNotRetryableExceptions(PayoutMessageParseException.class);

        return handler;
    }

    @Bean(name = "cashPayoutKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
    cashPayoutKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler cashPayoutErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(cashPayoutErrorHandler);
        return factory;
    }
}
