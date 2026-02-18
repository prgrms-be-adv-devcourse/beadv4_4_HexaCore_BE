package com.back.product.event.kafka;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductInfoPayload(
        @NotNull(message = "상품 정보 ID는 필수입니다.")
        @Min(value = 1, message = "상품 정보 ID는 1 이상의 정수여야 합니다.")
        Long productInfoId,

        @NotNull(message = "브랜드 정보 페이로드는 필수입니다.")
        @Valid
        BrandPayload brand,

        @NotNull(message = "카테고리 정보 페이로드는 필수입니다.")
        @Valid
        CategoryPayload category,

        @NotBlank(message = "상품 이름은 필수입니다.")
        @Pattern(regexp = "^[A-Za-z0-9 ]{1,50}$", message = "상품 이름은 영문, 숫자, 공백만 허용되며 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "상품 코드는 필수입니다.")
        @Size(min = 1, max = 50, message = "상품 코드는 1자 이상 50자 이하여야 합니다.")
        String code,

        @NotNull(message = "출시 가격은 필수입니다.")
        @DecimalMin(value = "1.00", message = "출시 가격은 1 이상이어야 합니다.")
        BigDecimal releasePrice,

        @NotNull(message = "출시일은 필수입니다.")
        LocalDateTime releaseDate
) {
}
