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
import com.back.product.dto.request.OptionValueModifyRequestDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.dto.response.OptionValueModifyResponseDto;
import com.back.product.mapper.OptionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionUseCase {
    private final OptionMapper optionMapper;
    private final ProductSupport productSupport;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionValueRepository optionValueRepository;

    @Transactional(readOnly = true)
    public Map<Long, OptionValue> findOptionValuesAsMap(@Valid List<Long> optionValueIds) {
        List<OptionValue> foundValues = productSupport.getAllOptionValues(optionValueIds);

        if (foundValues.size() != optionValueIds.size()) {
            throw new CustomException(FailureCode.OPTION_VALUE_NOT_FOUND);
        }

        return foundValues.stream()
                .collect(Collectors.toMap(OptionValue::getId, value -> value));
    }

    @Transactional(readOnly = true)
    public OptionListResponseDto findAllOptions() {
        List<OptionGroup> optionGroups = productSupport.getAllProductOptionGroups();

        if (optionGroups.isEmpty()) {
            return OptionListResponseDto.builder().build();
        }

        Map<OptionGroup, List<OptionValue>> optionValueAsMap = productSupport.getAllProductOptionValuesByOptionGroupIn(optionGroups)
                .stream().collect(Collectors.groupingBy(OptionValue::getOptionGroup));

        return convertToOptionResponseDto(optionGroups, optionValueAsMap);
    }

    @Transactional
    public OptionListResponseDto createOptions(@Valid OptionCreateRequestDto request) {
        List<OptionDto> createdOptions = request.options().stream()
                .map(option -> createOption(option.group(), option.values()))
                .toList();

        return OptionListResponseDto.builder().options(createdOptions).build();
    }

    @Transactional
    public OptionDto createOption(String groupName, List<String> valueNames) {
        OptionGroup group = productSupport.getOptionGroupByName(groupName.toLowerCase());
        
        if (group == null) {
            OptionGroup newGroup = createOptionGroup(groupName);
            group = optionGroupRepository.save(newGroup);
        }

        List<OptionValue> newValues = createOptionValues(group, valueNames);
        List<OptionValue> createdValues = optionValueRepository.saveAll(newValues);
        
        return convertToOptionDto(group, createdValues);
    }

    @Transactional
    public OptionResponseDto appendOptions(Long optionGroupId, @Valid OptionAppendRequestDto request) {
        OptionGroup group = productSupport.getOptionGroupById(optionGroupId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

        List<OptionValue> newValues = createOptionValues(group, request.values());

        List<OptionValue> createdValues = optionValueRepository.saveAll(newValues);

        return OptionResponseDto.builder().option(convertToOptionDto(group, createdValues)).build();
    }

    @Transactional
    public OptionGroupModifyResponseDto modifyOptionGroup(Long optionGroupId, @Valid OptionGroupModifyRequestDto request) {
        OptionGroup existsGroup = productSupport.getOptionGroupById(optionGroupId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

        existsGroup.modifyName(request.name());

        return convertToModifyGroupDto(existsGroup);
    }

    @Transactional
    public OptionValueModifyResponseDto modifyOptionValue(Long optionValueId, @Valid OptionValueModifyRequestDto request) {
        OptionValue existsValue = productSupport.getOptionValueById(optionValueId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_VALUE_NOT_FOUND));

        existsValue.modifyName(request.name());

        if (existsValue.willChangeGroup(request.optionGroupId())) {
            OptionGroup changedGroup = productSupport.getOptionGroupById(request.optionGroupId())
                            .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

            existsValue.changeGroup(changedGroup);
        }

        return convertToModifyValueDto(existsValue);
    }

    @Transactional
    public void deleteOptions(Long optionGroupId) {
        optionValueRepository.deleteAllByOptionGroup_Id(optionGroupId);

        optionGroupRepository.deleteById(optionGroupId);
    }

    @Transactional
    public void deleteOption(Long optionValueId) {
        optionValueRepository.deleteById(optionValueId);
    }
    
    private OptionGroup createOptionGroup(String name) {
        return optionMapper.toGroupEntity(name);
    }
    
    private List<OptionValue> createOptionValues(OptionGroup group, List<String> valueNames) {
        return valueNames.stream()
                .map(valueName -> createOptionValue(group, valueName))
                .toList();
    }
    
    private OptionValue createOptionValue(OptionGroup group, String valueName) {
        return optionMapper.toValueEntity(group, valueName);
    }

    private OptionListResponseDto convertToOptionResponseDto(List<OptionGroup> optionGroups, Map<OptionGroup, List<OptionValue>> optionValueAsMap) {
        List<OptionDto> optionDtos = optionGroups.stream().map(group -> {
            List<OptionValue> values = optionValueAsMap.get(group);

            if (values == null) values = new ArrayList<>();

            return convertToOptionDto(group, values);
        }).toList();

        return OptionListResponseDto.builder().options(optionDtos).build();
    }
    
    private OptionDto convertToOptionDto(OptionGroup group, List<OptionValue> values) {
        return optionMapper.toDto(group, values);
    }

    private OptionGroupModifyResponseDto convertToModifyGroupDto(OptionGroup group) {
        return OptionGroupModifyResponseDto.builder()
                .group(
                        OptionGroupModifyResponseDto.OptionGroupDto.builder()
                                .id(group.getId())
                                .name(group.getName())
                                .updatedAt(group.getLastModifiedAt())
                                .build()
                )
                .build();
    }

    private OptionValueModifyResponseDto convertToModifyValueDto(OptionValue value) {
        return OptionValueModifyResponseDto.builder()
                .value(
                        OptionValueModifyResponseDto.OptionValueDto.builder()
                                .id(value.getId())
                                .optionGroupId(value.getOptionGroup().getId())
                                .value(value.getValue())
                                .updatedAt(value.getLastModifiedAt())
                                .build()
                )
                .build();
    }
}
