package com.back.cash.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
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

import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${custom.kafka.topic.settlement-payout-requested-dlt}")
    private String payoutDltTopicName;

    @Value("${custom.kafka.topic.user-wallet-created-dlt}")
    private String walletDltTopicName;

    @Bean
    public KafkaTemplate<String, String> dltKafkaTemplate() {
        return new KafkaTemplate<>(
                new DefaultKafkaProducerFactory<>(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
                ))
        );
    }

    private ExponentialBackOffWithMaxRetries createBackOff() {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);
        return backOff;
    }

    private DefaultErrorHandler createErrorHandler(KafkaTemplate<String, String> dltKafkaTemplate, String dltTopicName) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(dltKafkaTemplate,
                        (record, ex) -> new TopicPartition(dltTopicName, -1)) {
                    @Override
                    public void accept(ConsumerRecord<?, ?> record,
                                       org.apache.kafka.clients.consumer.Consumer<?, ?> consumer,
                                       Exception exception) {
                        log.error("[DLT] 재시도 소진, DLT로 전송. topic={}, offset={}, value={}",
                                record.topic(), record.offset(), record.value(), exception);
                        super.accept(record, consumer, exception);
                    }
                };

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, createBackOff());
        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("[RETRY] 재시도 {}/3. topic={}, offset={}, cause={}",
                        deliveryAttempt, record.topic(), record.offset(), ex.getMessage())
        );
        return handler;
    }

    @Bean
    public DefaultErrorHandler cashPayoutErrorHandler(KafkaTemplate<String, String> dltKafkaTemplate) {
        return createErrorHandler(dltKafkaTemplate, payoutDltTopicName);
    }

    @Bean
    public DefaultErrorHandler walletErrorHandler(KafkaTemplate<String, String> dltKafkaTemplate) {
        return createErrorHandler(dltKafkaTemplate, walletDltTopicName);
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

    @Bean(name = "walletKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String>
    walletKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler walletErrorHandler
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(walletErrorHandler);
        return factory;
    }
}
