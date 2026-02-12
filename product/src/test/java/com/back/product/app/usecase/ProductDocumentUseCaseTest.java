package com.back.product.app.usecase;

import com.back.ai.app.usecase.EmbeddingUseCase;
import com.back.product.app.usecase.command.ProductDocumentUseCase;
import com.back.product.app.usecase.query.ProductDocumentSupport;
import com.back.product.document.ProductDocument;
import com.back.product.dto.command.ProductSearchCommand;
import com.back.product.dto.enums.ProductSortType;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.dto.model.ProductSearchDto;
import com.back.product.mapper.ProductDocumentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductDocumentUseCase 단위 테스트")
class ProductDocumentUseCaseTest {

    @InjectMocks
    private ProductDocumentUseCase productDocumentUseCase;

    @Mock
    private ProductDocumentSupport productDocumentSupport;

    @Mock
    private ProductDocumentMapper productDocumentMapper;

    @Mock
    private EmbeddingUseCase embeddingUseCase;

    @Nested
    @DisplayName("findProductPage 메서드")
    class FindProductPageTest {

        @Test
        @DisplayName("성공: 하이브리드 검색 조건으로 상품 페이지를 조회한다")
        void findProductPage_HybridSearch_Success() {
            // given
            ProductSearchCommand command = ProductSearchCommand.builder()
                    .keyword("Test")
                    .brandIds(List.of(1L))
                    .categoryIds(List.of(1L))
                    .minPrice(BigDecimal.valueOf(10000))
                    .maxPrice(BigDecimal.valueOf(100000))
                    .sort(ProductSortType.PRICE_HIGH)
                    .page(0L)
                    .size(20L)
                    .build();

            // Dummy data for mocking
            float[] dummyEmbedding = new float[]{0.1f, 0.2f, 0.3f};
            ProductDocument document = ProductDocument.builder().productInfo(ProductDocument.ProductInfo.builder().productName("Test Product").build()).build();
            List<ProductDocument> documents = List.of(document);
            PageImpl<ProductDocument> productPage = new PageImpl<>(documents, PageRequest.of(Math.toIntExact(command.page()), Math.toIntExact(command.size())), documents.size());
            ProductSearchDto dto = ProductSearchDto.builder().productName("Test Product").build();

            // Mocking dependencies
            given(embeddingUseCase.generateEmbeddings(command.keyword())).willReturn(dummyEmbedding);
            given(productDocumentSupport.findProductPage(any(Query.class))).willReturn(productPage);
            given(productDocumentMapper.toDto(document)).willReturn(dto);

            // when
            ProductSearchResponseDto result = productDocumentUseCase.findProductPage(command);

            // then
            // 1. Verify that dependent methods were called with the correct arguments
            verify(embeddingUseCase).generateEmbeddings(command.keyword());
            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(productDocumentSupport).findProductPage(queryCaptor.capture());
            verify(productDocumentMapper).toDto(document);

            // 2. Assert the captured query to check for hybrid search components
            Query capturedQuery = queryCaptor.getValue();
            assertThat(capturedQuery).isInstanceOf(NativeQuery.class);
            NativeQuery nativeQuery = (NativeQuery) capturedQuery;
            assertThat(nativeQuery.getKnnSearches()).isNotNull();
            assertThat(nativeQuery.getKnnSearches()).hasSize(1);
            assertThat(nativeQuery.getQuery()).isNotNull(); // Check that the bool query part also exists

            // 3. Assert the final response DTO
            assertThat(result).isNotNull();
            assertThat(result.products()).hasSize(1);
            assertThat(result.products().getFirst().productName()).isEqualTo("Test Product");
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.currentPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공: 조건이 없는 경우(키워드 없음)에도 정상적으로 동작한다")
        void findProductPage_Success_NoKeyword() {
            // given
            ProductSearchCommand command = ProductSearchCommand.builder()
                    .sort(ProductSortType.LATEST)
                    .page(0L)
                    .size(10L)
                    .build();

            ProductDocument document = ProductDocument.builder().productInfo(ProductDocument.ProductInfo.builder().productName("Another Product").build()).build();
            List<ProductDocument> documents = List.of(document);
            PageImpl<ProductDocument> productPage = new PageImpl<>(documents, PageRequest.of(Math.toIntExact(command.page()), Math.toIntExact(command.size())), documents.size());
            ProductSearchDto dto = ProductSearchDto.builder().productName("Another Product").build();

            given(productDocumentSupport.findProductPage(any(Query.class))).willReturn(productPage);
            given(productDocumentMapper.toDto(document)).willReturn(dto);

            // when
            ProductSearchResponseDto result = productDocumentUseCase.findProductPage(command);

            // then
            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(productDocumentSupport).findProductPage(queryCaptor.capture());

            // Assert that KNN search is NOT present
            Query capturedQuery = queryCaptor.getValue();
            assertThat(capturedQuery).isInstanceOf(NativeQuery.class);
            NativeQuery nativeQuery = (NativeQuery) capturedQuery;
            assertThat(nativeQuery.getKnnSearches()).isNullOrEmpty(); // Check that KNN part does not exist

            // Verify result
            assertThat(result).isNotNull();
            assertThat(result.products()).hasSize(1);
            assertThat(result.products().getFirst().productName()).isEqualTo("Another Product");
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.currentPage()).isEqualTo(0);
        }
    }
}
