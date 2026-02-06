package com.back.common.product.event;

import com.back.common.event.EventName;

public record ProductDeletedEvent(
        Long productInfoId
) implements EventName {
}
