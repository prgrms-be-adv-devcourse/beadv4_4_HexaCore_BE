package com.back.product.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionGroupModifyResponseDto(
        OptionGroupDto group
) {
    @Builder
    public record OptionGroupDto(
            Long id,
            String name,
            LocalDateTime updatedAt
    ) {
    }
}
