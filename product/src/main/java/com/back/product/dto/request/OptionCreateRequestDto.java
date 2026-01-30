package com.back.product.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionCreateRequestDto(
        @NotEmpty(message = "Options cannot be null")
        @Valid
        List<OptionDto> options
) {
    @Builder
    public record OptionDto(
            @NotBlank(message = "Option Group Name cannot be blank")
            @Pattern(regexp = "^[a-z]{1,20}$", message = "Option Group Name is permitted only lower case english")
            String group,

            @NotEmpty(message = "Option Values cannot be null")
            @Valid
            List<@Pattern(regexp = "[a-zA-Z0-9]{1,20}$", message = "Option values are permitted only english and number") String> values
    ) {
    }
}
