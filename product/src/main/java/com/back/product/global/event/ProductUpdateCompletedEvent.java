package com.back.product.global.event;

import com.back.product.dto.OptionDto;
import com.back.product.dto.ProductInfoDto;

import java.util.List;

public record ProductUpdateCompletedEvent(
        ProductInfoDto productInfoDto,
        List<OptionDto> optionDtos,
        String thumbnailUrl
) {
}
