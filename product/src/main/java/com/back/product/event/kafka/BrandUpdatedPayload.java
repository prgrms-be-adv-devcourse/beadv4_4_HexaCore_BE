package com.back.product.event.kafka;

import com.back.common.event.KafkaPayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record BrandUpdatedPayload(
        @NotEmpty(message = "브랜드 페이로드는 필수입니다.")
        @Valid
        BrandPayload brandPayload
) implements KafkaPayload {
}
