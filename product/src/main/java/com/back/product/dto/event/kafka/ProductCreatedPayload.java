package com.back.product.dto.event.kafka;

import com.back.common.event.KafkaPayload;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductCreatedPayload(
        ProductInfoPayload productInfo,
        List<OptionPayload> options,
        String thumbnailUrl
) implements KafkaPayload {
}
