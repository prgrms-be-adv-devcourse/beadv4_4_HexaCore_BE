package com.back.product.mapper;

import com.back.product.domain.OptionGroup;
import com.back.product.domain.OptionValue;
import com.back.product.dto.event.kafka.OptionPayload;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.response.OptionValueModifyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OptionValueMapper {
    private final OptionGroupMapper optionGroupMapper;

    public OptionValue toValueEntity(String group, String value) {
        OptionGroup groupEntity = optionGroupMapper.toGroupEntity(group);
        return toValueEntity(groupEntity, value);
    }

    public OptionValue toValueEntity(OptionGroup group, String value) {
        return OptionValue.builder().optionGroup(group).value(value).build();
    }

    public OptionDto.ValueDto toValueDto(OptionValue value) {
        return OptionDto.ValueDto.builder()
                .id(value.getId())
                .name(value.getValue())
                .build();
    }

    public OptionDto.ValueDto toValueDto(OptionPayload.ValuePayload payload) {
        return OptionDto.ValueDto.builder()
                .id(payload.valueId())
                .name(payload.valueName())
                .build();
    }

    public OptionValueModifyResponseDto.OptionValueDto toModifiedValueDto(OptionValue value) {
        return OptionValueModifyResponseDto.OptionValueDto.builder()
                .id(value.getId())
                .optionGroupId(value.getOptionGroup().getId())
                .value(value.getValue())
                .updatedAt(value.getLastModifiedAt())
                .build();
    }

    public OptionValueModifyResponseDto toModifyResponseDto(OptionValue value) {
        return OptionValueModifyResponseDto.builder()
                .value(toModifiedValueDto(value))
                .build();
    }

    public OptionPayload.ValuePayload toPayload(OptionDto.ValueDto value) {
        return OptionPayload.ValuePayload.builder()
                .valueId(value.id())
                .valueName(value.name())
                .build();
    }
}
