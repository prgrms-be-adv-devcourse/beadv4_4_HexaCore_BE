package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.OptionGroupRepository;
import com.back.product.adapter.out.OptionValueRepository;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.OptionDto;
import com.back.product.dto.request.OptionAppendRequestDto;
import com.back.product.dto.request.OptionCreateRequestDto;
import com.back.product.dto.request.OptionGroupModifyRequestDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.mapper.OptionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OptionUseCase 단위 테스트")
class OptionUseCaseTest {

    @InjectMocks
    private OptionUseCase optionUseCase;

    @Mock
    private ProductSupport productSupport;

    @Mock
    private OptionMapper optionMapper;

    @Mock
    private OptionGroupRepository optionGroupRepository;

    @Mock
    private OptionValueRepository optionValueRepository;

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
            OptionListResponseDto result = optionUseCase.findAllOptions();

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
            OptionListResponseDto result = optionUseCase.findAllOptions();

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
            OptionListResponseDto result = optionUseCase.findAllOptions();

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).hasSize(1);
            assertThat(result.options().get(0).group().name()).isEqualTo("Color");
            assertThat(result.options().get(0).values()).isEmpty();
        }
    }

    @Nested
    @DisplayName("createOptions 메서드")
    class CreateOptionsTest {

        @Test
        @DisplayName("성공: 새로운 옵션 그룹과 값들을 생성한다")
        void createOptions_success_newGroupAndValues() {
            // given
            // 1. 요청 DTO 생성
            OptionCreateRequestDto.OptionDto requestOption1 = new OptionCreateRequestDto.OptionDto("color", List.of("red", "blue"));
            OptionCreateRequestDto.OptionDto requestOption2 = new OptionCreateRequestDto.OptionDto("size", List.of("small", "large"));
            OptionCreateRequestDto requestDto = new OptionCreateRequestDto(List.of(requestOption1, requestOption2));

            // 2. Mocking ProductSupport for group lookup (returns null for new groups)
            given(productSupport.getOptionGroupByName(eq("color"))).willReturn(null);
            given(productSupport.getOptionGroupByName(eq("size"))).willReturn(null);

            // 3. Mocking OptionMapper for group and value entity conversion
            OptionGroup newGroup1 = OptionGroup.builder().id(1L).name("color").build();
            OptionGroup newGroup2 = OptionGroup.builder().id(2L).name("size").build();
            given(optionMapper.toGroupEntity(eq("color"))).willReturn(newGroup1);
            given(optionMapper.toGroupEntity(eq("size"))).willReturn(newGroup2);

            OptionValue newValue1_1 = OptionValue.builder().id(101L).optionGroup(newGroup1).value("red").build();
            OptionValue newValue1_2 = OptionValue.builder().id(102L).optionGroup(newGroup1).value("blue").build();
            OptionValue newValue2_1 = OptionValue.builder().id(201L).optionGroup(newGroup2).value("small").build();
            OptionValue newValue2_2 = OptionValue.builder().id(202L).optionGroup(newGroup2).value("large").build();

            given(optionMapper.toValueEntity(eq(newGroup1), eq("red"))).willReturn(newValue1_1);
            given(optionMapper.toValueEntity(eq(newGroup1), eq("blue"))).willReturn(newValue1_2);
            given(optionMapper.toValueEntity(eq(newGroup2), eq("small"))).willReturn(newValue2_1);
            given(optionMapper.toValueEntity(eq(newGroup2), eq("large"))).willReturn(newValue2_2);

            // 4. Mocking OptionGroupRepository save
            given(optionGroupRepository.save(eq(newGroup1))).willReturn(newGroup1);
            given(optionGroupRepository.save(eq(newGroup2))).willReturn(newGroup2);

            // 5. Mocking OptionValueRepository saveAll
            given(optionValueRepository.saveAll(any(List.class))).willAnswer(invocation -> {
                List<OptionValue> values = invocation.getArgument(0);
                // Assign IDs or perform other mock operations if needed
                return values;
            });

            // 6. Mocking OptionMapper toDto for final response conversion
            OptionDto.GroupDto responseGroup1 = OptionDto.GroupDto.builder().id(1L).name("color").build();
            List<OptionDto.ValueDto> responseValues1 = List.of(
                    OptionDto.ValueDto.builder().id(101L).name("red").build(),
                    OptionDto.ValueDto.builder().id(102L).name("blue").build()
            );
            OptionDto responseOptionDto1 = OptionDto.builder().group(responseGroup1).values(responseValues1).build();

            OptionDto.GroupDto responseGroup2 = OptionDto.GroupDto.builder().id(2L).name("size").build();
            List<OptionDto.ValueDto> responseValues2 = List.of(
                    OptionDto.ValueDto.builder().id(201L).name("small").build(),
                    OptionDto.ValueDto.builder().id(202L).name("large").build()
            );
            OptionDto responseOptionDto2 = OptionDto.builder().group(responseGroup2).values(responseValues2).build();

            given(optionMapper.toDto(eq(newGroup1), any(List.class))).willReturn(responseOptionDto1);
            given(optionMapper.toDto(eq(newGroup2), any(List.class))).willReturn(responseOptionDto2);


            // when
            OptionListResponseDto result = optionUseCase.createOptions(requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).hasSize(2);
            assertThat(result.options().get(0).group().name()).isEqualTo("color");
            assertThat(result.options().get(0).values()).hasSize(2);
            assertThat(result.options().get(1).group().name()).isEqualTo("size");
            assertThat(result.options().get(1).values()).hasSize(2);

            // Verify interactions
            verify(productSupport, times(1)).getOptionGroupByName(eq("color"));
            verify(productSupport, times(1)).getOptionGroupByName(eq("size"));
            verify(optionMapper, times(1)).toGroupEntity(eq("color"));
            verify(optionMapper, times(1)).toGroupEntity(eq("size"));
            verify(optionGroupRepository, times(1)).save(eq(newGroup1));
            verify(optionGroupRepository, times(1)).save(eq(newGroup2));
            verify(optionValueRepository, times(2)).saveAll(any(List.class)); // saveAll is called twice, once for each option
            verify(optionMapper, times(1)).toDto(eq(newGroup1), any(List.class));
            verify(optionMapper, times(1)).toDto(eq(newGroup2), any(List.class));
        }

        @Test
        @DisplayName("성공: 기존 옵션 그룹을 사용하고 새로운 값들을 생성한다")
        void createOptions_success_existingGroupAndNewValues() {
            // given
            // 1. 요청 DTO 생성
            OptionCreateRequestDto.OptionDto requestOption1 = new OptionCreateRequestDto.OptionDto("color", List.of("red", "blue"));
            OptionCreateRequestDto requestDto = new OptionCreateRequestDto(List.of(requestOption1));

            // 2. Mocking ProductSupport for group lookup (returns existing group)
            OptionGroup existingGroup1 = OptionGroup.builder().id(1L).name("color").build();
            given(productSupport.getOptionGroupByName(eq("color"))).willReturn(existingGroup1);

            // 3. Mocking OptionMapper for value entity conversion
            OptionValue newValue1_1 = OptionValue.builder().id(101L).optionGroup(existingGroup1).value("red").build();
            OptionValue newValue1_2 = OptionValue.builder().id(102L).optionGroup(existingGroup1).value("blue").build();

            given(optionMapper.toValueEntity(eq(existingGroup1), eq("red"))).willReturn(newValue1_1);
            given(optionMapper.toValueEntity(eq(existingGroup1), eq("blue"))).willReturn(newValue1_2);

            // 4. OptionGroupRepository save should not be called
            verify(optionGroupRepository, never()).save(any(OptionGroup.class));

            // 5. Mocking OptionValueRepository saveAll
            given(optionValueRepository.saveAll(any(List.class))).willAnswer(invocation -> {
                List<OptionValue> values = invocation.getArgument(0);
                return values;
            });

            // 6. Mocking OptionMapper toDto for final response conversion
            OptionDto.GroupDto responseGroup1 = OptionDto.GroupDto.builder().id(1L).name("color").build();
            List<OptionDto.ValueDto> responseValues1 = List.of(
                    OptionDto.ValueDto.builder().id(101L).name("red").build(),
                    OptionDto.ValueDto.builder().id(102L).name("blue").build()
            );
            OptionDto responseOptionDto1 = OptionDto.builder().group(responseGroup1).values(responseValues1).build();

            given(optionMapper.toDto(eq(existingGroup1), any(List.class))).willReturn(responseOptionDto1);

            // when
            OptionListResponseDto result = optionUseCase.createOptions(requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).hasSize(1);
            assertThat(result.options().get(0).group().name()).isEqualTo("color");
            assertThat(result.options().get(0).values()).hasSize(2);

            // Verify interactions
            verify(productSupport, times(1)).getOptionGroupByName(eq("color"));
            verify(optionMapper, never()).toGroupEntity(any(String.class)); // Not called for existing group
            verify(optionGroupRepository, never()).save(any(OptionGroup.class)); // Not called for existing group
            verify(optionValueRepository, times(1)).saveAll(any(List.class));
            verify(optionMapper, times(1)).toDto(eq(existingGroup1), any(List.class));
        }

        @Test
        @DisplayName("성공: 기존 그룹과 새로운 그룹이 혼합된 경우에도 정상적으로 생성한다")
        void createOptions_success_mixedGroupsAndValues() {
            // given
            // 1. 요청 DTO 생성
            OptionCreateRequestDto.OptionDto requestOption1 = new OptionCreateRequestDto.OptionDto("color", List.of("red", "blue")); // Existing group
            OptionCreateRequestDto.OptionDto requestOption2 = new OptionCreateRequestDto.OptionDto("pattern", List.of("stripe", "dot")); // New group
            OptionCreateRequestDto requestDto = new OptionCreateRequestDto(List.of(requestOption1, requestOption2));

            // 2. Mocking ProductSupport for group lookup
            OptionGroup existingGroup1 = OptionGroup.builder().id(1L).name("color").build();
            given(productSupport.getOptionGroupByName(eq("color"))).willReturn(existingGroup1); // Existing
            given(productSupport.getOptionGroupByName(eq("pattern"))).willReturn(null); // New

            // 3. Mocking OptionMapper for group and value entity conversion
            OptionGroup newGroup2 = OptionGroup.builder().id(2L).name("pattern").build();
            given(optionMapper.toGroupEntity(eq("pattern"))).willReturn(newGroup2); // Only for new group

            OptionValue newValue1_1 = OptionValue.builder().id(101L).optionGroup(existingGroup1).value("red").build();
            OptionValue newValue1_2 = OptionValue.builder().id(102L).optionGroup(existingGroup1).value("blue").build();
            OptionValue newValue2_1 = OptionValue.builder().id(201L).optionGroup(newGroup2).value("stripe").build();
            OptionValue newValue2_2 = OptionValue.builder().id(202L).optionGroup(newGroup2).value("dot").build();

            given(optionMapper.toValueEntity(eq(existingGroup1), eq("red"))).willReturn(newValue1_1);
            given(optionMapper.toValueEntity(eq(existingGroup1), eq("blue"))).willReturn(newValue1_2);
            given(optionMapper.toValueEntity(eq(newGroup2), eq("stripe"))).willReturn(newValue2_1);
            given(optionMapper.toValueEntity(eq(newGroup2), eq("dot"))).willReturn(newValue2_2);

            // 4. Mocking OptionGroupRepository save
            given(optionGroupRepository.save(eq(newGroup2))).willReturn(newGroup2); // Only for new group

            // 5. Mocking OptionValueRepository saveAll
            given(optionValueRepository.saveAll(any(List.class))).willAnswer(invocation -> {
                List<OptionValue> values = invocation.getArgument(0);
                return values;
            });

            // 6. Mocking OptionMapper toDto for final response conversion
            OptionDto.GroupDto responseGroup1 = OptionDto.GroupDto.builder().id(1L).name("color").build();
            List<OptionDto.ValueDto> responseValues1 = List.of(
                    OptionDto.ValueDto.builder().id(101L).name("red").build(),
                    OptionDto.ValueDto.builder().id(102L).name("blue").build()
            );
            OptionDto responseOptionDto1 = OptionDto.builder().group(responseGroup1).values(responseValues1).build();

            OptionDto.GroupDto responseGroup2 = OptionDto.GroupDto.builder().id(2L).name("pattern").build();
            List<OptionDto.ValueDto> responseValues2 = List.of(
                    OptionDto.ValueDto.builder().id(201L).name("stripe").build(),
                    OptionDto.ValueDto.builder().id(202L).name("dot").build()
            );
            OptionDto responseOptionDto2 = OptionDto.builder().group(responseGroup2).values(responseValues2).build();

            given(optionMapper.toDto(eq(existingGroup1), any(List.class))).willReturn(responseOptionDto1);
            given(optionMapper.toDto(eq(newGroup2), any(List.class))).willReturn(responseOptionDto2);

            // when
            OptionListResponseDto result = optionUseCase.createOptions(requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.options()).hasSize(2);
            assertThat(result.options().get(0).group().name()).isEqualTo("color");
            assertThat(result.options().get(0).values()).hasSize(2);
            assertThat(result.options().get(1).group().name()).isEqualTo("pattern");
            assertThat(result.options().get(1).values()).hasSize(2);

            // Verify interactions
            verify(productSupport, times(1)).getOptionGroupByName(eq("color"));
            verify(productSupport, times(1)).getOptionGroupByName(eq("pattern"));
            verify(optionMapper, never()).toGroupEntity(eq("color")); // Not called for existing group
            verify(optionMapper, times(1)).toGroupEntity(eq("pattern")); // Called for new group
            verify(optionGroupRepository, never()).save(eq(existingGroup1)); // Not called for existing group
            verify(optionGroupRepository, times(1)).save(eq(newGroup2)); // Called for new group
            verify(optionValueRepository, times(2)).saveAll(any(List.class));
            verify(optionMapper, times(1)).toDto(eq(existingGroup1), any(List.class));
            verify(optionMapper, times(1)).toDto(eq(newGroup2), any(List.class));
        }
    }

    @Nested
    @DisplayName("appendOptions 메서드")
    class AppendOptionsTest {

        @Test
        @DisplayName("성공: 기존 옵션 그룹에 새로운 값들을 추가한다")
        void appendOptions_success() {
            // given
            Long optionGroupId = 1L;
            OptionAppendRequestDto requestDto = OptionAppendRequestDto.builder()
                    .values(List.of("새로운값1", "새로운값2"))
                    .build();

            OptionGroup existingGroup = OptionGroup.builder()
                    .id(optionGroupId)
                    .name("색상")
                    .build();

            // Mocking for dependencies
            given(productSupport.getOptionGroupById(optionGroupId)).willReturn(Optional.of(existingGroup));

            OptionValue newValue1 = OptionValue.builder()
                    .id(101L)
                    .optionGroup(existingGroup)
                    .value("새로운값1")
                    .build();
            OptionValue newValue2 = OptionValue.builder()
                    .id(102L)
                    .optionGroup(existingGroup)
                    .value("새로운값2")
                    .build();
            List<OptionValue> createdValues = List.of(newValue1, newValue2);

            given(optionMapper.toValueEntity(existingGroup, "새로운값1")).willReturn(newValue1);
            given(optionMapper.toValueEntity(existingGroup, "새로운값2")).willReturn(newValue2);
            given(optionValueRepository.saveAll(any(List.class))).willReturn(createdValues);

            OptionDto.GroupDto responseGroupDto = OptionDto.GroupDto.builder()
                    .id(optionGroupId)
                    .name("색상")
                    .build();
            List<OptionDto.ValueDto> responseValueDtos = List.of(
                    OptionDto.ValueDto.builder().id(101L).name("새로운값1").build(),
                    OptionDto.ValueDto.builder().id(102L).name("새로운값2").build()
            );
            OptionDto finalOptionDto = OptionDto.builder()
                    .group(responseGroupDto)
                    .values(responseValueDtos)
                    .build();

            given(optionMapper.toDto(existingGroup, createdValues)).willReturn(finalOptionDto);

            // when
            OptionResponseDto result = optionUseCase.appendOptions(optionGroupId, requestDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.option().group().name()).isEqualTo("색상");
            assertThat(result.option().values()).hasSize(2);
            assertThat(result.option().values().get(0).name()).isEqualTo("새로운값1");

            // verify interactions
            verify(productSupport, times(1)).getOptionGroupById(optionGroupId);
            verify(optionValueRepository, times(1)).saveAll(any(List.class));
            verify(optionMapper, times(1)).toDto(any(OptionGroup.class), any(List.class));
        }

        @Test
        @DisplayName("실패: 옵션 그룹이 존재하지 않으면 CustomException을 발생시킨다")
        void appendOptions_fail_groupNotFound() {
            // given
            Long nonExistentGroupId = 99L;
            OptionAppendRequestDto requestDto = OptionAppendRequestDto.builder()
                    .values(List.of("새로운값"))
                    .build();

            given(productSupport.getOptionGroupById(nonExistentGroupId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    optionUseCase.appendOptions(nonExistentGroupId, requestDto)
            );

            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.OPTION_GROUP_NOT_FOUND);

            // verify that no save operation was attempted
            verify(optionValueRepository, never()).saveAll(any(List.class));
        }
    }

    @Nested
    @DisplayName("modifyOptionGroup 메서드")
    class ModifyOptionGroupTest {

        @Test
        @DisplayName("성공: 옵션 그룹의 이름을 성공적으로 변경한다")
        void modifyOptionGroup_success() {
            // given
            Long optionGroupId = 1L;
            String newName = "newcolor";
            OptionGroupModifyRequestDto requestDto = OptionGroupModifyRequestDto.builder()
                    .name(newName)
                    .build();

            // Use a real object for the entity to test the actual state change
            // but since it's a unit test, we can mock it as well to verify interactions.
            OptionGroup existingGroup = mock(OptionGroup.class);

            given(productSupport.getOptionGroupById(optionGroupId)).willReturn(Optional.of(existingGroup));

            // Stubbing the getters that will be used in convertToModifyGroupDto
            given(existingGroup.getId()).willReturn(optionGroupId);
            given(existingGroup.getName()).willReturn(newName); // Assume name is updated
            given(existingGroup.getLastModifiedAt()).willReturn(LocalDateTime.now());


            // when
            OptionGroupModifyResponseDto result = optionUseCase.modifyOptionGroup(optionGroupId, requestDto);

            // then
            // Verify that the entity's state-changing method was called
            verify(existingGroup, times(1)).modifyName(newName);

            // Assert the response DTO
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(optionGroupId);
            assertThat(result.name()).isEqualTo(newName);
            assertThat(result.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패: 옵션 그룹이 존재하지 않으면 CustomException을 발생시킨다")
        void modifyOptionGroup_fail_groupNotFound() {
            // given
            Long nonExistentGroupId = 99L;
            String newName = "newcolor";
            OptionGroupModifyRequestDto requestDto = OptionGroupModifyRequestDto.builder()
                    .name(newName)
                    .build();

            given(productSupport.getOptionGroupById(nonExistentGroupId)).willReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () ->
                    optionUseCase.modifyOptionGroup(nonExistentGroupId, requestDto)
            );

            assertThat(exception.getFailureCode()).isEqualTo(FailureCode.OPTION_GROUP_NOT_FOUND);
        }
    }
}
