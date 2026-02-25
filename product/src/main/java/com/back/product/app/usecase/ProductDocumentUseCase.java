package com.back.product.app.usecase;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.back.ai.app.usecase.EmbeddingUseCase;
import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.document.ProductDocumentRepository;
import com.back.product.adapter.out.event.ProductSpringEventPublisher;
import com.back.product.document.ProductDocument;
import com.back.product.dto.command.ProductSearchCommand;
import com.back.product.dto.enums.ProductSortType;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.dto.model.ProductSearchDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.mapper.ProductDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDocumentUseCase {
    private final ProductDocumentMapper productDocumentMapper;
    private final ProductDocumentSupport productDocumentSupport;
    private final ProductDocumentRepository productDocumentRepository;
    private final ProductSpringEventPublisher eventPublisher;

    private final EmbeddingUseCase embeddingUseCase;

    private static final Integer SIMILAR_MULTIPLIER = 10;
    private static final Integer SEARCH_MULTIPLIER = 5;

    @Loggable
    public void syncProduct(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        String description = buildProductInfo(productInfoDto, optionDtos);

        List<Float> embedding = embeddingUseCase.generateEmbeddings(description);
        if (embedding == null || embedding.isEmpty()) {
            log.error("[ProductDocumentUseCase] Generate Embedding Failed.");
            throw new CustomException(FailureCode.EMBEDDING_PROCESSING_FAILED);
        }

        ProductDocument documentToSync = productDocumentMapper.toDocument(productInfoDto, optionDtos, thumbnailUrl, embedding);

        documentToSync.assignId(productInfoDto.productInfoId().toString());

        productDocumentRepository.save(documentToSync);
    }

    @Loggable
    public void deleteProduct(Long productInfoId) {
        productDocumentRepository.deleteById(productInfoId.toString());
    }

    @Loggable
    public ProductSearchResponseDto findProductPage(ProductSearchCommand search) {
        Query query = buildSearchQuery(search);

        PageImpl<ProductDocument> productPage = productDocumentSupport.findProductPage(query);

        return convertToDto(
                productPage.getContent(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getNumber()
        );
    }

    @Loggable
    public ProductSearchResponseDto findSimilarProducts(Long productInfoId, Integer page, Integer size) {
        NativeQuery embeddingQuery = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t.field("_id").value(productInfoId.toString())))
                .withFields("embedding") // 핵심: 임베딩 필드를 콕 집어서 가져오라고 명령
                .build();

        ProductDocument document = productDocumentSupport.findProductWithEmbedding(embeddingQuery);
        if (document == null) {
            log.error("[ProductDocumentUseCase] Product Finding Failed. Product Document is Null");
            throw new CustomException(FailureCode.PRODUCT_NOT_FOUND);
        }
        List<Float> embedding = document.getEmbedding();

        if (embedding == null || embedding.isEmpty()) {
            log.warn("[ProductDocumentUseCase] Embedding not found for product: {}. Returning empty results.", productInfoId);

            Long productInfoIdToResync = Long.valueOf(Objects.requireNonNull(document.getId()));
            eventPublisher.sendResyncRequestEvent(productInfoIdToResync);

            return convertToDto(List.of(), 0, 0L, page);
        }

        // 자기 자신을 제외하는 필터 생성
        TermQuery excludeSelfFilter = new TermQuery.Builder()
                .field("_id")
                .value(productInfoId.toString())
                .build();

        KnnSearch knnSearch = buildKnnSearch(embedding, size, size * SIMILAR_MULTIPLIER, excludeSelfFilter);

        Pageable pageable = buildPageable(ProductSortType.LATEST, page, size);

        NativeQuery query = NativeQuery.builder()
                .withKnnSearches(knnSearch)
                .withPageable(pageable)
                .build();

        PageImpl<ProductDocument> productPage = productDocumentSupport.findProductPage(query);

        return convertToDto(
                productPage.getContent(),
                productPage.getTotalPages(),
                productPage.getTotalElements(),
                productPage.getNumber()
        );
    }

    private Query buildSearchQuery(ProductSearchCommand search) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 1. 키워드 검색 (should: 점수 기반 검색)
        buildKeywordSearchQuery(boolQuery, search.keyword());

        // 2. 필터링 조건 (filter: 정확한 매칭, 캐싱 가능)
        filterByBrands(boolQuery, search.brandIds());
        filterByCategories(boolQuery, search.categoryIds());
        filterByMinPrice(boolQuery, search.minPrice());
        filterByMaxPrice(boolQuery, search.maxPrice());

        // 3. 페이징 및 정렬
        Pageable pageable = buildPageable(search.sort(), search.page(), search.size());

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(boolQuery.build()._toQuery())
                .withPageable(pageable);

        // 4. 임베딩 유사도 검색 (should: 점수 기반 검색)
        if (StringUtils.hasText(search.keyword())) {
            List<Float> embedding = embeddingUseCase.generateEmbeddings(search.keyword());

            KnnSearch knnSearch = buildKnnSearch(embedding, search.size(), search.size() * SEARCH_MULTIPLIER);

            queryBuilder.withKnnSearches(knnSearch);
        }

        return queryBuilder.build();
    }

    private void buildKeywordSearchQuery(BoolQuery.Builder boolQuery, String keyword) {

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
    }

    private void filterByBrands(BoolQuery.Builder boolQuery, List<Long> brands) {
        if (brands != null && !brands.isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("productInfo.brand.brandId")
                    .terms(v -> v.value(brands.stream().map(FieldValue::of).toList()))
            ));
        }
    }

    private void filterByCategories(BoolQuery.Builder boolQuery, List<Long> categories) {
        if (categories != null && !categories.isEmpty()) {
            boolQuery.filter(f -> f.terms(t -> t
                    .field("productInfo.category.categoryId")
                    .terms(v -> v.value(categories.stream().map(FieldValue::of).toList()))
            ));
        }
    }

    private void filterByMinPrice(BoolQuery.Builder boolQuery, BigDecimal minPrice) {
        if (minPrice != null) {
            boolQuery.filter(f -> f.range(r -> r
                    .number(n -> n.field("productInfo.releasePrice").gte(minPrice.doubleValue()))
            ));
        }
    }

    private void filterByMaxPrice(BoolQuery.Builder boolQuery, BigDecimal maxPrice) {
        if (maxPrice != null) {
            boolQuery.filter(f -> f.range(r -> r
                    .number(n -> n.field("productInfo.releasePrice").lte(maxPrice.doubleValue()))
            ));
        }
    }

    private Pageable buildPageable(ProductSortType sortType, Integer page, Integer size) {
        // 정렬 조건 (기본 정렬: 최신순)
        Sort sort = Sort.by(
                ProductSortType.LATEST.getDirection(),
                ProductSortType.LATEST.getFieldName()
        );
        if (sortType != null) {
            sort = Sort.by(sortType.getDirection(), sortType.getFieldName());
        }

        // 페이징
        return PageRequest.of(page.intValue(), size.intValue(), sort);
    }

    private KnnSearch buildKnnSearch(List<Float> embedding, Integer k, Integer candidate) {
        return buildKnnSearch(embedding, k, candidate, null);
    }

    private KnnSearch buildKnnSearch(List<Float> embedding, Integer k, Integer candidate, TermQuery mustNotFilter) {
        return KnnSearch.of(knn -> {
            knn.queryVector(embedding)
                    .field("embedding")
                    .k(k) // 최종 결과 수
                    .numCandidates(candidate); // 후보 수 (k * 2 ~ k * 10 권장)

            if (mustNotFilter != null) {
                knn.filter(f -> f.bool(b -> b.mustNot(mustNotFilter)));
            }

            return knn;
        });
    }

    private ProductSearchResponseDto convertToDto(List<ProductDocument> productList, Integer totalPages, Long totalElements, Integer currentPage) {
        List<ProductSearchDto> products = productList.stream().map(productDocumentMapper::toDto).toList();

        return ProductSearchResponseDto.builder()
                .products(products)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .build();
    }

    // ex. "Brand: Nike. Category: Shoes. Product Code: NK12345. Product Name: Air Max. Release Price: 199.99. Release Date: 2023/10/15. Color: Red, Blue, Green; Size: 8, 9, 10."
    private String buildProductInfo(ProductInfoDto productInfoDto, List<OptionDto> optionDtos) {
        StringBuilder productInfo = new StringBuilder();

        productInfo.append("Brand: ").append(productInfoDto.brand().name()).append(". ");
        productInfo.append("Category: ").append(productInfoDto.category().name()).append(". ");
        productInfo.append("Product Code: ").append(productInfoDto.code()).append(". ");
        productInfo.append("Product Name: ").append(productInfoDto.name()).append(". ");
        productInfo.append("Release Price: ").append(productInfoDto.releasePrice()).append(". ");
        productInfo.append("Release Date: ").append(
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
                        .format(productInfoDto.releaseDate())
        ).append(". ");

        productInfo.append(
                optionDtos.stream()
                        .map(this::buildOptionInfo)
                        .collect(Collectors.joining("; "))
        ).append(". ");

        return productInfo.toString().trim();
    }

    // ex. "Color: Red, Blue, Green."
    private String buildOptionInfo(OptionDto optionDto) {
        StringBuilder optionInfo = new StringBuilder();

        optionInfo.append(optionDto.group().name()).append(": ");
        optionInfo.append(
                optionDto.values().stream()
                        .map(OptionDto.ValueDto::name)
                        .collect(Collectors.joining(", "))
        ).append(". ");

        return optionInfo.toString();
    }
}
