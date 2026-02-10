package com.back.product.dto.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Builder
public record ProductSearchDto(
        @NotNull(message = "상품 정보 ID는 필수입니다.")
        @Min(value = 1, message = "상품 정보 ID는 1 이상의 정수여야 합니다.")
        Long productInfoId,

        @NotBlank(message = "상품 이름은 필수입니다.")
        @Size(min = 1, max = 100, message = "상품 이름은 1자 이상 100자 이하여야 합니다.")
        String productName,

        @NotBlank(message = "썸네일 URL은 필수입니다.")
        @URL(message = "유효한 URL 형식이 아닙니다.")
        String thumbnailUrl,

        @NotBlank(message = "브랜드 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "브랜드 이름은 1자 이상 50자 이하여야 합니다.")
        String brandName,

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "카테고리 이름은 1자 이상 50자 이하여야 합니다.")
        String categoryName,

        @NotNull(message = "출시 가격은 필수입니다.")
        @DecimalMin(value = "0.00", message = "출시 가격은 0.00 이상이어야 합니다.")
        BigDecimal releasePrice
) {
}
