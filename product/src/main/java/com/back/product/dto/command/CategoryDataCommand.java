package com.back.product.dto.command;

import lombok.Builder;

@Builder
public record CategoryDataCommand(
        String name,
        String imageUrl
) {
}
