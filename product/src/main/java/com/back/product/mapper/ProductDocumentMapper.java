package com.back.product.mapper;

import com.back.product.document.ProductDocument;
import com.back.product.dto.response.ProductSearchResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductDocumentMapper {
    public ProductSearchResponseDto toDto(ProductDocument document) {
        return ProductSearchResponseDto.builder()
                .productInfoId(document.getProductInfo().getProductInfoId())
                .productName(document.getProductInfo().getProductName())
                .thumbnailUrl(document.getThumbnailUrl())
                .brandName(document.getProductInfo().getBrand().getBrandName())
                .categoryName(document.getProductInfo().getCategory().getCategoryName())
                .releasePrice(document.getProductInfo().getReleasePrice())
                .build();
    }
}
