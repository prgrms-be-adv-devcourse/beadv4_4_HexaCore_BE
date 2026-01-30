package com.back.product.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionValueModifyResponseDto(
        Long id,
        Long optionGroupId,
        String value,
        LocalDateTime updatedAt
) {
}
