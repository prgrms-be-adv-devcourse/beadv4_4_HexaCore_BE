package com.back.product.app.usecase.command;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.back.product.adapter.out.document.ProductDocumentRepository;
import com.back.product.app.usecase.query.ProductDocumentSupport;
import com.back.product.document.ProductDocument;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.dto.model.ProductSearchDto;
import com.back.product.mapper.ProductDocumentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
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
    private final ProductDocumentRepository productDocumentRepository;

    @Transactional
    public void syncProduct(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        ProductDocument documentToSync = productDocumentMapper.toDocument(productInfoDto, optionDtos, thumbnailUrl);

        documentToSync.assignId(productInfoDto.productInfoId().toString());

        productDocumentRepository.save(documentToSync);
    }

    @Transactional
    public void deleteProduct(Long productInfoId) {
        productDocumentRepository.deleteById(productInfoId.toString());
    }

    @Transactional(readOnly = true)
    public ProductSearchResponseDto findProductPage(@Valid ProductSearchRequestDto request, Long page, Long size) {
        Query query = buildSearchQuery(
                request.keyword(),
                request.brandIds(),
                request.categoryIds(),
                request.minPrice(),
                request.maxPrice()
        );

        PageImpl<ProductDocument> productPage = productDocumentSupport.findProductPage(query, request.sort(), page, size);

        return convertToDto(
                productPage.getContent(),
                (long) productPage.getTotalPages(),
                productPage.getTotalElements(),
                (long) page.intValue()
        );
    }

    private Query buildSearchQuery(
            String keyword,
            List<Long> brands,
            List<Long> categories,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        var boolQuery = new BoolQuery.Builder();

        // 1. 키워드 검색 (should: 점수 기반 검색)
        if (StringUtils.hasText(keyword)) {
            // (1) 일반 및 MultiField 검색 경로
            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.productName").query(keyword)));
            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.productName.nori").query(keyword)));
            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.productName.ngram").query(keyword)));

            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.brand.brandName").query(keyword)));
            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.brand.brandName.nori").query(keyword)));

            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.category.categoryName").query(keyword)));
            boolQuery.should(m -> m.match(mt -> mt.field("productInfo.category.categoryName.nori").query(keyword)));

            // (2) Nested 필드 검색 (totalOptions)
            // totalOptions 내의 groupName과 valueName 검색
            boolQuery.should(s -> s.nested(n -> n
                    .path("totalOptions")
                    .query(q -> q.bool(b -> b
                            .should(m -> m.match(mt -> mt.field("totalOptions.group.groupName").query(keyword)))
                            .should(m -> m.match(mt -> mt.field("totalOptions.group.groupName.nori").query(keyword)))
                            .should(m -> m.match(mt -> mt.field("totalOptions.value.valueName").query(keyword)))
                            .should(m -> m.match(mt -> mt.field("totalOptions.value.valueName.nori").query(keyword)))
                    ))
            ));

            boolQuery.minimumShouldMatch("1");
        }

        // 2. 필터링 조건 (filter: 정확한 매칭, 캐싱 가능)

        // 브랜드 ID 필터
        if (brands != null && !brands.isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("productInfo.brand.brandId")
                    .terms(v -> v.value(brands.stream().map(FieldValue::of).toList()))
            ));
        }

        // 카테고리 ID 필터
        if (categories != null && !categories.isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("productInfo.category.categoryId")
                    .terms(v -> v.value(categories.stream().map(FieldValue::of).toList()))
            ));
        }

        // 3. 가격 범위 필터 (컴파일 에러 해결 지점)
        if (minPrice != null) {
            boolQuery.filter(f -> f.range(r -> r
                    .number(n -> n.field("productInfo.releasePrice").gte(minPrice.doubleValue()))
            ));
        }
        if (maxPrice != null) {
            boolQuery.filter(f -> f.range(r -> r
                    .number(n -> n.field("productInfo.releasePrice").lte(maxPrice.doubleValue()))
            ));
        }

        return NativeQuery.builder()
                .withQuery(boolQuery.build()._toQuery())
                .build();
    }

    private ProductSearchResponseDto convertToDto(List<ProductDocument> productList, Long totalPages, Long totalElements, Long currentPage) {
        List<ProductSearchDto> products = productList.stream().map(productDocumentMapper::toDto).toList();

        return ProductSearchResponseDto.builder()
                .products(products)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }
}
