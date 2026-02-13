package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SellBiddingCreatedPayload(
        @NotNull(message = "판매자 ID는 필수입니다.")
        Long sellerUserId,

        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotBlank(message = "상품 번호는 필수입니다.")
        String productNumber,

        @NotBlank(message = "브랜드명은 필수입니다.")
        String brandName,

        @NotBlank(message = "카테고리명은 필수입니다.")
        String categoryName,

        @NotBlank(message = "상품 옵션은 필수입니다.")
        String productOption,

        @NotBlank(message = "썸네일 이미지는 필수입니다.")
        String thumbnailImage,

        @NotNull(message = "현재 가격은 필수입니다.")
        BigDecimal currentPrice
) implements KafkaPayload {
}
