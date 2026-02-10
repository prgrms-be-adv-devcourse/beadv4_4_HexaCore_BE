package com.back.product.dto.event.kafka;

import com.back.common.event.KafkaPayload;
import lombok.Builder;

@Builder
public record ProductDeletedPayload(
        Long productInfoId
) implements KafkaPayload {
}
