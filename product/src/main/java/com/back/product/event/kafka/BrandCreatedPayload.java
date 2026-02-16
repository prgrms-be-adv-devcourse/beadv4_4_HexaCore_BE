package com.back.product.event.kafka;

import com.back.common.event.KafkaPayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BrandCreatedPayload(
        @NotNull(message = "브랜드 페이로드는 필수입니다.")
        @Valid
        List<BrandPayload> brandPayloadList
) implements KafkaPayload {
}
