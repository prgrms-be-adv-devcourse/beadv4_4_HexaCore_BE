package com.back.product.app.usecase.query;

import com.back.product.document.ProductDocument;
import com.back.product.dto.enums.ProductSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDocumentSupport {
    private final ElasticsearchOperations elasticsearchOperations;

    @Transactional(readOnly = true)
    public PageImpl<ProductDocument> findProductPage(Query searchQuery) {
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        List<ProductDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent) .toList();

        Pageable pageable = searchQuery.getPageable();
        long totalHits = searchHits.getTotalHits();

        return new PageImpl<>(content, pageable, totalHits);
    }
}
