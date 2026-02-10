package com.back.product.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.app.facade.ProductFacade;
import com.back.product.app.usecase.command.*;
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

    @Mock
    private ProductUseCase productUseCase;

    @Mock
    private OptionUseCase optionUseCase;

    @Nested
    @DisplayName("deleteOptionGroup 메서드")
    class DeleteOptionGroupTest {

        @Test
        @DisplayName("성공: 옵션 그룹이 사용 중이지 않을 때, 삭제 로직을 정상적으로 호출한다")
        void deleteOptionGroup_success_whenNotInUse() {
            // given
            Long optionGroupId = 1L;

            // 1. productUseCase가 "사용 중이 아님(false)"을 반환하도록 설정
            given(productUseCase.isOptionGroupInUse(anyLong())).willReturn(false);

            // 2. optionUseCase.deleteOptions는 void를 반환하므로, 호출되었는지만 검증하면 됨
            // doNothing().when(optionUseCase).deleteOptions(anyLong()); // 이 코드는 선택 사항

            // when & then
            // 예외가 발생하지 않는 것을 검증
            assertDoesNotThrow(() -> productFacade.deleteOptionGroup(optionGroupId));

            // verify
            // 1. 사용 여부 확인 메서드가 호출되었는지 검증
            verify(productUseCase, times(1)).isOptionGroupInUse(eq(optionGroupId));
            // 2. 삭제 메서드가 호출되었는지 검증
            verify(optionUseCase, times(1)).deleteOptions(eq(optionGroupId));
        }

        @Test
        @DisplayName("실패: 옵션 그룹이 사용 중일 때, CustomException을 발생시킨다")
        void deleteOptionGroup_fail_whenInUse() {
            // given
            Long optionGroupId = 1L;

            // 1. productUseCase가 "사용 중(true)"임을 반환하도록 설정
            given(productUseCase.isOptionGroupInUse(anyLong())).willReturn(true);

            // when & then
            // 2. CustomException이 발생하는지 검증
            CustomException exception = assertThrows(CustomException.class, () ->
                    productFacade.deleteOptionGroup(optionGroupId)
            );

            // 3. 발생한 예외의 코드가 올바른지 확인
            assertEquals(FailureCode.OPTION_GROUP_IN_USE, exception.getFailureCode());

            // verify
            // 1. 사용 여부 확인 메서드는 호출되었는지 검증
            verify(productUseCase, times(1)).isOptionGroupInUse(eq(optionGroupId));
            // 2. 옵션이 사용 중이므로, 삭제 메서드는 호출되지 않았는지 검증
            verify(optionUseCase, never()).deleteOptions(anyLong());
        }
    }

    @Nested
    @DisplayName("deleteOptionValue 메서드")
    class DeleteOptionValueTest {

        @Test
        @DisplayName("성공: 옵션 값이 사용 중이지 않을 때, 삭제 로직을 정상적으로 호출한다")
        void deleteOptionValue_success_whenNotInUse() {
            // given
            Long optionValueId = 1L;
            given(productUseCase.isOptionValueInUse(anyLong())).willReturn(false);

            // when & then
            assertDoesNotThrow(() -> productFacade.deleteOptionValue(optionValueId));

            // verify
            verify(productUseCase, times(1)).isOptionValueInUse(eq(optionValueId));
            verify(optionUseCase, times(1)).deleteOption(eq(optionValueId));
        }

        @Test
        @DisplayName("실패: 옵션 값이 사용 중일 때, CustomException을 발생시킨다")
        void deleteOptionValue_fail_whenInUse() {
            // given
            Long optionValueId = 1L;
            given(productUseCase.isOptionValueInUse(anyLong())).willReturn(true);

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    productFacade.deleteOptionValue(optionValueId)
            );

            assertEquals(FailureCode.OPTION_VALUE_IN_USE, exception.getFailureCode());

            // verify
            verify(productUseCase, times(1)).isOptionValueInUse(eq(optionValueId));
            verify(optionUseCase, never()).deleteOption(anyLong());
        }
    }

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
