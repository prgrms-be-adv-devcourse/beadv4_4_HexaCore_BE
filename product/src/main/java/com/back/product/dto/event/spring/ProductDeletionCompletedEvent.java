package com.back.product.dto.event.spring;

import lombok.Builder;

@Builder
public record ProductDeletionCompletedEvent(
        Long productInfoId
) {
}
