package com.back.product.mapper;

import com.back.product.document.ProductDocument;
import com.back.product.dto.response.ProductSearchResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductDocumentMapper {
    public ProductSearchResponseDto toDto(ProductDocument document) {
        return ProductSearchResponseDto.builder()
                .productInfoId(document.getProductInfoId())
                .productName(document.getProductName())
                .thumbnailUrl(document.getThumbnailUrl())
                .brandName(document.getBrandName())
                .categoryName(document.getCategoryName())
                .releasePrice(document.getReleasePrice())
                .build();
    }
}
