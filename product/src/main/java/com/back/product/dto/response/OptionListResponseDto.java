package com.back.product.dto.response;

import com.back.product.dto.OptionDto;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionListResponseDto(
        List<OptionDto> options
) {
}
