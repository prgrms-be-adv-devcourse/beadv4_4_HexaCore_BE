package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.OptionGroupRepository;
import com.back.product.adapter.out.OptionValueRepository;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.OptionDto;
import com.back.product.dto.request.OptionCreateRequestDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.mapper.OptionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public OptionResponseDto findAllOptions() {
        List<OptionGroup> optionGroups = productSupport.getAllProductOptionGroups();

        if (optionGroups.isEmpty()) {
            return OptionResponseDto.builder().build();
        }

        Map<OptionGroup, List<OptionValue>> optionValueAsMap = productSupport.getAllProductOptionValuesByOptionGroupIn(optionGroups)
                .stream().collect(Collectors.groupingBy(OptionValue::getOptionGroup));

        return convertToOptionResponseDto(optionGroups, optionValueAsMap);
    }

    @Transactional
    public OptionResponseDto createOptions(@Valid OptionCreateRequestDto request) {
        List<OptionDto> createdOptions = request.options().stream()
                .map(option -> createOption(option.group(), option.values()))
                .toList();

        return OptionResponseDto.builder().options(createdOptions).build();
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

    private OptionResponseDto convertToOptionResponseDto(List<OptionGroup> optionGroups, Map<OptionGroup, List<OptionValue>> optionValueAsMap) {
        List<OptionDto> optionDtos = optionGroups.stream().map(group -> {
            List<OptionValue> values = optionValueAsMap.get(group);

            if (values == null) values = new ArrayList<>();

            return convertToOptionDto(group, values);
        }).toList();

        return OptionResponseDto.builder().options(optionDtos).build();
    }
    
    private OptionDto convertToOptionDto(OptionGroup group, List<OptionValue> values) {
        return optionMapper.toDto(group, values);
    }
}
