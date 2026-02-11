package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.CategoryRepository;
import com.back.product.app.usecase.command.CategoryUseCase;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.Category;
import com.back.product.dto.command.CategoryDataCommand;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.mapper.CategoryMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
            CategoryDataCommand newCategoryDto1 = new CategoryDataCommand("Tops", "https://e.com/1.png");
            CategoryDataCommand newCategoryDto2 = new CategoryDataCommand("Bottoms", "https://e.com/2.png");
            List<CategoryDataCommand> requestDto = List.of(newCategoryDto1, newCategoryDto2);

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
            verify(categoryMapper, times(2)).toEntity(any(CategoryDataCommand.class));
            verify(categoryMapper, times(2)).toDto(any(Category.class));
        }

        @Test
        @DisplayName("이미 존재하는 카테고리 이름은 필터링하고, 새로운 카테고리만 생성한다")
        void createCategories_Should_Filter_DuplicateName() {
            // given
            CategoryDataCommand existingDto = new CategoryDataCommand("Existed", "https://e.com/e.png");
            CategoryDataCommand newDto = new CategoryDataCommand("New", "https://e.com/n.png");
            List<CategoryDataCommand> requestDto = List.of(existingDto, newDto);

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
            verify(categoryMapper, times(1)).toEntity(any(CategoryDataCommand.class));
            verify(categoryMapper, times(1)).toDto(any(Category.class));
        }
    }

    @Nested
    @DisplayName("modifyCategory 메서드")
    class ModifyCategoryTest {

        @Spy
        private Category categoryToModify = Category.builder().id(1L).name("Original Name").imageUrl("original.png").build();

        @Test
        @DisplayName("카테고리 수정을 성공한다")
        void modifyCategory_Success() {
            // given
            final Long CATEGORY_ID = 1L;
            CategoryDataCommand requestDto = CategoryDataCommand.builder()
                    .name("Modified")
                    .imageUrl("modified.png")
                    .build();

            given(productSupport.findCategoryById(CATEGORY_ID)).willReturn(Optional.of(categoryToModify));
            given(productSupport.getAllCategories()).willReturn(List.of(categoryToModify));
            given(categoryMapper.toDto(categoryToModify)).willReturn(new CategoryDto(CATEGORY_ID, "Modified", "modified.png"));

            // when
            CategoryDto result = categoryUseCase.modifyCategory(CATEGORY_ID, requestDto);

            // then
            assertThat(result.name()).isEqualTo("Modified");
            assertThat(result.imageUrl()).isEqualTo("modified.png");
            verify(productSupport).findCategoryById(CATEGORY_ID);
            verify(productSupport).getAllCategories();
            verify(categoryToModify).modifyName("Modified");
            verify(categoryToModify).modifyImageUrl("modified.png");
            verify(categoryMapper).toDto(categoryToModify);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리를 수정하려고 하면 예외를 발생시킨다")
        void modifyCategory_Fail_CategoryNotFound() {
            // given
            final Long NON_EXISTENT_ID = 99L;
            CategoryDataCommand requestDto = CategoryDataCommand.builder().name("any").imageUrl("any.png").build();

            given(productSupport.findCategoryById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryUseCase.modifyCategory(NON_EXISTENT_ID, requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("failureCode", FailureCode.CATEGORY_NOT_FOUND);

            verify(productSupport).findCategoryById(NON_EXISTENT_ID);
            verify(productSupport, never()).getAllCategories();
        }

        @Test
        @DisplayName("다른 카테고리와 이름이 중복되면 예외를 발생시킨다")
        void modifyCategory_Fail_DuplicateName() {
            // given
            final Long CATEGORY_ID = 1L;
            Category existingCategoryWithSameName = Category.builder().id(2L).name("Existing").imageUrl("existing.png").build();
            CategoryDataCommand requestDto = CategoryDataCommand.builder().name("Existing").imageUrl("modified.png").build();

            given(productSupport.findCategoryById(CATEGORY_ID)).willReturn(Optional.of(categoryToModify));
            given(productSupport.getAllCategories()).willReturn(List.of(categoryToModify, existingCategoryWithSameName));

            // when & then
            assertThatThrownBy(() -> categoryUseCase.modifyCategory(CATEGORY_ID, requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("failureCode", FailureCode.CATEGORY_NAME_DUPLICATE);

            verify(productSupport).findCategoryById(CATEGORY_ID);
            verify(productSupport).getAllCategories();
            verify(categoryToModify, never()).modifyName(anyString());
        }
    }
}
