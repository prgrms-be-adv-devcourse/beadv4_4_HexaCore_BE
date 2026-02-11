package com.back.product.dto.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionDto(
        @NotNull(message = "옵션 그룹은 필수입니다.")
        @Valid
        GroupDto group,

        @NotEmpty(message = "옵션 값은 하나 이상이어야 합니다.")
        @Valid
        List<ValueDto> values
) {
    @Builder
    public record GroupDto(
            @NotNull(message = "옵션 그룹 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 그룹 ID는 1 이상의 정수여야 합니다.")
            Long id,

            @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
            @Pattern(regexp = "^[a-z]{1,20}$", message = "옵션 그룹 이름은 소문자 영어만 허용됩니다.")
            String name
    ) {
    }


    @Builder
    public record ValueDto(
            @NotNull(message = "옵션 값 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 값 ID는 1 이상의 정수여야 합니다.")
            Long id,

            @NotBlank(message = "옵션 값 이름은 필수입니다.")
            @Pattern(regexp = "[a-zA-Z0-9]{1,20}$", message = "옵션 값 이름은 영문과 숫자만 허용됩니다.")
            String name
    ) {
    }
}
