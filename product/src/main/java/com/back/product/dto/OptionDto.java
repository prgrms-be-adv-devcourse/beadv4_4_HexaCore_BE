package com.back.product.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionDto(
        GroupDto group,
        List<ValueDto> values
) {
    @Builder
    public record GroupDto(
            Long id,
            String name
    ) {
    }


    @Builder
    public record ValueDto(
            Long id,
            String name
    ) {
    }
}
