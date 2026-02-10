package com.back.product.mapper;

import com.back.product.domain.OptionGroup;
import com.back.product.dto.event.kafka.OptionPayload;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import org.springframework.stereotype.Component;

@Component
public class OptionGroupMapper {
    public OptionGroup toGroupEntity(String name) {
        return OptionGroup.builder().name(name).build();
    }

    public OptionDto.GroupDto toGroupDto(OptionGroup group) {
        return OptionDto.GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .build();
    }

    public OptionDto.GroupDto toGroupDto(OptionPayload.GroupPayload payload) {
        return OptionDto.GroupDto.builder()
                .id(payload.groupId())
                .name(payload.groupName())
                .build();
    }

    public OptionGroupModifyResponseDto.OptionGroupDto toModifiedGroupDto(OptionGroup group) {
        return OptionGroupModifyResponseDto.OptionGroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .updatedAt(group.getLastModifiedAt())
                .build();
    }

    public OptionGroupModifyResponseDto toModifyResponseDto(OptionGroup group) {
        return OptionGroupModifyResponseDto.builder()
                .group(toModifiedGroupDto(group))
                .build();
    }

    public OptionPayload.GroupPayload toPayload(OptionDto.GroupDto group) {
        return OptionPayload.GroupPayload.builder()
                .groupId(group.id())
                .groupName(group.name())
                .build();
    }
}
