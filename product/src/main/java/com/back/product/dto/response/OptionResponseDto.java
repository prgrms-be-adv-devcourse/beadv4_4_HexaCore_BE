package com.back.product.dto.response;

import com.back.product.dto.model.OptionDto;
import lombok.Builder;

@Builder
public record OptionResponseDto(
        OptionDto option
) {
}
