package com.back.product.mapper;

import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.OptionDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OptionMapper {
    public OptionGroup toGroupEntity(String name) {
        return OptionGroup.builder().name(name).build();
    }

    public OptionValue toValueEntity(String group, String value) {
        OptionGroup groupEntity = toGroupEntity(group);
        return toValueEntity(groupEntity, value);
    }

    public OptionValue toValueEntity(OptionGroup group, String value) {
        return OptionValue.builder().optionGroup(group).value(value).build();
    }

    public OptionDto toDto(OptionGroup group, List<OptionValue> values) {
        OptionDto.GroupDto optionGroup = toDto(group);
        List<OptionDto.ValueDto> optionValues = values.stream().map(this::toDto).toList();

        return OptionDto.builder()
                .group(optionGroup)
                .values(optionValues)
                .build();
    }

    private OptionDto.GroupDto toDto(OptionGroup group) {
        return OptionDto.GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .build();
    }

    private OptionDto.ValueDto toDto(OptionValue value) {
        return OptionDto.ValueDto.builder()
                .id(value.getId())
                .name(value.getValue())
                .build();
    }
}
