package com.back.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record OptionGroupModifyRequestDto(
        @NotBlank(message = "Option Group Name cannot be blank")
        @Pattern(regexp = "^[a-z]{1,20}$", message = "Option Group Name is permitted only lower case english")
        String name
) {
}
