package com.back.product.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionValueModifyResponseDto(
        OptionValueDto value
) {
    @Builder
    public record OptionValueDto(
            Long id,
            Long optionGroupId,
            String value,
            LocalDateTime updatedAt
    ) {
    }
}
