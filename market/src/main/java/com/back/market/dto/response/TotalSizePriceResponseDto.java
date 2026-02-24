package com.back.market.dto.response;

import java.util.List;

public record TotalSizePriceResponseDto(
        Long productInfoId,
        List<ProductSizePriceResponseDto> sizePriceList
) {
}
