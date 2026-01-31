package com.back.product.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductFacade 단위 테스트")
class ProductFacadeTest {

    @InjectMocks
    private ProductFacade productFacade;

    @Mock
    private ProductInfoUseCase productInfoUseCase;

    @Mock
    private BrandUseCase brandUseCase;

    @Mock
    private CategoryUseCase categoryUseCase;

    @Nested
    @DisplayName("deleteBrand 메소드")
    class DeleteBrandTest {

        private final Long BRAND_ID = 1L;

        @Test
        @DisplayName("브랜드가 사용 중이지 않으면 브랜드 삭제를 성공한다")
        void deleteBrand_Success() {
            // given
            // isBrandInUse가 false를 반환하도록 설정
            given(productInfoUseCase.isBrandInUse(BRAND_ID)).willReturn(false);
            // deleteBrand는 void이므로 아무것도 하지 않도록 설정
            doNothing().when(brandUseCase).deleteBrand(BRAND_ID);

            // when
            productFacade.deleteBrand(BRAND_ID);

            // then
            // 각 UseCase의 메소드가 올바른 인자로 호출되었는지 검증
            verify(productInfoUseCase).isBrandInUse(BRAND_ID);
            verify(brandUseCase).deleteBrand(BRAND_ID);
        }

        @Test
        @DisplayName("브랜드가 사용 중이면 BRAND_IN_USE 예외를 발생시킨다")
        void deleteBrand_Fail_BrandInUse() {
            // given
            // isBrandInUse가 true를 반환하도록 설정
            given(productInfoUseCase.isBrandInUse(BRAND_ID)).willReturn(true);

            // when & then
            // 예외 발생을 검증
            assertThatThrownBy(() -> productFacade.deleteBrand(BRAND_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("failureCode", FailureCode.BRAND_IN_USE);

            // isBrandInUse는 호출되었는지 검증
            verify(productInfoUseCase).isBrandInUse(BRAND_ID);
            // brandUseCase.deleteBrand는 호출되지 않았는지 검증
            verify(brandUseCase, never()).deleteBrand(BRAND_ID);
        }
    }

    @Nested
    @DisplayName("deleteCategory 메소드")
    class DeleteCategoryTest {

        private final Long CATEGORY_ID = 1L;

        @Test
        @DisplayName("카테고리가 사용 중이지 않으면 카테고리 삭제를 성공한다")
        void deleteCategory_Success() {
            // given
            // isCategoryInUse가 false를 반환하도록 설정
            given(productInfoUseCase.isCategoryInUse(CATEGORY_ID)).willReturn(false);
            // deleteCategory는 void이므로 아무것도 하지 않도록 설정
            doNothing().when(categoryUseCase).deleteCategory(CATEGORY_ID);

            // when
            productFacade.deleteCategory(CATEGORY_ID);

            // then
            // 각 UseCase의 메소드가 올바른 인자로 호출되었는지 검증
            verify(productInfoUseCase).isCategoryInUse(CATEGORY_ID);
            verify(categoryUseCase).deleteCategory(CATEGORY_ID);
        }

        @Test
        @DisplayName("카테고리가 사용 중이면 CATEGORY_IN_USE 예외를 발생시킨다")
        void deleteCategory_Fail_CategoryInUse() {
            // given
            // isCategoryInUse가 true를 반환하도록 설정
            given(productInfoUseCase.isCategoryInUse(CATEGORY_ID)).willReturn(true);

            // when & then
            // 예외 발생을 검증
            assertThatThrownBy(() -> productFacade.deleteCategory(CATEGORY_ID))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("failureCode", FailureCode.CATEGORY_IN_USE);

            // isCategoryInUse는 호출되었는지 검증
            verify(productInfoUseCase).isCategoryInUse(CATEGORY_ID);
            // categoryUseCase.deleteCategory는 호출되지 않았는지 검증
            verify(categoryUseCase, never()).deleteCategory(CATEGORY_ID);
        }
    }
}
