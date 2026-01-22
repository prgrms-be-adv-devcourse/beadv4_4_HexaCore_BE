package com.back.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Builder
public record ProductVariantCreateRequestDto(
        @NotEmpty(message = "Option values for variant cannot be empty")
        List<Long> optionValueIds,

        @NotNull(message = "Inventory cannot be null")
        @Min(value = 0, message = "Inventory must be a non-negative value")
        Long inventory,

        @NotEmpty(message = "Image URLs for variant cannot be empty")
        List<@URL(message = "유효한 URL 형식이 아닙니다.") String> imageUrls
) {
}
