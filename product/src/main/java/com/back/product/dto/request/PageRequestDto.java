package com.back.product.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import org.hibernate.validator.constraints.Range;

@Builder
public record PageRequestDto(
        @Min(value = 0, message = "page는 0 이상의 값이어야 합니다.")
        Integer page,

        @Range(min = 5, max = 50, message = "size는 5에서 50 사이의 값이어야 합니다.")
        Integer size
) {
    // 기본값 설정 용 컴팩트 생성자
    public PageRequestDto {
        if (size == null) {
            size = 5;
        }

        if (page == null) {
            page = 0;
        }
    }
}
