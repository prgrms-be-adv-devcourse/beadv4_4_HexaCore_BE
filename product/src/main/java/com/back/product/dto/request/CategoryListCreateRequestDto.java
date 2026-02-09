package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record CategoryListCreateRequestDto(
        @NotEmpty(message = "Categories cannot be empty")
        @Valid
    List<CategoryCreateRequestDto> categories
) {
}
