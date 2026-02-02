package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionAppendRequestDto(
        @NotEmpty(message = "Option Values cannot be null")
        @Valid
        List<@Pattern(regexp = "[a-zA-Z0-9]{1,20}$", message = "Option values can be only english and number") String> values
) {
}
