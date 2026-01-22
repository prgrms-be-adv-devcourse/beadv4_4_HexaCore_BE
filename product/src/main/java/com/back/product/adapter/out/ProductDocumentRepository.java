package com.back.product.adapter.out;

import com.back.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, String> {
}
