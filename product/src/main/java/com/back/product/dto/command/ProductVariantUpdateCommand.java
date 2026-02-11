package com.back.product.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductVariantUpdateCommand(
        Long productId,
        List<Long> optionValueIds,
        Long inventory,
        List<String> imageUrls
) {
}
