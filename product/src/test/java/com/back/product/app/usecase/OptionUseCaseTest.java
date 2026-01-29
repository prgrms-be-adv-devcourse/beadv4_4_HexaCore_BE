package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.OptionDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.mapper.OptionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("OptionUseCase 단위 테스트")
class OptionUseCaseTest {

    @InjectMocks
    private OptionUseCase optionUseCase;

    @Mock
    private ProductSupport productSupport;

    @Mock
    private OptionMapper optionMapper;

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

    @Nested
    @DisplayName("findAllOptions 메서드")
    class FindAllOptionsTest {

        @Test
        @DisplayName("성공: 모든 옵션 그룹과 값을 조회하여 DTO로 반환한다")
        void findAllOptions_Success_WithOptionGroupsAndValues() {
            // given
            OptionGroup group1 = OptionGroup.builder().id(1L).name("Color").build();
            OptionGroup group2 = OptionGroup.builder().id(2L).name("Size").build();
            List<OptionGroup> optionGroups = List.of(group1, group2);

            OptionValue value1 = OptionValue.builder().id(10L).value("Red").optionGroup(group1).build();
            OptionValue value2 = OptionValue.builder().id(11L).value("Blue").optionGroup(group1).build();
            OptionValue value3 = OptionValue.builder().id(20L).value("Small").optionGroup(group2).build();
            List<OptionValue> optionValues = List.of(value1, value2, value3);

            given(productSupport.getAllProductOptionGroups()).willReturn(optionGroups);
            given(productSupport.getAllProductOptionValuesByOptionGroupIn(any(List.class))).willReturn(optionValues);

            // Mock OptionMapper behavior
            OptionDto.GroupDto groupDto1 = OptionDto.GroupDto.builder().id(1L).name("Color").build();
            OptionDto.ValueDto valueDto1 = OptionDto.ValueDto.builder().id(10L).name("Red").build();
            OptionDto.ValueDto valueDto2 = OptionDto.ValueDto.builder().id(11L).name("Blue").build();
            OptionDto optionDto1 = OptionDto.builder().group(groupDto1).values(List.of(valueDto1, valueDto2)).build();
            given(optionMapper.toDto(eq(group1), any(List.class))).willReturn(optionDto1);


            OptionDto.GroupDto groupDto2 = OptionDto.GroupDto.builder().id(2L).name("Size").build();
            OptionDto.ValueDto valueDto3 = OptionDto.ValueDto.builder().id(20L).name("Small").build();
            OptionDto optionDto2 = OptionDto.builder().group(groupDto2).values(List.of(valueDto3)).build();
            given(optionMapper.toDto(eq(group2), any(List.class))).willReturn(optionDto2);


            // when
            OptionResponseDto result = optionUseCase.findAllOptions();

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).hasSize(2);
            assertThat(result.options().get(0).group().name()).isEqualTo("Color");
            assertThat(result.options().get(0).values()).hasSize(2);
            assertThat(result.options().get(1).group().name()).isEqualTo("Size");
            assertThat(result.options().get(1).values()).hasSize(1);
        }

        @Test
        @DisplayName("성공: 옵션 그룹이 없을 경우 빈 응답 DTO를 반환한다")
        void findAllOptions_Success_NoOptionGroups() {
            // given
            given(productSupport.getAllProductOptionGroups()).willReturn(new ArrayList<>());

            // when
            OptionResponseDto result = optionUseCase.findAllOptions();

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).isNull();
        }

        @Test
        @DisplayName("성공: 옵션 그룹은 존재하지만 해당 그룹에 속하는 옵션 값이 없을 경우 빈 옵션 값 리스트를 포함한 DTO를 반환한다")
        void findAllOptions_Success_OptionGroupsButNoValues() {
            // given
            OptionGroup group1 = OptionGroup.builder().id(1L).name("Color").build();
            List<OptionGroup> optionGroups = List.of(group1);

            given(productSupport.getAllProductOptionGroups()).willReturn(optionGroups);
            given(productSupport.getAllProductOptionValuesByOptionGroupIn(any(List.class))).willReturn(new ArrayList<>());

            // Mock OptionMapper behavior for a group with no values
            OptionDto.GroupDto groupDto1 = OptionDto.GroupDto.builder().id(1L).name("Color").build();
            OptionDto optionDto1 = OptionDto.builder().group(groupDto1).values(new ArrayList<>()).build();
            given(optionMapper.toDto(eq(group1), any(List.class))).willReturn(optionDto1);

            // when
            OptionResponseDto result = optionUseCase.findAllOptions();

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).hasSize(1);
            assertThat(result.options().get(0).group().name()).isEqualTo("Color");
            assertThat(result.options().get(0).values()).isEmpty();
        }
    }
}
