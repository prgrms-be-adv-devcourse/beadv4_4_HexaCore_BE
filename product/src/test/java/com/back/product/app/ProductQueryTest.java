package com.back.product.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.BaseIntegrationTest;
import com.back.product.adapter.out.*;
import com.back.product.domain.*;
import com.back.product.dto.response.ProductResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Testcontainers
@DisplayName("ProductQuery 통합 테스트")
class ProductQueryTest extends BaseIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    // DB-Setup & Verification
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductInfoRepository productInfoRepository;
    @Autowired private ProductImageRepository productImageRepository;
    @Autowired private ProductOptionValuesRepository productOptionValuesRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private OptionGroupRepository optionGroupRepository;
    @Autowired private OptionValueRepository optionValueRepository;

    private Brand savedBrand;
    private Category savedCategory;
    private OptionValue savedOptionValueBlack;
    private OptionValue savedOptionValue95;
    private ProductInfo savedProductInfo;

    @BeforeEach
    void setUp() {
        // Given: Prerequisite data
        savedBrand = brandRepository.save(Brand.builder().name("Test Brand").imageUrl("logo.png").build());
        savedCategory = categoryRepository.save(Category.builder().name("Test Category").imageUrl("img.png").build());

        OptionGroup savedOptionGroupColor = optionGroupRepository.save(OptionGroup.builder().name("Color").build());
        OptionGroup savedOptionGroupSize = optionGroupRepository.save(OptionGroup.builder().name("Size").build());

        savedOptionValueBlack = optionValueRepository.save(OptionValue.builder().optionGroup(savedOptionGroupColor).value("Black").build());
        savedOptionValue95 = optionValueRepository.save(OptionValue.builder().optionGroup(savedOptionGroupSize).value("95").build());

        savedProductInfo = productInfoRepository.save(ProductInfo.builder()
                .brand(savedBrand)
                .category(savedCategory)
                .name("Test Product")
                .productCode("TEST-001")
                .releasePrice(BigDecimal.valueOf(100000))
                .releasedDate(LocalDateTime.now())
                .build());

        Product savedProduct = productRepository.save(Product.builder()
                .productInfo(savedProductInfo)
                .inventory(50L)
                .build());

        productImageRepository.save(ProductImage.builder().product(savedProduct).imageUrl("test_image.jpg").build());

        productOptionValuesRepository.saveAll(List.of(
                ProductOptionValues.builder().product(savedProduct).optionValue(savedOptionValueBlack).build(),
                ProductOptionValues.builder().product(savedProduct).optionValue(savedOptionValue95).build()
        ));
    }

    @Nested
    @DisplayName("getProductDetail 메서드")
    class GetProductDetailTest {

        @Test
        @DisplayName("성공: 상품 상세 정보를 정상적으로 조회한다")
        void getProductDetail_Success() {
            // when
            ProductResponseDto response = productFacade.getProductDetail(savedProductInfo.getId());

            // then
            assertThat(response).isNotNull();
            // ProductInfo 검증
            assertThat(response.productInfo().productInfoId()).isEqualTo(savedProductInfo.getId());
            assertThat(response.productInfo().name()).isEqualTo("Test Product");
            assertThat(response.productInfo().brand().name()).isEqualTo("Test Brand");
            assertThat(response.productInfo().category().name()).isEqualTo("Test Category");

            // Product(variant) 검증
            assertThat(response.products().get(0).inventory()).isEqualTo(50L);

            // ProductImage 검증
            assertThat(response.products().get(0).imageUrls()).containsExactly("test_image.jpg");

            // ProductOption 검증
            assertThat(response.products().get(0).options()).hasSize(2);
            assertThat(response.products().get(0).options().stream().map(o -> o.groupName() + ":" + o.value()))
                    .containsExactlyInAnyOrder("Color:Black", "Size:95");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회 시 예외를 발생시킨다")
        void getProductDetail_Fail_ProductInfoNotFound() {
            // given
            Long nonExistentId = 999L;

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productFacade.getProductDetail(nonExistentId));

            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.PRODUCT_INFO_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 상품 정보는 있으나 하위 상품(variant)이 없을 경우 예외를 발생시킨다")
        void getProductDetail_Fail_ProductsNotFound() {
            // given
            productImageRepository.deleteAll();
            productOptionValuesRepository.deleteAll();
            productRepository.deleteAll();

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productFacade.getProductDetail(savedProductInfo.getId()));

            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.ENTITY_NOT_FOUND);
        }
    }
}
