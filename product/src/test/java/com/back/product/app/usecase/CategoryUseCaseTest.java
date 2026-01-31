package com.back.product.app.usecase;

import com.back.product.adapter.out.CategoryRepository;
import com.back.product.domain.Category;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import com.back.product.mapper.CategoryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryUseCase 단위 테스트")
class CategoryUseCaseTest {

    @InjectMocks
    private CategoryUseCase categoryUseCase;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductSupport productSupport;

    @Mock
    private CategoryRepository categoryRepository;


    @Nested
    @DisplayName("getCategories 메서드")
    class GetCategoriesTest {

        @Test
        @DisplayName("모든 카테고리를 조회하여 DTO 리스트로 반환한다")
        void getCategories_Success() {
            // given
            Category category1 = Category.builder().id(1L).name("Tops").imageUrl("https://example.com/image.png").build();
            Category category2 = Category.builder().id(2L).name("Bottoms").imageUrl("https://example.com/image.png").build();
            List<Category> allCategories = List.of(category1, category2);

            given(productSupport.getAllCategories()).willReturn(allCategories);
            given(categoryMapper.toDto(category1)).willReturn(new CategoryDto(1L, "Tops", "https://example.com/image.png"));
            given(categoryMapper.toDto(category2)).willReturn(new CategoryDto(2L, "Bottoms", "https://example.com/image.png"));

            // when
            List<CategoryDto> result = categoryUseCase.getCategories();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(CategoryDto::name).containsExactlyInAnyOrder("Tops", "Bottoms");
            verify(productSupport).getAllCategories();
            verify(categoryMapper, times(2)).toDto(any(Category.class));
        }
    }

    @Nested
    @DisplayName("createCategories 메서드")
    class CreateCategoriesTest {

        @Test
        @DisplayName("새로운 카테고리들을 DB에 저장하고 생성된 정보 리스트를 반환한다")
        void createCategories_Success() {
            // given
            CategoryCreateRequestDto.CategoryDto newCategoryDto1 = new CategoryCreateRequestDto.CategoryDto("Tops", "https://e.com/1.png");
            CategoryCreateRequestDto.CategoryDto newCategoryDto2 = new CategoryCreateRequestDto.CategoryDto("Bottoms", "https://e.com/2.png");
            CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto(List.of(newCategoryDto1, newCategoryDto2));

            Category newCategoryEntity1 = Category.builder().name("Tops").imageUrl("https://e.com/1.png").build();
            Category newCategoryEntity2 = Category.builder().name("Bottoms").imageUrl("https://e.com/2.png").build();
            List<Category> categoriesToCreate = List.of(newCategoryEntity1, newCategoryEntity2);

            Category savedCategoryEntity1 = Category.builder().id(1L).name("Tops").imageUrl("https://e.com/1.png").build();
            Category savedCategoryEntity2 = Category.builder().id(2L).name("Bottoms").imageUrl("https://e.com/2.png").build();
            List<Category> savedCategories = List.of(savedCategoryEntity1, savedCategoryEntity2);

            given(productSupport.getAllCategories()).willReturn(Collections.emptyList());
            given(categoryMapper.toEntity(newCategoryDto1)).willReturn(newCategoryEntity1);
            given(categoryMapper.toEntity(newCategoryDto2)).willReturn(newCategoryEntity2);
            given(categoryRepository.saveAll(categoriesToCreate)).willReturn(savedCategories);
            given(categoryMapper.toDto(savedCategoryEntity1)).willReturn(new CategoryDto(1L, "Tops", "https://e.com/1.png"));
            given(categoryMapper.toDto(savedCategoryEntity2)).willReturn(new CategoryDto(2L, "Bottoms", "https://e.com/2.png"));

            // when
            List<CategoryDto> result = categoryUseCase.createCategories(requestDto);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(CategoryDto::name).containsExactlyInAnyOrder("Tops", "Bottoms");
            verify(productSupport).getAllCategories();
            verify(categoryRepository).saveAll(categoriesToCreate);
            verify(categoryMapper, times(2)).toEntity(any(CategoryCreateRequestDto.CategoryDto.class));
            verify(categoryMapper, times(2)).toDto(any(Category.class));
        }

        @Test
        @DisplayName("이미 존재하는 카테고리 이름은 필터링하고, 새로운 카테고리만 생성한다")
        void createCategories_Should_Filter_DuplicateName() {
            // given
            CategoryCreateRequestDto.CategoryDto existingDto = new CategoryCreateRequestDto.CategoryDto("Existed", "https://e.com/e.png");
            CategoryCreateRequestDto.CategoryDto newDto = new CategoryCreateRequestDto.CategoryDto("New", "https://e.com/n.png");
            CategoryCreateRequestDto requestDto = new CategoryCreateRequestDto(List.of(existingDto, newDto));

            Category existingEntity = Category.builder().id(1L).name("Existed").imageUrl("https://e.com/e.png").build();
            Category newEntity = Category.builder().name("New").imageUrl("https://e.com/n.png").build();
            Category savedNewEntity = Category.builder().id(2L).name("New").imageUrl("https://e.com/n.png").build();

            given(productSupport.getAllCategories()).willReturn(List.of(existingEntity));
            given(categoryMapper.toEntity(newDto)).willReturn(newEntity);
            given(categoryRepository.saveAll(List.of(newEntity))).willReturn(List.of(savedNewEntity));
            given(categoryMapper.toDto(savedNewEntity)).willReturn(new CategoryDto(2L, "New", "https://e.com/n.png"));

            // when
            List<CategoryDto> result = categoryUseCase.createCategories(requestDto);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("New");

            verify(productSupport).getAllCategories();
            verify(categoryRepository).saveAll(List.of(newEntity));
            verify(categoryMapper, times(1)).toEntity(any(CategoryCreateRequestDto.CategoryDto.class));
            verify(categoryMapper, times(1)).toDto(any(Category.class));
        }
    }
}
