package com.back.product.app.usecase;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDocumentSupport {
    private final ElasticsearchOperations elasticsearchOperations;

    @Loggable
    public PageImpl<ProductDocument> findProductPage(Query searchQuery) {
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(searchQuery, ProductDocument.class);

        List<ProductDocument> content = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent) .toList();

        Pageable pageable = searchQuery.getPageable();
        long totalHits = searchHits.getTotalHits();

        return new PageImpl<>(content, pageable, totalHits);
    }

    @Loggable
    public ProductDocument findProductWithEmbedding(Query query) {
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);

        if (!hits.hasSearchHits()) {
            throw new CustomException(FailureCode.PRODUCT_INFO_NOT_FOUND);
        }

        return hits.getSearchHit(0).getContent();
    }
}
