package com.back.product.app.usecase;

import com.back.common.exception.CustomException;
import com.back.product.adapter.out.BrandRepository;
import com.back.product.domain.Brand;
import com.back.product.dto.BrandDto;
import com.back.product.dto.request.BrandCreateRequestDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("BrandUseCase 통합 테스트")
class BrandUseCaseTest {

    @Autowired
    private BrandUseCase brandUseCase;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeEach
    void setUp() {
        brandRepository.deleteAll();
    }

    @Nested
    @DisplayName("getBrands 메서드")
    class GetBrandsTest {

        @Test
        @DisplayName("DB에 저장된 모든 브랜드를 조회하여 DTO 리스트로 반환한다")
        void getBrands_Success() {
            // given
            brandRepository.saveAll(List.of(
                    Brand.builder().name("Nike").imageUrl("https://example.com/logo1.png").build(),
                    Brand.builder().name("Adidas").imageUrl("https://example.com/logo2.png").build()
            ));

            // when
            List<BrandDto> result = brandUseCase.getBrands();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BrandDto::name).containsExactlyInAnyOrder("Nike", "Adidas");
        }
    }

    @Nested
    @DisplayName("createBrand 메서드")
    class CreateBrandTest {

        @Test
        @DisplayName("새로운 브랜드를 DB에 저장하고 생성된 정보를 반환한다")
        void createBrand_Success() {
            // given
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto("New Balance", "https://example.com/logo.png");

            // when
            BrandDto result = brandUseCase.createBrand(requestDto);

            // then
            assertThat(result.name()).isEqualTo("New Balance");
            assertThat(result.logoUrl()).isEqualTo("https://example.com/logo.png");

            // DB에 실제 저장되었는지, ID는 생성되었는지 재검증
            List<Brand> allBrands = brandRepository.findAll();
            assertThat(allBrands).hasSize(1);
            assertThat(allBrands.get(0).getId()).isNotNull();
            assertThat(allBrands.get(0).getName()).isEqualTo("New Balance");
        }

        @Test
        @DisplayName("이미 존재하는 브랜드 이름이면 예외를 발생시킨다")
        void createBrand_Fail_DuplicateName() {
            // given
            // DB에 미리 같은 이름의 브랜드를 저장
            brandRepository.save(Brand.builder().name("Existing Brand").imageUrl("https://example.com/logo.png").build());
            BrandCreateRequestDto requestDto = new BrandCreateRequestDto("Existing Brand", "https://example.com/logo.png");

            // when & then
            assertThatThrownBy(() -> brandUseCase.createBrand(requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 존재하는 브랜드 이름입니다.");
        }
    }
}