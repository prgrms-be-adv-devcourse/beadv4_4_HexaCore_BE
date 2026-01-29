package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.OptionDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.mapper.OptionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OptionUseCase {
    private final OptionMapper optionMapper;
    private final ProductSupport productSupport;

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

        return convertToDto(optionGroups, optionValueAsMap);
    }

    private OptionResponseDto convertToDto(List<OptionGroup> optionGroups, Map<OptionGroup, List<OptionValue>> optionValueAsMap) {
        List<OptionDto> optionDtos = optionGroups.stream().map(group -> {
            List<OptionValue> values = optionValueAsMap.get(group);

            if (values == null) values = new ArrayList<>();

            return optionMapper.toDto(group, values);
        }).toList();

        return OptionResponseDto.builder().options(optionDtos).build();
    }
}
