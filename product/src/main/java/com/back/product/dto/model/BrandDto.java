package com.back.product.dto.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

@Builder
public record BrandDto(
        @NotNull(message = "브랜드 ID는 필수입니다.")
        @Min(value = 1, message = "브랜드 ID는 1 이상의 정수여야 합니다.")
        Long brandId,

        @NotBlank(message = "브랜드 이름은 필수입니다.")
        @Size(min=1, max=50, message = "브랜드 이름은 1자 이상 50자 이하여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9 .&_+-]+$", message = "브랜드명은 영문, 숫자, 공백 및 일부 특수문자(.&_+-만) 허용됩니다.")
        String name,

        @NotBlank(message = "로고 URL은 필수입니다.")
        @Size(min=1, max=255, message = "로고 URL은 1자 이상 255자 이하여야 합니다.")
        @URL(message = "유효한 URL 형식이 아닙니다.")
        String imageUrl
) {
}
