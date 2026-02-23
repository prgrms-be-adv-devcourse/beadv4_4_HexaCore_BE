package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.ProductImageRepository;
import com.back.product.adapter.out.persistence.ProductOptionValuesRepository;
import com.back.product.adapter.out.persistence.ProductRepository;
import com.back.product.domain.*;
import com.back.product.dto.command.ProductVariantCreateCommand;
import com.back.product.dto.command.ProductVariantUpdateCommand;
import com.back.product.dto.model.ProductDetailDto;
import com.back.product.dto.model.ProductDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.mapper.ProductImageMapper;
import com.back.product.mapper.ProductInfoMapper;
import com.back.product.mapper.ProductMapper;
import com.back.product.mapper.ProductOptionValuesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductUseCase 단위 테스트")
class ProductUseCaseTest {

    @InjectMocks
    private ProductUseCase productUseCase;

    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductImageMapper productImageMapper;
    @Mock
    private ProductOptionValuesMapper productOptionValuesMapper;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductOptionValuesRepository productOptionValuesRepository;
    @Mock
    private ProductImageRepository productImageRepository;
    @Mock
    private ProductSupport productSupport; // Though not used in create, good to have for other methods

    @Mock
    private ProductInfoMapper productInfoMapper;

    private ProductInfo productInfo;
    private OptionValue optionValue1, optionValue2;

    @BeforeEach
    void setUp() {
        productInfo = ProductInfo.builder().id(1L).name("Test Product Info").build();
        optionValue1 = OptionValue.builder().id(10L).value("Black").build();
        optionValue2 = OptionValue.builder().id(20L).value("95").build();
    }

    @Nested
    @DisplayName("createMultipleProduct 메서드")
    class CreateMultipleProductTest {

        @Test
        @DisplayName("성공: 여러 상품(variant)을 생성하고 DTO로 변환하여 반환한다")
        void createMultipleProduct_Success() {
            // given
            ProductVariantCreateCommand variantDto1 = new ProductVariantCreateCommand(List.of(10L, 20L), 100L, List.of("img1.jpg"));
            ProductVariantCreateCommand variantDto2 = new ProductVariantCreateCommand(List.of(10L), 50L, List.of("img2.jpg"));
            List<ProductVariantCreateCommand> variants = List.of(variantDto1, variantDto2);
            List<OptionValue> optionValues = List.of(optionValue1, optionValue2);

            Product product1 = Product.builder().id(1L).inventory(100L).build();
            Product product2 = Product.builder().id(2L).inventory(50L).build();

            // Mocking mappers
            given(productMapper.toEntity(productInfo, 100L)).willReturn(product1);
            given(productMapper.toEntity(productInfo, 50L)).willReturn(product2);
            given(productOptionValuesMapper.toEntity(any(Product.class), any(OptionValue.class)))
                    .willAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        OptionValue ov = invocation.getArgument(1);
                        return ProductOptionValues.builder().product(p).optionValue(ov).build();
                    });
            given(productImageMapper.toEntity(any(Product.class), any(String.class)))
                    .willAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        String url = invocation.getArgument(1);
                        return com.back.product.domain.ProductImage.builder().product(p).imageUrl(url).build();
                    });

            // Mocking repository saves
            given(productRepository.saveAll(any(List.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(productOptionValuesRepository.saveAll(any(List.class))).willAnswer(invocation -> invocation.getArgument(0));
            given(productImageRepository.saveAll(any(List.class))).willAnswer(invocation -> invocation.getArgument(0));
            
            // findOptionValuesAsMap 내부에서 호출되는 getAllOptionValues 스터빙 추가
            given(productSupport.getAllOptionValues(anyList())).willReturn(optionValues);
            
            // Mocking final DTO conversion
            given(productMapper.toDto(any(Product.class), any(List.class), any(List.class)))
                    .willAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        return ProductDto.builder().productId(p.getId()).inventory(p.getInventory()).build();
                    });


            // when
            List<ProductDto> resultDtos = productUseCase.createMultipleProduct(productInfo, variants);

            // then
            // Verify mappers were called correctly
            verify(productMapper, times(2)).toEntity(any(ProductInfo.class), any(Long.class));
            verify(productOptionValuesMapper, times(3)).toEntity(any(Product.class), any(OptionValue.class)); // 2 for variant1, 1 for variant2
            verify(productImageMapper, times(2)).toEntity(any(Product.class), any(String.class));

            // Verify repositories were called to save entities
            ArgumentCaptor<List<Product>> productCaptor = ArgumentCaptor.forClass(List.class);
            verify(productRepository).saveAll(productCaptor.capture());
            assertThat(productCaptor.getValue()).hasSize(2);

            ArgumentCaptor<List<ProductOptionValues>> povCaptor = ArgumentCaptor.forClass(List.class);
            verify(productOptionValuesRepository).saveAll(povCaptor.capture());
            assertThat(povCaptor.getValue()).hasSize(3);

            ArgumentCaptor<List<com.back.product.domain.ProductImage>> imageCaptor = ArgumentCaptor.forClass(List.class);
            verify(productImageRepository).saveAll(imageCaptor.capture());
            assertThat(imageCaptor.getValue()).hasSize(2);

            // Verify the final result
            assertThat(resultDtos).hasSize(2);
            assertThat(resultDtos.stream().map(ProductDto::inventory).toList()).containsExactlyInAnyOrder(100L, 50L);
        }
    }

    @Nested
    @DisplayName("updateMultipleProduct 메서드")
    class UpdateMultipleProductTest {

        @Test
        @DisplayName("성공: 상품(variant)들을 수정, 추가, 삭제한다")
        void updateMultipleProduct_Success() {
            // given
            Product existProductToUpdate = Product.builder().id(1L).inventory(100L).productInfo(productInfo).build();
            Product existProductToDelete = Product.builder().id(2L).inventory(200L).productInfo(productInfo).build();
            List<Product> existProducts = List.of(existProductToUpdate, existProductToDelete);

            ProductVariantUpdateCommand updateDto = new ProductVariantUpdateCommand(1L, List.of(10L), 150L, List.of("update.jpg"));
            ProductVariantUpdateCommand createDto = new ProductVariantUpdateCommand(null, List.of(20L), 300L, List.of("new.jpg"));
            List<ProductVariantUpdateCommand> variants = List.of(updateDto, createDto);

            List<OptionValue> optionValues = List.of(optionValue1, optionValue2);
            Product newProduct = Product.builder().inventory(300L).productInfo(productInfo).build();

            // Mocking for the first part of the method
            given(productSupport.getAllProductsByProductInfo(productInfo)).willReturn(existProducts);
            // findOptionValuesAsMap 내부에서 호출되는 getAllOptionValues 스터빙 추가
            given(productSupport.getAllOptionValues(anyList())).willReturn(optionValues);

            // Mocking for mappers called during update AND creation
            given(productMapper.toEntity(productInfo, 300L)).willReturn(newProduct);
            given(productOptionValuesMapper.toEntity(any(Product.class), any(OptionValue.class)))
                    .willAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        OptionValue ov = invocation.getArgument(1);
                        return ProductOptionValues.builder().product(p).optionValue(ov).build();
                    });
            given(productImageMapper.toEntity(any(Product.class), any(String.class)))
                    .willAnswer(invocation -> {
                        Product p = invocation.getArgument(0);
                        String url = invocation.getArgument(1);
                        return com.back.product.domain.ProductImage.builder().product(p).imageUrl(url).build();
                    });

            // Mocking for the final DTO conversion part to prevent NPE
            given(productSupport.getAllProductOptionValuesByProductsIn(any())).willReturn(List.of());
            given(productSupport.getAllProductImagesByProductsIn(any())).willReturn(List.of());
            given(productMapper.toDto(any(), any(), any())).willReturn(ProductDto.builder().build());


            // when
            productUseCase.updateMultipleProduct(productInfo, variants);

            // then
            // 1. Verify deletions
            ArgumentCaptor<List<Product>> deleteCaptor = ArgumentCaptor.forClass(List.class);
            verify(productRepository).deleteAll(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue()).hasSize(1);
            assertThat(deleteCaptor.getValue().get(0).getId()).isEqualTo(existProductToDelete.getId());

            // 2. Verify updates
            assertThat(existProductToUpdate.getInventory()).isEqualTo(150L);

            // 3. Verify creations and saveAll invocations
            ArgumentCaptor<List<Product>> createdProductsCaptor = ArgumentCaptor.forClass(List.class);
            verify(productRepository).saveAll(createdProductsCaptor.capture());
            List<Product> savedProducts = createdProductsCaptor.getValue();
            assertThat(savedProducts).hasSize(1);
            assertThat(savedProducts.get(0)).isEqualTo(newProduct);

            verify(productOptionValuesRepository, times(2)).saveAll(any());
            verify(productImageRepository, times(2)).saveAll(any());
        }
    }

    @Nested
    @DisplayName("deleteMultipleProduct 메서드")
    class DeleteMultipleProductTest {

        @Test
        @DisplayName("성공: ProductInfo에 속한 모든 상품(variant)들을 삭제한다")
        void deleteMultipleProduct_Success() {
            // given
            long productInfoId = 1L;
            Product product1 = Product.builder().id(1L).build();
            Product product2 = Product.builder().id(2L).build();
            List<Product> productsToDelete = List.of(product1, product2);

            given(productSupport.getAllProductsByProductInfoId(productInfoId)).willReturn(productsToDelete);

            // when
            productUseCase.deleteMultipleProduct(productInfoId);

            // then
            verify(productSupport).getAllProductsByProductInfoId(productInfoId);

            ArgumentCaptor<List<Product>> deleteCaptor = ArgumentCaptor.forClass(List.class);
            verify(productOptionValuesRepository).deleteAllByProductIn(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue()).hasSize(2);

            verify(productImageRepository).deleteAllByProductIn(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue()).hasSize(2);

            verify(productRepository).deleteAll(deleteCaptor.capture());
            assertThat(deleteCaptor.getValue()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findAllProduct 메서드")
    class FindAllProductTest {

        @Test
        @DisplayName("성공: ProductInfo에 속한 모든 상품(variant)들을 조회한다")
        void findAllProduct_Success() {
            // given
            Product product1 = Product.builder().id(1L).build();
            Product product2 = Product.builder().id(2L).build();
            List<Product> products = List.of(product1, product2);

            given(productSupport.getAllProductsByProductInfo(productInfo)).willReturn(products);
            given(productSupport.getAllProductOptionValuesByProductsIn(products)).willReturn(List.of());
            given(productSupport.getAllProductImagesByProductsIn(products)).willReturn(List.of());
            given(productMapper.toDto(any(), any(), any())).willReturn(ProductDto.builder().build());

            // when
            List<ProductDto> result = productUseCase.findAllProduct(productInfo);

            // then
            assertThat(result).hasSize(2);
            verify(productSupport).getAllProductsByProductInfo(productInfo);
            verify(productSupport).getAllProductOptionValuesByProductsIn(products);
            verify(productSupport).getAllProductImagesByProductsIn(products);
            verify(productMapper, times(2)).toDto(any(), any(), any());
        }

        @Test
        @DisplayName("실패: ProductInfo에 속한 상품이 없으면 예외를 발생시킨다")
        void findAllProduct_Fail_NoProducts() {
            // given
            given(productSupport.getAllProductsByProductInfo(productInfo)).willReturn(Collections.emptyList());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productUseCase.findAllProduct(productInfo)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.ENTITY_NOT_FOUND);

            verify(productSupport).getAllProductsByProductInfo(productInfo);
            verify(productSupport, never()).getAllProductOptionValuesByProductsIn(any());
            verify(productSupport, never()).getAllProductImagesByProductsIn(any());
        }
    }

    @Nested
    @DisplayName("findMultipleProduct 메서드")
    class FindMultipleProductTest {

        @Test
        @DisplayName("성공: 여러 상품 ID로 조회하여 ProductDetailDto 목록을 반환한다")
        void findMultipleProduct_Success() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            Product product1 = Product.builder().id(1L).productInfo(productInfo).inventory(10L).build();
            Product product2 = Product.builder().id(2L).productInfo(productInfo).inventory(20L).build();
            List<Product> foundProducts = List.of(product1, product2);

            ProductImage image1 = ProductImage.builder().product(product1).imageUrl("img1.jpg").build();
            ProductOptionValues option1 = ProductOptionValues.builder().product(product1).optionValue(optionValue1).build();

            given(productSupport.findMultipleProductByIds(productIds)).willReturn(foundProducts);
            given(productSupport.getAllProductImagesByProductsIn(foundProducts)).willReturn(List.of(image1));
            given(productSupport.getAllProductOptionValuesByProductsIn(foundProducts)).willReturn(List.of(option1));

            // productInfoMapper.toDto 스터빙 변경: willAnswer를 사용하여 호출된 ProductInfo로부터 DTO 생성
            ProductInfoDto expectedProductInfoDto = ProductInfoDto.builder().productInfoId(productInfo.getId()).name(productInfo.getName()).build();
            given(productInfoMapper.toDto(any(ProductInfo.class))).willReturn(expectedProductInfoDto);

            // productMapper.toDto 스터빙 (findMultipleProduct 내부에서 호출됨)
            ProductDto productDto1 = ProductDto.builder().productId(1L).inventory(10L).build();
            ProductDto productDto2 = ProductDto.builder().productId(2L).inventory(20L).build();
            // any(Product.class)를 사용하여 Product 파라미터에 대한 구체적인 인스턴스 매칭 회피
            given(productMapper.toDto(any(Product.class), anyList(), anyList()))
                    .willReturn(productDto1) // 첫 번째 호출 시 productDto1 반환
                    .willReturn(productDto2); // 두 번째 호출 시 productDto2 반환

            given(productMapper.toDetailDto(any(ProductInfoDto.class), anyList()))
                    .willAnswer(invocation -> {
                        ProductInfoDto infoDto = invocation.getArgument(0);
                        List<ProductDto> dtos = invocation.getArgument(1);
                        return ProductDetailDto.builder()
                                .productInfo(infoDto)
                                .products(dtos)
                                .build();
                    });

            // when
            List<ProductDetailDto> result = productUseCase.findMultipleProduct(productIds);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1); // 하나의 ProductInfo에 대한 ProductDetailDto가 반환됨
            ProductDetailDto productDetail = result.getFirst();
            assertThat(productDetail).isNotNull(); // productDetail이 null이 아님을 방어적으로 확인
            assertThat(productDetail.productInfo()).isEqualTo(expectedProductInfoDto); // 비교할 객체를 변경
            assertThat(productDetail.products()).containsExactlyInAnyOrder(productDto1, productDto2);

            verify(productSupport).findMultipleProductByIds(productIds);
            verify(productSupport).getAllProductImagesByProductsIn(foundProducts);
            verify(productSupport).getAllProductOptionValuesByProductsIn(foundProducts);
            verify(productInfoMapper, times(1)).toDto(productInfo); // productInfo는 한 번만 매핑됨
            verify(productMapper, times(2)).toDto(any(Product.class), any(), any());
        }

        @Test
        @DisplayName("실패: 요청한 ID와 다른 수의 상품이 조회되면 예외를 발생시킨다")
        void findMultipleProduct_Fail_NotFound() {
            // given
            List<Long> productIds = List.of(1L, 999L);
            Product product1 = Product.builder().id(1L).productInfo(productInfo).build();
            List<Product> foundProducts = List.of(product1); // Only one found

            given(productSupport.findMultipleProductByIds(productIds)).willReturn(foundProducts);

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productUseCase.findMultipleProduct(productIds)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.ENTITY_NOT_FOUND);

            verify(productSupport).findMultipleProductByIds(productIds);
            verify(productSupport, never()).getAllProductImagesByProductsIn(any());
            verify(productSupport, never()).getAllProductOptionValuesByProductsIn(any());
            verify(productInfoMapper, never()).toDto((ProductInfo) any());
            verify(productMapper, never()).toDto(any(), any(), any());
        }
    }
}
