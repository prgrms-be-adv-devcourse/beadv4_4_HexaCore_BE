package com.back.product.event.kafka;

import com.back.common.event.KafkaPayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Builder
public record ProductCreatedPayload(
        @NotNull(message = "상품 정보 페이로드는 필수입니다.")
        @Valid
        ProductInfoPayload productInfo,

        @NotEmpty(message = "옵션 페이로드 목록은 비어 있을 수 없습니다.")
        @Valid
        List<OptionPayload> options,

        @NotBlank(message = "썸네일 URL은 필수입니다.")
        @URL(message = "유효한 URL 형식이 아닙니다.")
        String thumbnailUrl
) implements KafkaPayload {
}
