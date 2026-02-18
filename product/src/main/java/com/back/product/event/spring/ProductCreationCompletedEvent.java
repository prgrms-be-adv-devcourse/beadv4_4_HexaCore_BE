package com.back.product.event.spring;

import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Builder
public record ProductCreationCompletedEvent(
        @NotEmpty(message = "이벤트 아이디는 필수입니다.")
        String eventId,

        @NotNull(message = "상품 정보 DTO는 필수입니다.")
        @Valid
        ProductInfoDto productInfoDto,

        @NotEmpty(message = "옵션 DTO 목록은 비어 있을 수 없습니다.")
        @Valid
        List<OptionDto> optionDtos,

        @NotBlank(message = "썸네일 URL은 필수입니다.")
        @URL(message = "유효한 URL 형식이 아닙니다.")
        String thumbnailUrl
) {
}
