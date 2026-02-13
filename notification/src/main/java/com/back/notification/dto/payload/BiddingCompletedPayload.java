package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BiddingCompletedPayload(
        @NotNull(message = "입찰 ID는 필수입니다.")
        Long biddingId,

        @NotNull(message = "구매자 ID는 필수입니다.")
        Long buyerUserId,

        @NotNull(message = "판매자 ID는 필수입니다.")
        Long sellerUserId,

        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotBlank(message = "상품 사이즈는 필수입니다.")
        String productSize,

        @NotBlank(message = "썸네일 이미지는 필수입니다.")
        String thumbnailImage,

        @NotBlank(message = "브랜드명은 필수입니다.")
        String brandName,

        @NotNull(message = "가격은 필수입니다.")
        BigDecimal price,

        @NotBlank(message = "입찰 포지션은 필수입니다.")
        String biddingPosition
) implements KafkaPayload {
}
