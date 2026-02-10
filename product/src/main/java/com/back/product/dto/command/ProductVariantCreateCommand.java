package com.back.product.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductVariantCreateCommand(
        List<Long> optionValueIds,
        Long inventory,
        List<String> imageUrls
) {
}
