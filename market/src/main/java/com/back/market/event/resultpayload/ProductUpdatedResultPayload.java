package com.back.market.event.resultpayload;

import com.back.common.event.EventName;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductUpdatedResultPayload(
        Long productOptionId,    // 원본 옵션 ID (비즈니스 키)
        Long productInfoId,      // 원본 상품 정보 ID
        String name,             // 상품명
        String productOption,    // 사이즈(옵션)
        String productNumber,    // 제품번호
        String thumbnailImage,   // 이미지 URL
        BigDecimal releasePrice, // 발매가
        String brandName,        // 브랜드명
        String categoryName      // 카테고리명
) implements EventName {
}
