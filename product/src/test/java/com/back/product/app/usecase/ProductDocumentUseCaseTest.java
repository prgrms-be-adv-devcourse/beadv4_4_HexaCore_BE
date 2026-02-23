package com.back.product.app.usecase;

import com.back.ai.app.usecase.EmbeddingUseCase;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.document.ProductDocumentRepository;
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
    private ProductDocumentRepository productDocumentRepository;

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
                    .page(0)
                    .size(20)
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
                    .page(0)
                    .size(10)
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

    @Nested
    @DisplayName("findSimilarProducts 메서드")
    class FindSimilarProductsTest {
        private final Long PRODUCT_INFO_ID = 1L;
        private final Long OTHER_PRODUCT_INFO_ID_1 = 2L;
        private final Long OTHER_PRODUCT_INFO_ID_2 = 3L;
        private final float[] DUMMY_EMBEDDING = {0.1f, 0.2f, 0.3f};

        @Test
        @DisplayName("성공: 유사 상품을 정상적으로 조회한다 (자기 자신 제외)")
        void findSimilarProducts_Success_ExcludesSelf() {
            // given
            Integer page = 0;
            Integer size = 5;

            ProductDocument targetProduct = ProductDocument.builder()
                    .productInfo(
                            ProductDocument.ProductInfo.builder()
                                    .productInfoId(PRODUCT_INFO_ID)
                                    .build()
                    )
                    .embedding(DUMMY_EMBEDDING)
                    .productInfo(ProductDocument.ProductInfo.builder().productName("Target Product").build())
                    .build();

            ProductDocument similarProduct1 = ProductDocument.builder()
                    .productInfo(
                            ProductDocument.ProductInfo.builder()
                                    .productInfoId(OTHER_PRODUCT_INFO_ID_1)
                                    .build()
                    )
                    .productInfo(ProductDocument.ProductInfo.builder().productName("Similar Product 1").build())
                    .build();
            ProductDocument similarProduct2 = ProductDocument.builder()
                    .productInfo(
                            ProductDocument.ProductInfo.builder()
                                    .productInfoId(OTHER_PRODUCT_INFO_ID_2)
                                    .build()
                    )
                    .productInfo(ProductDocument.ProductInfo.builder().productName("Similar Product 2").build())
                    .build();

            List<ProductDocument> searchResults = List.of(similarProduct1, similarProduct2); // Expect only similar products
            PageImpl<ProductDocument> productPage = new PageImpl<>(searchResults, PageRequest.of(page.intValue(), size.intValue()), searchResults.size());

            ProductSearchDto dto1 = ProductSearchDto.builder().productName("Similar Product 1").build();
            ProductSearchDto dto2 = ProductSearchDto.builder().productName("Similar Product 2").build();


            given(productDocumentSupport.findProductPage(any(Query.class))).willReturn(productPage);
            given(productDocumentRepository.findById(PRODUCT_INFO_ID.toString())).willReturn(java.util.Optional.of(targetProduct));
            given(productDocumentMapper.toDto(similarProduct1)).willReturn(dto1);
            given(productDocumentMapper.toDto(similarProduct2)).willReturn(dto2);

            // when
            ProductSearchResponseDto result = productDocumentUseCase.findSimilarProducts(PRODUCT_INFO_ID, page, size);

            // then
            ArgumentCaptor<NativeQuery> queryCaptor = ArgumentCaptor.forClass(NativeQuery.class);
            verify(productDocumentSupport).findProductPage(queryCaptor.capture());
            verify(productDocumentRepository).findById(PRODUCT_INFO_ID.toString());
            verify(productDocumentMapper).toDto(similarProduct1);
            verify(productDocumentMapper).toDto(similarProduct2);

            NativeQuery capturedQuery = queryCaptor.getValue();
            assertThat(capturedQuery.getKnnSearches()).hasSize(1);
            // Verify that the filter to exclude self was applied
            assertThat(capturedQuery.getKnnSearches().getFirst().filter()).isNotNull();
            // A more detailed check for the filter would involve parsing the query, which is complex for unit tests.
            // Rely on integration tests for full query verification.

            assertThat(result).isNotNull();
            assertThat(result.products()).hasSize(2); // Should not include the target product itself
            assertThat(result.products().getFirst().productName()).isEqualTo("Similar Product 1");
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.currentPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("실패: 대상 상품의 임베딩이 없을 경우 CustomException 발생")
        void findSimilarProducts_Fail_EmbeddingNotFound() {
            // given
            Integer page = 0;
            Integer size = 5;

            ProductDocument targetProduct = ProductDocument.builder()
                    .productInfo(
                            ProductDocument.ProductInfo.builder()
                                    .productInfoId(PRODUCT_INFO_ID)
                                    .build()
                    )
                    .embedding(new float[]{}) // Empty embedding
                    .productInfo(ProductDocument.ProductInfo.builder().productName("Target Product").build())
                    .build();

            given(productDocumentRepository.findById(PRODUCT_INFO_ID.toString())).willReturn(java.util.Optional.of(targetProduct));

            // when
            org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                            productDocumentUseCase.findSimilarProducts(PRODUCT_INFO_ID, page, size))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(FailureCode.EMBEDDING_NOT_FOUND.getMessage());

            // then
            verify(productDocumentRepository).findById(PRODUCT_INFO_ID.toString());
            verify(productDocumentSupport, org.mockito.Mockito.never()).findProductPage(any(Query.class));
        }

        @Test
        @DisplayName("실패: 대상 상품을 찾을 수 없을 경우 CustomException 발생")
        void findSimilarProducts_Fail_ProductNotFound() {
            // given
            Integer page = 0;
            Integer size = 5;

            given(productDocumentRepository.findById(PRODUCT_INFO_ID.toString())).willReturn(java.util.Optional.empty());

            // when
            org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                            productDocumentUseCase.findSimilarProducts(PRODUCT_INFO_ID, page, size))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(FailureCode.PRODUCT_INFO_NOT_FOUND.getMessage());

            // then
            verify(productDocumentRepository).findById(PRODUCT_INFO_ID.toString());
            verify(productDocumentSupport, org.mockito.Mockito.never()).findProductPage(any(Query.class));
        }
        
        @Test
        @DisplayName("성공: 유사 상품이 없을 경우 빈 리스트 반환")
        void findSimilarProducts_Success_NoSimilarProducts() {
            // given
            Integer page = 0;
            Integer size = 5;

            ProductDocument targetProduct = ProductDocument.builder()
                    .productInfo(
                            ProductDocument.ProductInfo.builder()
                                    .productInfoId(PRODUCT_INFO_ID)
                                    .build()
                    )
                    .embedding(DUMMY_EMBEDDING)
                    .productInfo(ProductDocument.ProductInfo.builder().productName("Target Product").build())
                    .build();
            
            List<ProductDocument> searchResults = List.of(); // No similar products found
            PageImpl<ProductDocument> productPage = new PageImpl<>(searchResults, PageRequest.of(page.intValue(), size.intValue()), 0);

            given(productDocumentSupport.findProductPage(any(Query.class))).willReturn(productPage);
            given(productDocumentRepository.findById(PRODUCT_INFO_ID.toString())).willReturn(java.util.Optional.of(targetProduct));

            // when
            ProductSearchResponseDto result = productDocumentUseCase.findSimilarProducts(PRODUCT_INFO_ID, page, size);

            // then
            assertThat(result).isNotNull();
            assertThat(result.products()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);
            assertThat(result.totalPages()).isEqualTo(0);
            assertThat(result.currentPage()).isEqualTo(0);
        }
    }
}

