package com.back.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoryCreateRequestDto(
        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(min=1, max=50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "카테고리 명은 영문만 허용됩니다.")
        String name,

        @NotBlank(message = "이미지 URL은 필수입니다.")
        @Size(min=1, max=255, message = "이미지 URL은 1자 이상 255자 이하여야 합니다.")
        String imageUrl
) {
}
