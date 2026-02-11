package com.back.product.app.usecase.command;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.OptionGroupRepository;
import com.back.product.adapter.out.persistence.OptionValueRepository;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.command.OptionCreateCommand;
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

    @Loggable
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

    @Loggable
    @Transactional
    public List<OptionDto> createOptions(List<OptionCreateCommand> options) {
        return options.stream()
                .map(option -> createOption(option.group(), option.values()))
                .toList();
    }

    @Loggable
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

    @Loggable
    @Transactional
    public OptionDto appendOptions(Long optionGroupId, List<String> values) {
        OptionGroup group = productSupport.getOptionGroupById(optionGroupId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

        List<OptionValue> newValues = createOptionValues(group, values);

        List<OptionValue> createdValues = optionValueRepository.saveAll(newValues);

        return optionMapper.toDto(group, createdValues);
    }

    @Loggable
    @Transactional
    public OptionGroupModifyResponseDto modifyOptionGroup(Long optionGroupId, String name) {
        OptionGroup existsGroup = productSupport.getOptionGroupById(optionGroupId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

        existsGroup.modifyName(name);

        return optionGroupMapper.toModifyResponseDto(existsGroup);
    }

    @Loggable
    @Transactional
    public OptionValueModifyResponseDto modifyOptionValue(Long optionGroupId, Long optionValueId, String name) {
        OptionValue existsValue = productSupport.getOptionValueById(optionValueId)
                .orElseThrow(() -> new CustomException(FailureCode.OPTION_VALUE_NOT_FOUND));

        existsValue.modifyName(name);

        if (existsValue.willChangeGroup(optionGroupId)) {
            OptionGroup changedGroup = productSupport.getOptionGroupById(optionGroupId)
                            .orElseThrow(() -> new CustomException(FailureCode.OPTION_GROUP_NOT_FOUND));

            existsValue.changeGroup(changedGroup);
        }

        return optionValueMapper.toModifyResponseDto(existsValue);
    }

    @Loggable
    @Transactional
    public void deleteOptions(Long optionGroupId) {
        optionValueRepository.deleteAllByOptionGroup_Id(optionGroupId);

        optionGroupRepository.deleteById(optionGroupId);
    }

    @Loggable
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
