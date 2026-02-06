package com.back.settlement.app.event.handler;

import com.back.common.event.EventName;
import com.back.common.event.KafkaEventPublisher;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@TestConfiguration
public class KafkaTestConsumerConfig {

    @Bean
    KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, EventName> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    @Bean
    SettlementCashPayoutEventHandler handler(KafkaEventPublisher publisher) {
        return new SettlementCashPayoutEventHandler(publisher, "settlement-payout-request");
    }

    @Bean
    ProducerFactory<String, EventName> producerFactory(EmbeddedKafkaBroker broker) {
        Map<String, Object> props = new HashMap<>(KafkaTestUtils.producerProps(broker));
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    KafkaTemplate<String, EventName> kafkaTemplate(ProducerFactory<String, EventName> pf) {
        return new KafkaTemplate<>(pf);
    }
}
