package com.back.product.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OptionGroupModifyResponseDto(
        @NotNull(message = "옵션 그룹 정보는 필수입니다.")
        @Valid
        OptionGroupDto group
) {
    @Builder
    public record OptionGroupDto(
            @NotNull(message = "옵션 그룹 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 그룹 ID는 1 이상의 정수여야 합니다.")
            Long id,

            @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
            @Pattern(regexp = "^[a-z]{1,20}$", message = "옵션 그룹 이름은 소문자 영어만 허용됩니다.")
            String name,

            @NotNull(message = "수정일시는 필수입니다.")
            LocalDateTime updatedAt
    ) {
    }
}
