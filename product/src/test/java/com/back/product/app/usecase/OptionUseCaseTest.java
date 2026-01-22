package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.domain.OptionValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("OptionUseCase 단위 테스트")
class OptionUseCaseTest {

    @InjectMocks
    private OptionUseCase optionUseCase;

    @Mock
    private ProductSupport productSupport;

    @Nested
    @DisplayName("findOptionValuesAsMap 메서드")
    class FindOptionValuesAsMapTest {

        @Test
        @DisplayName("성공: ID 목록으로 OptionValue 맵을 반환한다")
        void findOptionValuesAsMap_Success() {
            // given
            List<Long> ids = List.of(1L, 2L);
            OptionValue value1 = OptionValue.builder().id(1L).value("Black").build();
            OptionValue value2 = OptionValue.builder().id(2L).value("95").build();
            List<OptionValue> values = List.of(value1, value2);

            given(productSupport.getAllOptionValues(ids)).willReturn(values);

            // when
            Map<Long, OptionValue> result = optionUseCase.findOptionValuesAsMap(ids);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(1L).getValue()).isEqualTo("Black");
            assertThat(result.get(2L).getValue()).isEqualTo("95");
        }

        @Test
        @DisplayName("실패: 요청한 ID와 조회된 결과의 개수가 다르면 예외를 발생시킨다")
        void findOptionValuesAsMap_Fail_NotFound() {
            // given
            List<Long> ids = List.of(1L, 2L, 99L); // 99L is not found
            OptionValue value1 = OptionValue.builder().id(1L).value("Black").build();
            OptionValue value2 = OptionValue.builder().id(2L).value("95").build();
            List<OptionValue> foundValues = List.of(value1, value2);

            given(productSupport.getAllOptionValues(ids)).willReturn(foundValues);

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    optionUseCase.findOptionValuesAsMap(ids)
            );
            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.OPTION_VALUE_NOT_FOUND);
        }
    }
}
