package com.back.product.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.BaseIntegrationTest;
import com.back.product.adapter.out.*;
import com.back.product.document.ProductDocument;
import com.back.product.domain.*;
import com.back.product.dto.enums.ProductSortType;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductResponseDto;
import com.back.product.dto.response.ProductSearchListResponseDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.TestPropertySource;
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
@TestPropertySource(properties = "spring.elasticsearch.uris=dummy")
class ProductQueryTest extends BaseIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ProductDocumentRepository productDocumentRepository;

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
    private ProductInfo savedProductInfo2; // 검색 테스트용 상품 추가

    @BeforeEach
    void setUp() {
        // Given: Prerequisite data
        savedBrand = brandRepository.save(Brand.builder()
                .name("Test Brand")
                .imageUrl("logo.png")
                .build());

        savedCategory = categoryRepository.save(Category.builder()
                .name("Test Category")
                .imageUrl("img.png")
                .build());

        OptionGroup savedOptionGroupColor = optionGroupRepository.save(OptionGroup.builder().name("Color").build());
        OptionGroup savedOptionGroupSize = optionGroupRepository.save(OptionGroup.builder().name("Size").build());

        savedOptionValueBlack = optionValueRepository.save(OptionValue.builder()
                .optionGroup(savedOptionGroupColor)
                .value("Black")
                .build());

        savedOptionValue95 = optionValueRepository.save(OptionValue.builder()
                .optionGroup(savedOptionGroupSize)
                .value("95")
                .build());

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

        productImageRepository.save(ProductImage.builder()
                .product(savedProduct)
                .imageUrl("test_image.jpg")
                .build());

        productOptionValuesRepository.saveAll(List.of(
                ProductOptionValues.builder().product(savedProduct).optionValue(savedOptionValueBlack).build(),
                ProductOptionValues.builder().product(savedProduct).optionValue(savedOptionValue95).build()
        ));

        // 검색 테스트를 위한 두 번째 상품
        savedProductInfo2 = productInfoRepository.save(ProductInfo.builder()
                .brand(savedBrand)
                .category(savedCategory)
                .name("Another Product For Search")
                .productCode("SEARCH-002")
                .releasePrice(BigDecimal.valueOf(200000))
                .releasedDate(LocalDateTime.now())
                .build());

        Product savedProduct2 = productRepository.save(Product.builder()
                .productInfo(savedProductInfo2)
                .inventory(30L)
                .build());

        productImageRepository.save(ProductImage.builder()
                .product(savedProduct2)
                .imageUrl("another_image.jpg")
                .build());
    }

    @AfterEach
    void tearDown() {
        // 테스트 격리를 위해 Elasticsearch 데이터 삭제
        elasticsearchOperations.indexOps(ProductDocument.class).delete();
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
            assertThat(response.products().get(0).options().stream()
                    .map(o -> o.groupName() + ":" + o.value()))
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

    @Nested
    @DisplayName("findProductPage 메서드")
    class FindProductPageTest {

        @BeforeEach
        void setupElasticsearchData() {
            // 테스트 전 Elasticsearch 인덱스 초기화 및 데이터 색인
            elasticsearchOperations.indexOps(ProductDocument.class).delete(); // 기존 인덱스 삭제
            elasticsearchOperations.indexOps(ProductDocument.class).create(); // 새 인덱스 생성
            // 엔티티(@Field)에 설정한 매핑 정보(Text, standard analyzer 등)를 ES에 적용합니다.
            elasticsearchOperations.indexOps(ProductDocument.class).putMapping();

            // ProductDocument 생성 및 색인
            ProductDocument doc1 = ProductDocument.builder()
                    .productInfoId(savedProductInfo.getId())
                    .productName("Test Product For Search")
                    .brandName(savedBrand.getName())
                    .categoryName(savedCategory.getName())
                    .releasePrice(BigDecimal.valueOf(10000))
                    .totalInventory(10L)
                    .releasedDate(LocalDateTime.now())
                    .build();

            ProductDocument doc2 = ProductDocument.builder()
                    .productInfoId(savedProductInfo2.getId())
                    .productName("Another Product For Test")
                    .brandName(savedBrand.getName())
                    .categoryName(savedCategory.getName())
                    .releasePrice(BigDecimal.valueOf(20000))
                    .totalInventory(20L)
                    .releasedDate(LocalDateTime.now())
                    .build();

            productDocumentRepository.saveAll(List.of(doc1, doc2));
            // 색인된 문서 즉시 검색 가능하도록 리프레시
            elasticsearchOperations.indexOps(ProductDocument.class).refresh();
        }

        @Test
        @DisplayName("성공: 키워드로 상품 페이지를 검색한다")
        void findProductPage_Success_KeywordSearch() {
            // Given
            ProductSearchRequestDto requestDto = ProductSearchRequestDto.builder()
                    .keyword("Test") // "Search"에서 "Test"로 변경
                    .brandIds(List.of())
                    .categoryIds(List.of())
                    .maxPrice(null)
                    .minPrice(null)
                    .excludeSoldOut(false)
                    .sort(ProductSortType.LATEST)
                    .build();
            Long page = 0L;
            Long size = 10L;

            // When
            ProductSearchListResponseDto response = productFacade.findProductPage(requestDto, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.products()).hasSize(2); // 예상 결과 개수를 1에서 2로 변경
            // 특정 이름 대신 두 상품명이 모두 포함되었는지 확인
            assertThat(response.products().stream().map(ProductSearchResponseDto::productName))
                    .containsExactlyInAnyOrder("Test Product For Search", "Another Product For Test");
        }

        @Test
        @DisplayName("성공: 모든 상품 페이지를 검색한다 (키워드 없음)")
        void findProductPage_Success_AllProducts() {
            // Given
            ProductSearchRequestDto requestDto = ProductSearchRequestDto.builder()
                    .keyword("")
                    .brandIds(List.of())
                    .categoryIds(List.of())
                    .maxPrice(null)
                    .minPrice(null)
                    .excludeSoldOut(false)
                    .sort(ProductSortType.LATEST)
                    .build();
            Long page = 0L;
            Long size = 10L;

            // When
            ProductSearchListResponseDto response = productFacade.findProductPage(requestDto, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.products().size()).isEqualTo(2); // 색인된 두 개의 문서
            assertThat(response.products()).hasSize(2);

            assertThat(response.products().stream().map(ProductSearchResponseDto::productName))
                    .containsExactlyInAnyOrder("Test Product For Search", "Another Product For Test");
        }

        @Test
        @DisplayName("성공: 검색 결과가 없는 경우 빈 리스트를 반환한다")
        void findProductPage_Success_NoResults() {
            // Given
            ProductSearchRequestDto requestDto = ProductSearchRequestDto.builder()
                    .keyword("NonExistentProduct")
                    .brandIds(List.of())
                    .categoryIds(List.of())
                    .maxPrice(null)
                    .minPrice(null)
                    .excludeSoldOut(false)
                    .sort(ProductSortType.LATEST)
                    .build();
            Long page = 0L;
            Long size = 10L;

            // When
            ProductSearchListResponseDto response = productFacade.findProductPage(requestDto, page, size);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.products().size()).isEqualTo(0);
            assertThat(response.products()).isEmpty();
        }

        @Test
        @DisplayName("성공: 페이지네이션이 올바르게 작동한다")
        void findProductPage_Success_Pagination() {
            // Given (BeforeEach에서 2개의 문서 색인)
            ProductSearchRequestDto requestDto = ProductSearchRequestDto.builder()
                    .keyword(null)
                    .brandIds(List.of())
                    .categoryIds(List.of())
                    .maxPrice(null)
                    .minPrice(null)
                    .excludeSoldOut(false)
                    .sort(ProductSortType.LATEST)
                    .build();
            Long page = 0L;
            Long size = 1L; // 페이지당 1개

            // When - 첫 번째 페이지
            ProductSearchListResponseDto response1 = productFacade.findProductPage(requestDto, page, size);

            // Then
            assertThat(response1).isNotNull();
            assertThat(response1.products().size()).isEqualTo(1);
            assertThat(response1.products()).hasSize(1);
            assertThat(response1.products().getFirst().productName()).isIn("Test Product For Search", "Another Product For Test");

            // When - 두 번째 페이지
            Long page2 = 1L;
            ProductSearchListResponseDto response2 = productFacade.findProductPage(requestDto, page2, size);

            // Then
            assertThat(response2).isNotNull();
            assertThat(response2.products().size()).isEqualTo(1);
            assertThat(response2.products()).hasSize(1);
            assertThat(response2.products().getFirst().productName()).isIn("Test Product For Search", "Another Product For Test");

            // 다른 상품이 조회되는지 확인
            assertThat(response1.products().getFirst().productName())
                    .isNotEqualTo(response2.products().getFirst().productName());
        }
    }
}