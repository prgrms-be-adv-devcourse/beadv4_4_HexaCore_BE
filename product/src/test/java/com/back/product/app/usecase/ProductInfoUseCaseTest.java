package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.ProductInfoRepository;
import com.back.product.domain.Brand;
import com.back.product.domain.Category;
import com.back.product.domain.ProductInfo;
import com.back.product.dto.command.ProductInfoDataCommand;
import com.back.product.mapper.ProductInfoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductInfoUseCase 단위 테스트")
class ProductInfoUseCaseTest {

    @InjectMocks
    private ProductInfoUseCase productInfoUseCase;

    @Mock
    private ProductInfoMapper productInfoMapper;

    @Mock
    private ProductInfoRepository productInfoRepository;

    @Mock
    private ProductSupport productSupport;

    private Brand brand;
    private Category category;
    private ProductInfo productInfo;

    @BeforeEach
    void setUp() {
        brand = Brand.builder().name("Test Brand").imageUrl("logo.png").build();
        category = Category.builder().name("Test Category").imageUrl("img.png").build();
        productInfo = ProductInfo.builder()
                .id(1L)
                .brand(brand)
                .category(category)
                .name("Test Product")
                .productCode("TP-01")
                .releasePrice(BigDecimal.valueOf(10000))
                .releasedDate(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("createProductInfo 메서드")
    class CreateProductInfoTest {

        @Test
        @DisplayName("성공: 새로운 상품 정보를 생성한다")
        void createProductInfo_Success() {
            String name = "New Product";
            String code = "NP-01";
            BigDecimal releasePrice = BigDecimal.valueOf(20000);
            LocalDateTime releasedDate = LocalDateTime.now();

            // given
            ProductInfoDataCommand command = ProductInfoDataCommand.builder()
                    .brand(brand)
                    .category(category)
                    .name(name)
                    .code(code)
                    .releasePrice(releasePrice)
                    .releasedDate(releasedDate)
                    .build();
            given(productSupport.existsProductInfoByBrandAndCode(any(Brand.class), anyString())).willReturn(false);
            given(productInfoMapper.toEntity(command)).willReturn(productInfo);
            given(productInfoRepository.save(productInfo)).willReturn(productInfo);

            // when
            ProductInfo result = productInfoUseCase.createProductInfo(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getProductCode()).isEqualTo("TP-01");
            verify(productSupport).existsProductInfoByBrandAndCode(brand, "NP-01");
            verify(productInfoRepository).save(productInfo);
        }

        @Test
        @DisplayName("실패: 동일한 브랜드 내에 상품 코드가 중복되면 예외를 발생시킨다")
        void createProductInfo_Fail_DuplicateCode() {
            // given
            ProductInfoDataCommand command = ProductInfoDataCommand.builder()
                    .brand(brand)
                    .category(category)
                    .name("New Product")
                    .code("TP-01")
                    .releasePrice(BigDecimal.valueOf(20000))
                    .releasedDate(LocalDateTime.now())
                    .build();
            given(productSupport.existsProductInfoByBrandAndCode(brand, "TP-01")).willReturn(true);

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productInfoUseCase.createProductInfo(command)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.DUPLICATE_PRODUCT_INFO);
            verify(productInfoRepository, never()).save(any(ProductInfo.class));
        }
    }

    @Nested
    @DisplayName("updateProductInfo 메서드")
    class UpdateProductInfoTest {
        @Test
        @DisplayName("성공: 상품 정보를 수정한다")
        void updateProductInfo_Success() {
            // given
            ProductInfoDataCommand command = ProductInfoDataCommand.builder()
                    .brand(brand)
                    .category(category)
                    .name("Updated Name")
                    .code("UTP-01")
                    .releasePrice(BigDecimal.valueOf(12000))
                    .releasedDate(LocalDateTime.now())
                    .build();

            given(productSupport.findProductInfoById(1L)).willReturn(Optional.of(productInfo));

            // when
            ProductInfo result = productInfoUseCase.updateProductInfo(1L, command);

            // then
            assertThat(result.getName()).isEqualTo("Updated Name");
            assertThat(result.getProductCode()).isEqualTo("UTP-01");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품 정보 ID이면 예외를 발생시킨다")
        void updateProductInfo_Fail_NotFound() {
            // given
            ProductInfoDataCommand command = ProductInfoDataCommand.builder()
                    .brand(brand)
                    .category(category)
                    .name("Updated Name")
                    .code("UTP-01")
                    .releasePrice(BigDecimal.valueOf(12000))
                    .releasedDate(LocalDateTime.now())
                    .build();
            given(productSupport.findProductInfoById(99L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productInfoUseCase.updateProductInfo(99L, command)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.PRODUCT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("deleteProductInfo 메서드")
    class DeleteProductInfoTest {
        @Test
        @DisplayName("성공: 상품 정보를 삭제한다")
        void deleteProductInfo_Success() {
            // given
            given(productSupport.findProductInfoById(1L)).willReturn(Optional.of(productInfo));

            // when
            productInfoUseCase.deleteProductInfo(1L);

            // then
            verify(productInfoRepository).delete(productInfo);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품 정보 ID이면 예외를 발생시킨다")
        void deleteProductInfo_Fail_NotFound() {
            // given
            given(productSupport.findProductInfoById(99L)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productInfoUseCase.deleteProductInfo(99L)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.PRODUCT_INFO_NOT_FOUND);
            verify(productInfoRepository, never()).delete(any(ProductInfo.class));
        }
    }

    @Nested
    @DisplayName("findProductInfo 메서드")
    class FindProductInfoTest {

        @Test
        @DisplayName("성공: 상품 정보를 조회한다")
        void findProductInfo_Success() {
            // given
            long productInfoId = 1L;
            given(productSupport.findProductInfoById(productInfoId)).willReturn(Optional.of(productInfo));

            // when
            ProductInfo result = productInfoUseCase.findProductInfo(productInfoId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(productInfoId);
            verify(productSupport).findProductInfoById(productInfoId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품 정보 ID이면 예외를 발생시킨다")
        void findProductInfo_Fail_NotFound() {
            // given
            long productInfoId = 99L;
            given(productSupport.findProductInfoById(productInfoId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productInfoUseCase.findProductInfo(productInfoId)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.PRODUCT_INFO_NOT_FOUND);
            verify(productSupport).findProductInfoById(productInfoId);
        }
    }
}
