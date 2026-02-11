package com.back.product.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionValueModifyResponseDto(
        @NotNull(message = "옵션 값 정보는 필수입니다.")
        @Valid
        OptionValueDto value
) {
    @Builder
    public record OptionValueDto(
            @NotNull(message = "옵션 값 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 값 ID는 1 이상의 정수여야 합니다.")
            Long id,

            @NotNull(message = "옵션 그룹 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 그룹 ID는 1 이상의 정수여야 합니다.")
            Long optionGroupId,

            @NotBlank(message = "옵션 값은 필수입니다.")
            @Pattern(regexp = "[a-zA-Z0-9]{1,20}$", message = "옵션 값은 영문과 숫자만 허용됩니다.")
            String value,

            @NotNull(message = "수정일시는 필수입니다.")
            LocalDateTime updatedAt
    ) {
    }
}
