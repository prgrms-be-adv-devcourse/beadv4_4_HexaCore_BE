package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionListCreateRequestDto(
        @NotEmpty(message = "Options cannot be null")
        @Valid
        List<OptionCreateRequestDto> options
) {
}
