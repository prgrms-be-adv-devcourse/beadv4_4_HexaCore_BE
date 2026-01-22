package com.back.product.app;

import com.back.product.adapter.out.*;
import com.back.product.domain.*;
import com.back.product.dto.ProductDto;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductInfoCreateRequestDto;
import com.back.product.dto.request.ProductVariantCreateRequestDto;
import com.back.product.dto.request.ProductInfoUpdateRequestDto;
import com.back.product.dto.request.ProductVariantUpdateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import com.back.product.dto.response.ProductResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("ProductFacade 통합 테스트")
class ProductCommandTest {

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
    private OptionValue savedOptionValueWhite;
    private OptionValue savedOptionValue95;
    private OptionValue savedOptionValue100;

    @BeforeEach
    void setUp() {
        // Clean all repositories to ensure a clean state for each test
        productImageRepository.deleteAll();
        productOptionValuesRepository.deleteAll();
        productRepository.deleteAll();
        productInfoRepository.deleteAll();
        optionValueRepository.deleteAll();
        optionGroupRepository.deleteAll();
        brandRepository.deleteAll();
        categoryRepository.deleteAll();

        // Given: Prerequisite data
        savedBrand = brandRepository.save(Brand.builder().name("Test Brand").imageUrl("logo.png").build());
        savedCategory = categoryRepository.save(Category.builder().name("Test Category").imageUrl("img.png").build());

        OptionGroup savedOptionGroupColor = optionGroupRepository.save(OptionGroup.builder().name("Color").build());
        OptionGroup savedOptionGroupSize = optionGroupRepository.save(OptionGroup.builder().name("Size").build());

        savedOptionValueBlack = optionValueRepository.save(OptionValue.builder().optionGroup(savedOptionGroupColor).value("Black").build());
        savedOptionValueWhite = optionValueRepository.save(OptionValue.builder().optionGroup(savedOptionGroupColor).value("White").build());
        savedOptionValue95 = optionValueRepository.save(OptionValue.builder().optionGroup(savedOptionGroupSize).value("95").build());
        savedOptionValue100 = optionValueRepository.save(OptionValue.builder().optionGroup(savedOptionGroupSize).value("100").build());
    }

    @Nested
    @DisplayName("createProduct 메서드")
    class CreateProductTest {

        @Test
        @DisplayName("성공: 상품 정보와 여러 variant를 포함하는 완전한 상품을 생성한다")
        void createProduct_Success() {
            // given
            ProductInfoCreateRequestDto productInfoRequest = ProductInfoCreateRequestDto.builder()
                    .brandId(savedBrand.getId())
                    .categoryId(savedCategory.getId())
                    .name("New Awesome Product")
                    .code("NAP-001")
                    .releasePrice(BigDecimal.valueOf(150000))
                    .releasedDate(LocalDateTime.now())
                    .build();

            ProductVariantCreateRequestDto variant1 = new ProductVariantCreateRequestDto(
                    List.of(savedOptionValueBlack.getId(), savedOptionValue95.getId()),
                    50L,
                    List.of("image_black_95.jpg")
            );

            ProductVariantCreateRequestDto variant2 = new ProductVariantCreateRequestDto(
                    List.of(savedOptionValueWhite.getId(), savedOptionValue100.getId()),
                    100L,
                    List.of("image_white_100_1.jpg", "image_white_100_2.jpg")
            );

            ProductCreateRequestDto request = new ProductCreateRequestDto(productInfoRequest, List.of(variant1, variant2));

            // when
            ProductResponseDto response = productFacade.createProduct(request);

            // then
            // 1. Response DTO Verification
            assertThat(response).isNotNull();
            assertThat(response.productInfo().code()).isEqualTo("NAP-001");
            assertThat(response.products()).hasSize(2);
            assertThat(response.products().stream().mapToLong(ProductDto::inventory).sum()).isEqualTo(150L);

            // 2. Database Verification
            assertThat(productInfoRepository.count()).isEqualTo(1);
            assertThat(productRepository.count()).isEqualTo(2);
            assertThat(productOptionValuesRepository.count()).isEqualTo(4); // 2 options per variant
            assertThat(productImageRepository.count()).isEqualTo(3); // 1 + 2 images

            // 3. Verify association
            Long savedProductInfoId = response.productInfo().productInfoId();
            List<Product> savedProducts = productRepository.findAll();
            assertThat(savedProducts.get(0).getProductInfo().getId()).isEqualTo(savedProductInfoId);
            assertThat(savedProducts.get(1).getProductInfo().getId()).isEqualTo(savedProductInfoId);
        }
    }

    @Nested
    @DisplayName("updateProduct 메서드")
    class UpdateProductTest {
        private ProductInfo initialProductInfo;
        private Product variantToUpdate;
        private Product variantToDelete;

        @BeforeEach
        void setUp() {
            // given: 먼저 상품과 2개의 variant를 생성
            initialProductInfo = productInfoRepository.save(ProductInfo.builder()
                    .brand(savedBrand)
                    .category(savedCategory)
                    .name("Initial Product")
                    .productCode("IP-001")
                    .releasePrice(BigDecimal.valueOf(100000))
                    .releasedDate(LocalDateTime.now())
                    .build());

            variantToUpdate = productRepository.save(Product.builder().productInfo(initialProductInfo).inventory(10L).build());
            productOptionValuesRepository.saveAll(List.of(
                    ProductOptionValues.builder().product(variantToUpdate).optionValue(savedOptionValueBlack).build(),
                    ProductOptionValues.builder().product(variantToUpdate).optionValue(savedOptionValue95).build()
            ));

            variantToDelete = productRepository.save(Product.builder().productInfo(initialProductInfo).inventory(20L).build());
            productOptionValuesRepository.saveAll(List.of(
                    ProductOptionValues.builder().product(variantToDelete).optionValue(savedOptionValueWhite).build(),
                    ProductOptionValues.builder().product(variantToDelete).optionValue(savedOptionValue100).build()
            ));
        }

        @Test
        @DisplayName("성공: 상품 정보와 variant들을 수정, 추가, 삭제한다")
        void updateProduct_Success() {
            // given
            // 1. 상품 정보 수정
            ProductInfoUpdateRequestDto productInfoUpdate = ProductInfoUpdateRequestDto.builder()
                    .brandId(savedBrand.getId())
                    .categoryId(savedCategory.getId())
                    .name("Updated Awesome Product")
                    .code("UAP-001")
                    .releasePrice(BigDecimal.valueOf(120000))
                    .releasedDate(LocalDateTime.now())
                    .build();

            // 2. Variant 수정 (variantToUpdate)
            ProductVariantUpdateRequestDto variantUpdate = new ProductVariantUpdateRequestDto(
                    variantToUpdate.getId(),
                    List.of(savedOptionValueBlack.getId(), savedOptionValue100.getId()), // 옵션 변경
                    5L, // 재고 변경
                    List.of("updated_image.jpg")
            );

            // 3. Variant 추가
            ProductVariantUpdateRequestDto variantCreate = new ProductVariantUpdateRequestDto(
                    null,
                    List.of(savedOptionValueWhite.getId(), savedOptionValue95.getId()),
                    30L,
                    List.of("new_image.jpg")
            );

            // variantToDelete는 요청에 포함하지 않아 삭제될 것
            ProductUpdateRequestDto request = new ProductUpdateRequestDto(productInfoUpdate, List.of(variantUpdate, variantCreate));

            // when
            ProductResponseDto response = productFacade.updateProduct(initialProductInfo.getId(), request);

            // then
            // 1. Response DTO 검증
            assertThat(response.productInfo().code()).isEqualTo("UAP-001");
            assertThat(response.productInfo().releasePrice()).isEqualTo(BigDecimal.valueOf(120000));
            assertThat(response.products()).hasSize(2); // 1개 수정, 1개 추가

            // 2. DB 검증
            assertThat(productInfoRepository.findById(initialProductInfo.getId()).get().getProductCode()).isEqualTo("UAP-001");
            assertThat(productRepository.count()).isEqualTo(2);
            assertThat(productRepository.findById(variantToUpdate.getId()).get().getInventory()).isEqualTo(5L);
            assertThat(productRepository.existsById(variantToDelete.getId())).isFalse(); // soft-delete
        }
    }

    @Nested
    @DisplayName("deleteProduct 메서드")
    class DeleteProductTest {

        @Test
        @DisplayName("성공: 상품 정보와 하위 variant들을 모두 삭제(soft-delete)한다")
        void deleteProduct_Success() {
            // given: 상품과 2개의 variant를 생성
            ProductInfo productInfo = productInfoRepository.save(ProductInfo.builder()
                    .brand(savedBrand).category(savedCategory).name("To Be Deleted").productCode("TBD-001")
                    .releasePrice(BigDecimal.valueOf(100000)).releasedDate(LocalDateTime.now()).build());

            Product variant1 = productRepository.save(Product.builder().productInfo(productInfo).inventory(10L).build());
            Product variant2 = productRepository.save(Product.builder().productInfo(productInfo).inventory(20L).build());
            productImageRepository.save(ProductImage.builder().product(variant1).imageUrl("img.jpg").build());
            productOptionValuesRepository.save(ProductOptionValues.builder().product(variant1).optionValue(savedOptionValueBlack).build());

            assertThat(productInfoRepository.count()).isEqualTo(1);
            assertThat(productRepository.count()).isEqualTo(2);
            assertThat(productImageRepository.count()).isEqualTo(1);
            assertThat(productOptionValuesRepository.count()).isEqualTo(1);

            // when
            productFacade.deleteProduct(productInfo.getId());

            // then
            // @SQLDelete 와 @SQLRestriction 에 의해 count, findById 등이 모두 정상적으로 동작해야 함
            assertThat(productInfoRepository.count()).isZero();
            assertThat(productRepository.count()).isZero();
            assertThat(productImageRepository.count()).isZero();
            assertThat(productOptionValuesRepository.count()).isZero();
        }
    }
}
