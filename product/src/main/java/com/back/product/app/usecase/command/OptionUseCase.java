package com.back.product.app.usecase.command;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.OptionGroupRepository;
import com.back.product.adapter.out.persistence.OptionValueRepository;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.request.OptionAppendRequestDto;
import com.back.product.dto.request.OptionListCreateRequestDto;
import com.back.product.dto.request.OptionGroupModifyRequestDto;
import com.back.product.dto.request.OptionValueModifyRequestDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.dto.response.OptionValueModifyResponseDto;
import com.back.product.mapper.OptionGroupMapper;
import com.back.product.mapper.OptionMapper;
import com.back.product.mapper.OptionValueMapper;
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
    private final OptionGroupMapper optionGroupMapper;
    private final OptionValueMapper optionValueMapper;

    private final ProductSupport productSupport;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionValueRepository optionValueRepository;

    @Transactional(readOnly = true)
    public List<OptionValue> findOptionValues(@Valid List<Long> optionValueIds) {
        List<OptionValue> foundValues = productSupport.getAllOptionValues(optionValueIds);

        if (foundValues.size() != optionValueIds.size()) {
            throw new CustomException(FailureCode.OPTION_VALUE_NOT_FOUND);
        }

        return foundValues;
    }

    @Transactional(readOnly = true)
    public List<OptionDto> findAllOptions() {
        List<OptionGroup> optionGroups = productSupport.getAllProductOptionGroups();

        if (optionGroups.isEmpty()) {
            return List.of();
        }

        Map<OptionGroup, List<OptionValue>> optionValueAsMap = productSupport.getAllProductOptionValuesByOptionGroupIn(optionGroups)
                .stream().collect(Collectors.groupingBy(OptionValue::getOptionGroup));

        return optionGroups.stream().map(group -> {
            List<OptionValue> values = optionValueAsMap.getOrDefault(group, new ArrayList<>());
            return optionMapper.toDto(group, values);
        }).toList();
    }

    @Transactional
    public List<OptionDto> createOptions(@Valid OptionListCreateRequestDto request) {
        return request.options().stream()
                .map(option -> createOption(option.group(), option.values()))
                .toList();
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
        
        return optionMapper.toDto(group, createdValues);
    }

    @Transactional
    public OptionDto appendOptions(Long optionGroupId, @Valid OptionAppendRequestDto request) {
        OptionGroup group = productSupport.getOptionGroupById(optionGroupId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

        List<OptionValue> newValues = createOptionValues(group, request.values());

        List<OptionValue> createdValues = optionValueRepository.saveAll(newValues);

        return optionMapper.toDto(group, createdValues);
    }

    @Transactional
    public OptionGroupModifyResponseDto modifyOptionGroup(Long optionGroupId, @Valid OptionGroupModifyRequestDto request) {
        OptionGroup existsGroup = productSupport.getOptionGroupById(optionGroupId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

        existsGroup.modifyName(request.name());

        return optionGroupMapper.toModifyResponseDto(existsGroup);
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

        return optionValueMapper.toModifyResponseDto(existsValue);
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
        return optionGroupMapper.toGroupEntity(name);
    }
    
    private List<OptionValue> createOptionValues(OptionGroup group, List<String> valueNames) {
        return valueNames.stream()
                .map(valueName -> createOptionValue(group, valueName))
                .toList();
    }
    
    private OptionValue createOptionValue(OptionGroup group, String valueName) {
        return optionValueMapper.toValueEntity(group, valueName);
    }
}
