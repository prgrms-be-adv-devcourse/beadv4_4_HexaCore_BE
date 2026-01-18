package com.back.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BrandCreateRequestDto(
        @NotBlank(message = "브랜드 이름은 필수입니다.")
        @Size(min=1, max=50, message = "브랜드 이름은 1자 이상 50자 이하여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9 .&_+-]+$", message = "브랜드명은 영문, 숫자, 공백 및 일부 특수문자(.&_+-만) 허용됩니다.")
        String name,

        @NotBlank(message = "로고 URL은 필수입니다.")
        @Size(min=1, max=255, message = "로고 URL은 1자 이상 255자 이하여야 합니다.")
        String logoUrl
) {
}
