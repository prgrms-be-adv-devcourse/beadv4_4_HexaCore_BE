package com.back.market.mapper;

import com.back.market.domain.MarketProduct;
import com.back.market.event.payload.OptionPayload;
import com.back.market.event.payload.ProductCreatedPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MarketProductMapper {
    public MarketProduct toEntity(
            Long productOptionId,
            Long productInfoId,
            String brandName,
            String name,
            String productNumber,
            String productOption,
            Long price,
            String categoryName,
            String thumbnailImage
    ) {
        return MarketProduct.builder()
                .productOptionId(productOptionId)
                .productInfoId(productInfoId)
                .brandName(brandName)
                .name(name)
                .productNumber(productNumber)
                .productOption(productOption) // 사이즈 등 옵션
                .releasePrice(BigDecimal.valueOf(price)) // 편의상 long으로 받아 여기에서 BigDecimal 변환
                .categoryName(categoryName)
                .thumbnailImage(thumbnailImage)
                .build();
    }

    /**
     * Kafka 이벤트를 통해 들어온 Payload를 엔티티로 변환하는 메서드
     * @param payload ProductCreatedPayload
     * @param optionValue OptionPayload.ValuePayload
     * @return MarketProduct 엔티티
     */
    public MarketProduct fromProductCreatedPayload(
            ProductCreatedPayload payload,
            OptionPayload.ValuePayload optionValue
    ) {
        return MarketProduct.builder()
                .productOptionId(optionValue.valueId())
                .productInfoId(payload.productInfo().productInfoId())
                .name(payload.productInfo().name())
                .productOption(optionValue.valueName()) // 예: "230", "240"
                .productNumber(payload.productInfo().code())
                .thumbnailImage(payload.thumbnailUrl())
                .releasePrice(payload.productInfo().releasePrice())
                .brandName(payload.productInfo().brand().name())
                .categoryName(payload.productInfo().category().name())
                .build();
    }
}
