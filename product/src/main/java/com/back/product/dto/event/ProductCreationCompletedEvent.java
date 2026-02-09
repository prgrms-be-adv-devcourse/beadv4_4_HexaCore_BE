package com.back.product.dto.event;

import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;

import java.util.List;

public record ProductCreationCompletedEvent(
        ProductInfoDto productInfoDto,
        List<OptionDto> optionDtos,
        String thumbnailUrl
) {
}
