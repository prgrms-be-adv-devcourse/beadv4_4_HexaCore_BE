package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.BrandRepository;
import com.back.product.app.usecase.command.BrandUseCase;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.Brand;
import com.back.product.dto.command.BrandDataCommand;
import com.back.product.dto.model.BrandDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.mapper.BrandMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrandUseCase 단위 테스트")
class BrandUseCaseTest {

    @InjectMocks
    private BrandUseCase brandUseCase;

    @Mock
    private BrandMapper brandMapper;

    @Mock
    private ProductSupport productSupport;

    @Mock
    private BrandRepository brandRepository;

    @Nested
    @DisplayName("getBrands 메서드")
    class GetBrandsTest {

        @Test
        @DisplayName("모든 브랜드를 조회하여 DTO 리스트로 반환한다")
        void getBrands_Success() {
            // given
            Brand brand1 = Brand.builder().id(1L).name("Nike").imageUrl("https://example.com/logo1.png").build();
            Brand brand2 = Brand.builder().id(2L).name("Adidas").imageUrl("https://example.com/logo2.png").build();
            List<Brand> allBrands = List.of(brand1, brand2);

            given(productSupport.getAllBrands()).willReturn(allBrands);
            given(brandMapper.toDto(brand1)).willReturn(new BrandDto(1L, "Nike", "https://example.com/logo1.png"));
            given(brandMapper.toDto(brand2)).willReturn(new BrandDto(2L, "Adidas", "https://example.com/logo2.png"));

            // when
            List<BrandDto> result = brandUseCase.getBrands();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BrandDto::name).containsExactlyInAnyOrder("Nike", "Adidas");
            verify(productSupport).getAllBrands();
            verify(brandMapper, times(2)).toDto(any(Brand.class));
        }
    }

    @Nested
    @DisplayName("createBrands 메서드")
    class CreateBrandsTest {

        @Test
        @DisplayName("새로운 브랜드들을 DB에 저장하고 생성된 정보 리스트를 반환한다")
        void createBrands_Success() {
            // given
            BrandDataCommand newBrandDto1 = new BrandDataCommand("New Balance", "https://example.com/logo.png");
            BrandDataCommand newBrandDto2 = new BrandDataCommand("Nike", "https://example.com/logo2.png");
            List<BrandDataCommand> requestDto = List.of(newBrandDto1, newBrandDto2);

            Brand newBrandEntity1 = Brand.builder().name("New Balance").imageUrl("https://example.com/logo.png").build();
            Brand newBrandEntity2 = Brand.builder().name("Nike").imageUrl("https://example.com/logo2.png").build();
            List<Brand> brandsToCreate = List.of(newBrandEntity1, newBrandEntity2);

            Brand savedBrandEntity1 = Brand.builder().id(1L).name("New Balance").imageUrl("https://example.com/logo.png").build();
            Brand savedBrandEntity2 = Brand.builder().id(2L).name("Nike").imageUrl("https://example.com/logo2.png").build();
            List<Brand> savedBrands = List.of(savedBrandEntity1, savedBrandEntity2);

            given(productSupport.getAllBrands()).willReturn(Collections.emptyList());
            given(brandMapper.toEntity(newBrandDto1)).willReturn(newBrandEntity1);
            given(brandMapper.toEntity(newBrandDto2)).willReturn(newBrandEntity2);
            given(brandRepository.saveAll(brandsToCreate)).willReturn(savedBrands);
            given(brandMapper.toDto(savedBrandEntity1)).willReturn(new BrandDto(1L, "New Balance", "https://example.com/logo.png"));
            given(brandMapper.toDto(savedBrandEntity2)).willReturn(new BrandDto(2L, "Nike", "https://example.com/logo2.png"));

            // when
            List<BrandDto> result = brandUseCase.createBrands(requestDto);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(BrandDto::name).containsExactlyInAnyOrder("New Balance", "Nike");
            verify(productSupport).getAllBrands();
            verify(brandRepository).saveAll(brandsToCreate);
            verify(brandMapper, times(2)).toEntity(any(BrandDataCommand.class));
            verify(brandMapper, times(2)).toDto(any(Brand.class));
        }

        @Test
        @DisplayName("이미 존재하는 브랜드 이름은 필터링하고, 새로운 브랜드만 생성한다")
        void createBrands_Should_Filter_DuplicateName() {
            // given
            BrandDataCommand existingBrandDto = new BrandDataCommand("Existing Brand", "https://example.com/logo_exist.png");
            BrandDataCommand newBrandDto = new BrandDataCommand("New Brand", "https://example.com/logo_new.png");
            List<BrandDataCommand> requestDto = List.of(existingBrandDto, newBrandDto);

            Brand existingEntity = Brand.builder().id(1L).name("Existing Brand").imageUrl("https://example.com/logo_exist.png").build();
            Brand newEntity = Brand.builder().name("New Brand").imageUrl("https://example.com/logo_new.png").build();
            Brand savedNewEntity = Brand.builder().id(2L).name("New Brand").imageUrl("https://example.com/logo_new.png").build();

            given(productSupport.getAllBrands()).willReturn(List.of(existingEntity));
            given(brandMapper.toEntity(newBrandDto)).willReturn(newEntity);
            given(brandRepository.saveAll(List.of(newEntity))).willReturn(List.of(savedNewEntity));
            given(brandMapper.toDto(savedNewEntity)).willReturn(new BrandDto(2L, "New Brand", "https://example.com/logo_new.png"));

            // when
            List<BrandDto> result = brandUseCase.createBrands(requestDto);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("New Brand");

            verify(productSupport).getAllBrands();
            verify(brandRepository).saveAll(List.of(newEntity));
            verify(brandMapper, times(1)).toEntity(any(BrandDataCommand.class));
            verify(brandMapper, times(1)).toDto(any(Brand.class));
        }
    }

    @Nested
    @DisplayName("modifyBrand 메서드")
    class ModifyBrandTest {

        @Spy
        private Brand brandToModify = Brand.builder().id(1L).name("Original Name").imageUrl("original.png").build();

        @Test
        @DisplayName("브랜드 수정을 성공한다")
        void modifyBrand_Success() {
            // given
            final Long BRAND_ID = 1L;
            BrandDataCommand requestDto = BrandDataCommand.builder()
                    .name("Modified Name")
                    .imageUrl("modified.png")
                    .build();

            given(productSupport.findBrandById(BRAND_ID)).willReturn(Optional.of(brandToModify));
            given(productSupport.getAllBrands()).willReturn(List.of(brandToModify));
            given(brandMapper.toDto(brandToModify)).willReturn(new BrandDto(BRAND_ID, "Modified Name", "modified.png"));

            // when
            BrandDto result = brandUseCase.modifyBrand(BRAND_ID, requestDto);

            // then
            assertThat(result.name()).isEqualTo("Modified Name");
            assertThat(result.imageUrl()).isEqualTo("modified.png");
            verify(productSupport).findBrandById(BRAND_ID);
            verify(productSupport).getAllBrands();
            verify(brandToModify).modifyName("Modified Name");
            verify(brandToModify).modifyImageUrl("modified.png");
            verify(brandMapper).toDto(brandToModify);
        }

        @Test
        @DisplayName("존재하지 않는 브랜드를 수정하려고 하면 예외를 발생시킨다")
        void modifyBrand_Fail_BrandNotFound() {
            // given
            final Long NON_EXISTENT_ID = 99L;
            BrandDataCommand requestDto = BrandDataCommand.builder().name("any").imageUrl("any.png").build();

            given(productSupport.findBrandById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> brandUseCase.modifyBrand(NON_EXISTENT_ID, requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("failureCode", FailureCode.BRAND_NOT_FOUND);

            verify(productSupport).findBrandById(NON_EXISTENT_ID);
            verify(productSupport, never()).getAllBrands();
        }

        @Test
        @DisplayName("다른 브랜드와 이름이 중복되면 예외를 발생시킨다")
        void modifyBrand_Fail_DuplicateName() {
            // given
            final Long BRAND_ID = 1L;
            Brand existingBrandWithSameName = Brand.builder().id(2L).name("Existing Name").imageUrl("existing.png").build();
            BrandDataCommand command = BrandDataCommand.builder().name("Existing Name").imageUrl("modified.png").build();

            given(productSupport.findBrandById(BRAND_ID)).willReturn(Optional.of(brandToModify));
            given(productSupport.getAllBrands()).willReturn(List.of(brandToModify, existingBrandWithSameName));

            // when & then
            assertThatThrownBy(() -> brandUseCase.modifyBrand(BRAND_ID, command))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("failureCode", FailureCode.BRAND_NAME_DUPLICATE);

            verify(productSupport).findBrandById(BRAND_ID);
            verify(productSupport).getAllBrands();
            verify(brandToModify, never()).modifyName(anyString());
        }
    }
}