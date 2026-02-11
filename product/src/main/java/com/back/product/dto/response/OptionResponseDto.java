package com.back.product.dto.response;

import com.back.product.dto.model.OptionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OptionResponseDto(
        @NotNull(message = "옵션 정보는 필수입니다.")
        @Valid
        OptionDto option
) {
}
