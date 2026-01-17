package com.back.product.app.usecase;

import com.back.common.exception.CustomException;
import com.back.product.adapter.out.CategoryRepository;
import com.back.product.domain.Category;
import com.back.product.dto.CategoryDto;
import com.back.product.dto.request.CategoryCreateRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("CategoryUseCase 통합 테스트")
class CategoryUseCaseTest {

    @Autowired
    private CategoryUseCase categoryUseCase;

    @Autowired
    private CategoryRepository categoryRepository;

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    @Nested
    @DisplayName("getCategories 메서드")
    class GetCategoriesTest {

        @Test
        @DisplayName("DB에 저장된 모든 카테고리를 조회하여 DTO 리스트로 반환한다")
        void getCategories_Success() {
            // given
            categoryRepository.saveAll(List.of(
                    Category.builder().name("Tops").imageUrl("image.png").build(),
                    Category.builder().name("Bottoms").imageUrl("image.png").build()
            ));

            // when
            List<CategoryDto> result = categoryUseCase.getCategories();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(CategoryDto::name).containsExactlyInAnyOrder("Tops", "Bottoms");
        }
    }

    @Nested
    @DisplayName("createCategory 메서드")
    class CreateCategoryTest {

        @Test
        @DisplayName("새로운 카테고리를 DB에 저장하고 생성된 정보를 반환한다")
        void createCategory_Success() {
            // given
            CategoryCreateRequestDto requestDto = CategoryCreateRequestDto.builder().name("Accessories").imageUrl("image.png").build();

            // when
            CategoryDto result = categoryUseCase.createCategory(requestDto);

            // then
            assertThat(result.name()).isEqualTo("Accessories");

            // DB에 실제 저장되었는지, ID는 생성되었는지 재검증
            List<Category> allCategories = categoryRepository.findAll();
            assertThat(allCategories).hasSize(1);
            assertThat(allCategories.getFirst().getId()).isNotNull();
            assertThat(allCategories.getFirst().getName()).isEqualTo("Accessories");
        }

        @Test
        @DisplayName("이미 존재하는 카테고리 이름이면 예외를 발생시킨다")
        void createCategory_Fail_DuplicateName() {
            // given
            // DB에 미리 같은 이름의 카테고리를 저장
            categoryRepository.save(Category.builder().name("Existed").imageUrl("image.png").build());
            CategoryCreateRequestDto requestDto = CategoryCreateRequestDto.builder().name("Existed").imageUrl("image.png").build();

            // when & then
            assertThatThrownBy(() -> categoryUseCase.createCategory(requestDto))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 존재하는 카테고리 이름입니다.");
        }
    }
}
