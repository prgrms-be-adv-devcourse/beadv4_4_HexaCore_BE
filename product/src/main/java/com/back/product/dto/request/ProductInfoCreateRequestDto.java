package com.back.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductInfoCreateRequestDto(
        @NotNull(message = "Brand ID cannot be null")
        @Min(value = 1, message = "Brand ID must be a Integer value")
        Long brandId,

        @NotNull(message = "Category ID cannot be null")
        @Min(value = 1, message = "Category ID must be a Integer value")
        Long categoryId,

        @NotBlank(message = "Name cannot be blank")
        @Pattern(regexp = "^[A-Za-z0-9 ]{1,50}$", message = "Name must be alphanumeric and up to 50 characters")
        String name,

        @NotBlank(message = "Code cannot be blank")
        @Size(min = 1, max = 50, message = "Code must be between 1 and 50 characters")
        String code,

        @NotNull(message = "Release price cannot be null")
        @Min(value = 1, message = "Release price must be at least 1")
        BigDecimal releasePrice,

        @NotNull(message = "Released date cannot be null")
        LocalDateTime releasedDate
) {
}
