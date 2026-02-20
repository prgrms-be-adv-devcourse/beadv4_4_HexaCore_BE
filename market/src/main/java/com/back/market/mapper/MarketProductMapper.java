package com.back.market.mapper;

import com.back.market.domain.MarketProduct;
import com.back.market.event.payload.OptionPayload;
import com.back.market.event.payload.ProductCreatedPayload;
import com.back.market.event.resultpayload.ProductCreatedResultPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MarketProductMapper {

    // 1단계: 원본(Payload)을 중간재(ResultPayload)로 변환
    public ProductCreatedResultPayload toResultPayload(ProductCreatedPayload payload, OptionPayload.ValuePayload optionValue) {
        return ProductCreatedResultPayload.builder()
                .productOptionId(optionValue.valueId())
                .productInfoId(payload.productInfo().productInfoId())
                .name(payload.productInfo().name())
                .productOption(optionValue.valueName())
                .productNumber(payload.productInfo().code())
                .thumbnailImage(payload.thumbnailUrl())
                .releasePrice(payload.productInfo().releasePrice())
                .brandName(payload.productInfo().brand().name())
                .categoryName(payload.productInfo().category().name())
                .build();
    }

    // 2단계: 중간재(ResultPayload)를 최종(Entity)으로 변환
    public MarketProduct toEntity(ProductCreatedResultPayload payload) {
        return MarketProduct.builder()
                .productOptionId(payload.productOptionId())
                .productInfoId(payload.productInfoId())
                .name(payload.name())
                .productOption(payload.productOption())
                .productNumber(payload.productNumber())
                .thumbnailImage(payload.thumbnailImage())
                .releasePrice(payload.releasePrice())
                .brandName(payload.brandName())
                .categoryName(payload.categoryName())
                .build();
    }

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
}
