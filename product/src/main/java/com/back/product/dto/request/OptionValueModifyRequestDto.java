package com.back.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record OptionValueModifyRequestDto(
        @NotNull(message = "Option Group ID cannot be null")
        @Min(value = 1, message = "Option Group Id must be over 1")
        Long optionGroupId,

        @NotNull(message = "Option Value Name cannot be null")
        @Pattern(regexp = "[a-zA-Z0-9]{1,20}$", message = "Option values are permitted only english and number")
        String name
) {
}
