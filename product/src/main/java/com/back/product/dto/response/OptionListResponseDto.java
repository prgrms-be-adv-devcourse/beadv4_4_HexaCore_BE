package com.back.product.dto.response;

import com.back.product.dto.model.OptionDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionListResponseDto(
        @NotEmpty(message = "옵션 목록은 비어 있을 수 없습니다.")
        @Valid
        List<OptionDto> options
) {
}
