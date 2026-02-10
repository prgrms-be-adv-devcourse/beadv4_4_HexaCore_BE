package com.back.product.dto.event.spring;

import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ProductCreationCompletedEvent(
        ProductInfoDto productInfoDto,
        List<OptionDto> optionDtos,
        String thumbnailUrl
) {
}
