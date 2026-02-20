package com.back.product.adapter.in.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaPayload;
import com.back.product.app.facade.EventConsumptionFacade;
import com.back.product.dto.command.EventConsumptionCommand;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
import com.back.product.mapper.EventConsumptionCommandMapper;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductKafkaEventListener {
    private final EventConsumptionFacade eventConsumptionFacade;
    private final EventConsumptionCommandMapper eventConsumptionCommandMapper;
    private final JsonMapper jsonMapper;

    private static final String ATTEMPTS = "5";
    private static final long INIT_DELAY_MS = 2_000L;
    private static final long MULTIPLIER = 2L;
    private static final long MAX_DELAY_MS = 60_000L;

    @RetryableTopic(
            kafkaTemplate = "retryKafkaTemplate",
            attempts = ATTEMPTS,
            backOff = @BackOff(delay = INIT_DELAY_MS, multiplier = MULTIPLIER, maxDelay = MAX_DELAY_MS),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            dltTopicSuffix = ".dlt",
            retryTopicSuffix = ".retry"
    )
    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductCreate(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        try {
            Envelope<ProductCreatedPayload> event = jsonMapper.readValue(message, new TypeReference<>() {});

            ProductCreatedPayload payload = event.payload();

            log.info("[KafkaListenerSuccess] ProductCreatedEvent 수신 : productInfo = {}", payload.productInfo());

            EventConsumptionCommand command = eventConsumptionCommandMapper.toCommand(
                    event.header().eventId(), event.header().eventType(),
                    topic, partition, offset, message
            );

            eventConsumptionFacade.syncCreatedProduct(command, payload);
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] ProductCreatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(
            kafkaTemplate = "retryKafkaTemplate",
            attempts = ATTEMPTS,
            backOff = @BackOff(delay = INIT_DELAY_MS, multiplier = MULTIPLIER, maxDelay = MAX_DELAY_MS),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            dltTopicSuffix = ".dlt",
            retryTopicSuffix = ".retry"
    )
    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductUpdate(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        try {
            Envelope<ProductUpdatedPayload> event = jsonMapper.readValue(message, new TypeReference<>() {});

            ProductUpdatedPayload payload = event.payload();

            log.info("[KafkaListenerSuccess] ProductUpdatedEvent 수신 : productInfo = {}", payload.productInfo());

            EventConsumptionCommand command = eventConsumptionCommandMapper.toCommand(
                    event.header().eventId(), event.header().eventType(),
                    topic, partition, offset, message
            );

            eventConsumptionFacade.syncUpdatedProduct(command, payload);
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] ProductUpdatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw e;
        }
    }

    @RetryableTopic(
            kafkaTemplate = "retryKafkaTemplate",
            attempts = ATTEMPTS,
            backOff = @BackOff(delay = INIT_DELAY_MS, multiplier = MULTIPLIER, maxDelay = MAX_DELAY_MS),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            dltTopicSuffix = ".dlt",
            retryTopicSuffix = ".retry"
    )
    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductDelete(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        try {
            Envelope<ProductDeletedPayload> event = jsonMapper.readValue(message, new TypeReference<>() {});

            ProductDeletedPayload payload = event.payload();

            log.info("[KafkaListenerSuccess] ProductDeletedEvent 수신 : productInfoId = {}", payload.productInfoId());

            EventConsumptionCommand command = eventConsumptionCommandMapper.toCommand(
                    event.header().eventId(), event.header().eventType(),
                    topic, partition, offset, message
            );

            eventConsumptionFacade.syncDeletedProduct(command, payload);
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] ProductDeletedEvent 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
            throw e;
        }
    }

    @DltHandler
    public void handleDlt(
            String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            @Header(KafkaHeaders.EXCEPTION_MESSAGE) String errorMessage
    ) {
        try {
            log.error("[KafkaListenerDlt] Topic: {}, Partition: {}, Offset: {}, Error: {}, Message: {}",
                    topic, partition, offset, errorMessage, message);

            JsonNode rootNode = jsonMapper.readTree(message);
            String eventId = rootNode.path("header").path("eventId").asString();
            if (eventId == null || eventId.isBlank()) {
                throw new IllegalStateException("eventId is null or empty");
            }

            eventConsumptionFacade.eventFailedLog(eventId, errorMessage);
        } catch (JacksonException | IllegalStateException e) {
            log.error("[KafkaListenerDlt] KafkaDlt 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
        }
    }
}
