package com.back.product.event.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionPayload(
        @NotNull(message = "옵션 그룹은 필수입니다.")
        @Valid
        GroupPayload group,

        @NotEmpty(message = "옵션 값은 하나 이상이어야 합니다.")
        @Valid
        List<ValuePayload> values
) {
    @Builder
    public record GroupPayload(
            @NotNull(message = "옵션 그룹 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 그룹 ID는 1 이상의 정수여야 합니다.")
            Long groupId,

            @NotBlank(message = "옵션 그룹 이름은 필수입니다.")
            @Pattern(regexp = "^[a-z]{1,20}$", message = "옵션 그룹 이름은 소문자 영어만 허용됩니다.")
            String groupName
    ) {
    }

    @Builder
    public record ValuePayload(
            @NotNull(message = "옵션 값 ID는 필수입니다.")
            @Min(value = 1, message = "옵션 값 ID는 1 이상의 정수여야 합니다.")
            Long valueId,

            @NotBlank(message = "옵션 값 이름은 필수입니다.")
            @Pattern(regexp = "[a-zA-Z0-9]{1,20}$", message = "옵션 값 이름은 영문과 숫자만 허용됩니다.")
            String valueName
    ) {
    }
}
