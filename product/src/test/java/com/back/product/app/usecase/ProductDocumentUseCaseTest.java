package com.back.product.app.usecase;

import com.back.product.document.ProductDocument;
import com.back.product.dto.enums.ProductSortType;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductSearchListResponseDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import com.back.product.mapper.ProductDocumentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Nested
    @DisplayName("findProductPage 메서드")
    class FindProductPageTest {

        @Test
        @DisplayName("성공: 검색 조건으로 상품 페이지를 조회한다")
        void findProductPage_Success() {
            // given
            ProductSearchRequestDto request = ProductSearchRequestDto.builder()
                    .keyword("Test")
                    .brandIds(List.of(1L))
                    .categoryIds(List.of(1L))
                    .minPrice(BigDecimal.valueOf(10000))
                    .maxPrice(BigDecimal.valueOf(100000))
                    .excludeSoldOut(true)
                    .sort(ProductSortType.PRICE_HIGH)
                    .build();
            long page = 0L;
            long size = 20L;

            ProductDocument document = ProductDocument.builder().productName("Test Product").build();
            List<ProductDocument> documents = List.of(document);
            
            ProductSearchResponseDto dto = ProductSearchResponseDto.builder().productName("Test Product").build();

            given(productDocumentSupport.findProductPage(any(Query.class), any(ProductSortType.class), any(Long.class), any(Long.class)))
                    .willReturn(documents);
            given(productDocumentMapper.toDto(document)).willReturn(dto);

            // when
            ProductSearchListResponseDto result = productDocumentUseCase.findProductPage(request, page, size);

            // then
            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(productDocumentSupport).findProductPage(queryCaptor.capture(), any(ProductSortType.class), any(Long.class), any(Long.class));
            
            verify(productDocumentMapper).toDto(document);
            assertThat(result).isNotNull();
            assertThat(result.products()).hasSize(1);
            assertThat(result.products().getFirst().productName()).isEqualTo("Test Product");
        }
        
        @Test
        @DisplayName("성공: 조건이 없는 경우에도 정상적으로 동작한다")
        void findProductPage_Success_NoConditions() {
            // given
            ProductSearchRequestDto request = ProductSearchRequestDto.builder()
                    .sort(ProductSortType.LATEST)
                    .build();
            long page = 0L;
            long size = 10L;

            ProductDocument document = ProductDocument.builder().productName("Another Product").build();
            List<ProductDocument> documents = List.of(document);
            ProductSearchResponseDto dto = ProductSearchResponseDto.builder().productName("Another Product").build();
            
            given(productDocumentSupport.findProductPage(any(Query.class), any(ProductSortType.class), any(Long.class), any(Long.class)))
                    .willReturn(documents);
            given(productDocumentMapper.toDto(document)).willReturn(dto);

            // when
            ProductSearchListResponseDto result = productDocumentUseCase.findProductPage(request, page, size);

            // then
            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
            verify(productDocumentSupport).findProductPage(queryCaptor.capture(), any(ProductSortType.class), any(Long.class), any(Long.class));
            
            // Verify result
            assertThat(result).isNotNull();
            assertThat(result.products()).hasSize(1);
            assertThat(result.products().getFirst().productName()).isEqualTo("Another Product");
        }
    }
}
