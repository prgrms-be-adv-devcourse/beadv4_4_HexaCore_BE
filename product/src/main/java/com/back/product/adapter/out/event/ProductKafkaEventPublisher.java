package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.product.dto.event.kafka.ProductCreatedPayload;
import com.back.product.dto.event.kafka.ProductDeletedPayload;
import com.back.product.dto.event.kafka.ProductUpdatedPayload;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.mapper.ProductPayloadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductKafkaEventPublisher {
    private final KafkaEventPublisher kafkaEventPublisher;
    private final ProductPayloadMapper productPayloadMapper;

    @Value("${custom.kafka.topic.product-item-created}")
    private String productCreatedTopic;

    @Value("${custom.kafka.topic.product-item-updated}")
    private String productUpdatedTopic;

    @Value("${custom.kafka.topic.product-item-deleted}")
    private String productDeletedTopic;

    public void sendCreatedEvent(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        ProductCreatedPayload payload = productPayloadMapper.toCreatedPayload(productInfoDto, optionDtos, thumbnailUrl);

        Envelope<ProductCreatedPayload> event = Envelope.of(productCreatedTopic, payload);

        kafkaEventPublisher.publish(productCreatedTopic, event);

        log.info("[ProductKafkaEventPublisher] Sent ProductCreatedPayload for productInfoId: {}", productInfoDto.productInfoId());
    }

    public void sendModifiedEvent(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        ProductUpdatedPayload payload = productPayloadMapper.toUpdatedPayload(productInfoDto, optionDtos, thumbnailUrl);

        Envelope<ProductUpdatedPayload> event = Envelope.of(productUpdatedTopic, payload);

        kafkaEventPublisher.publish(productUpdatedTopic, event);

        log.info("[ProductKafkaEventPublisher] Sent ProductUpdatedPayload for productInfoId: {}", productInfoDto.productInfoId());
    }

    public void sendDeletedEvent(Long productInfoId) {
        ProductDeletedPayload payload = productPayloadMapper.toDeletedPayload(productInfoId);

        Envelope<ProductDeletedPayload> event = Envelope.of(productDeletedTopic, payload);

        kafkaEventPublisher.publish(productDeletedTopic, event);

        log.info("[ProductKafkaEventPublisher] Sent ProductDeletedPayload for productInfoId: {}", productInfoId);
    }
}
