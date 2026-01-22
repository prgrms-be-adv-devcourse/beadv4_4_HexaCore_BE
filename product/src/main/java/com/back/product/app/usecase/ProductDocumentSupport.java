package com.back.product.app.usecase;

import com.back.product.adapter.out.ProductDocumentRepository;
import com.back.product.document.ProductDocument;
import com.back.product.dto.enums.ProductSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDocumentSupport {
    private final ProductDocumentRepository productDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional(readOnly = true)
    public List<ProductDocument> findProductPage(Query searchQuery, ProductSortType sort, Long page, Long size) {
        Pageable pageable = buildPageable(sort, page, size);

        searchQuery.setPageable(pageable);

        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        return searchHits.stream().map(SearchHit::getContent).toList();
    }

    private Pageable buildPageable(ProductSortType sortType, Long page, Long size) {
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
}
