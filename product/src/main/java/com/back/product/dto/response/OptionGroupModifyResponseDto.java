package com.back.product.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionGroupModifyResponseDto(
        Long id,
        String name,
        LocalDateTime updatedAt
) {
}
