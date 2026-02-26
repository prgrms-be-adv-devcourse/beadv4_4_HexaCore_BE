package com.back.product.event.spring;

import lombok.Builder;

@Builder
public record ProductResyncRequestEvent(
        Long productInfoId
) {
}
