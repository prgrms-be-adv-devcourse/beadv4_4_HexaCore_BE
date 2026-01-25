package com.back.product.app.usecase;

import com.back.product.document.ProductDocument;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductSearchListResponseDto;
import com.back.product.mapper.ProductDocumentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDocumentUseCase {
    private final ProductDocumentSupport productDocumentSupport;
    private final ProductDocumentMapper productDocumentMapper;

    @Transactional(readOnly = true)
    public ProductSearchListResponseDto findProductPage(@Valid ProductSearchRequestDto request, Long page, Long size) {
        Query query = buildSearchQuery(
                request.keyword(),
                request.brandIds(),
                request.categoryIds(),
                request.minPrice(),
                request.maxPrice(),
                request.excludeSoldOut()
        );

        PageImpl<ProductDocument> productPage = productDocumentSupport.findProductPage(query, request.sort(), page, size);

        return convertToDto(
                productPage.getContent(),
                (long) productPage.getTotalPages(),
                productPage.getTotalElements(),
                (long) page.intValue()
        );
    }

    private Query buildSearchQuery(String keyword, List<Long> brands, List<Long> categories, BigDecimal minPrice, BigDecimal maxPrice, Boolean excludeSoldOut) {
        Criteria criteria = new Criteria();

        // 검색어 (전문 검색)
        if (StringUtils.hasText(keyword)) {
            criteria = criteria.and(
                    new Criteria("productName").contains(keyword)
                            .or("totalOptions").contains(keyword)
                            .or("brandName").contains(keyword)
                            .or("categoryName").contains(keyword)
            );
        }

        // 필터링 조건 (다중 선택 가능)
        if (brands != null && !brands.isEmpty()) {
            criteria = criteria.and("brandId").in(brands);
        }
        if (categories != null && !categories.isEmpty()) {
            criteria = criteria.and("categoryId").in(categories);
        }

        // 범위 필터링 (가격)
        if (minPrice != null) {
            criteria = criteria.and("releasePrice").greaterThanEqual(minPrice);
        }
        if (maxPrice != null) {
            criteria = criteria.and("releasePrice").lessThanEqual(maxPrice);
        }

        // 상태 필터링 (품절 상품 제외 여부)
        if (excludeSoldOut != null && excludeSoldOut) {
            criteria = criteria.and("totalInventory").greaterThan(0);
        }

        return new CriteriaQuery(criteria);
    }

    private ProductSearchListResponseDto convertToDto(List<ProductDocument> productList, Long totalPages, Long totalElements, Long currentPage) {
        List<ProductSearchResponseDto> products = productList.stream().map(productDocumentMapper::toDto).toList();

        return ProductSearchListResponseDto.builder()
                .products(products)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }
}
