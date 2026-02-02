package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductQueryRequestDto(
        @NotEmpty(message = "Product IDs cannot be empty")
        @Valid
        List<@Min(value = 1, message = "Product ID must be Integer") Long> productIds
) {
}
