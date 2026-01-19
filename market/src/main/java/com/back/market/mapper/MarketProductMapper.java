package com.back.market.mapper;

import com.back.market.domain.MarketProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MarketProductMapper {
    public MarketProduct toEntity(Long id, String brandName, String name, String productNumber,String productOption, Long price, String categoryName, String thumbnailImage) {
        return MarketProduct.builder()
                .id(id)
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
