package com.back.product.mapper;

import com.back.common.product.event.payload.OptionPayload;
import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptionMapper {
    private final OptionGroupMapper optionGroupMapper;
    private final OptionValueMapper optionValueMapper;

    public OptionDto toDto(OptionGroup group, List<OptionValue> values) {
        OptionDto.GroupDto optionGroup = optionGroupMapper.toGroupDto(group);
        List<OptionDto.ValueDto> optionValues = values.stream().map(optionValueMapper::toValueDto).toList();

        return OptionDto.builder()
                .group(optionGroup)
                .values(optionValues)
                .build();
    }

    public OptionDto toDto(OptionPayload payload) {
        OptionDto.GroupDto groupDto = optionGroupMapper.toGroupDto(payload.group());
        List<OptionDto.ValueDto> valueDtos = payload.values().stream().map(optionValueMapper::toValueDto).toList();

        return OptionDto.builder()
                .group(groupDto)
                .values(valueDtos)
                .build();
    }

    public List<OptionDto> toDtoList(List<OptionValue> optionValues) {
        return optionValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getOptionGroup))
                .entrySet().stream().map(entry -> {
                    OptionGroup group = entry.getKey();
                    List<OptionValue> values = entry.getValue();
                    return toDto(group, values);
                }).toList();
    }

    public OptionListResponseDto toListResponseDto(List<OptionDto> optionDtos) {
        return OptionListResponseDto.builder()
                .options(optionDtos)
                .build();
    }

    public OptionResponseDto toResponseDto(OptionDto optionDto) {
        return OptionResponseDto.builder()
                .option(optionDto)
                .build();
    }
}
