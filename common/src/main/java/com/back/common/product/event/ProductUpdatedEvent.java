package com.back.common.product.event;

import com.back.common.event.EventName;
import com.back.common.product.event.payload.OptionPayload;
import com.back.common.product.event.payload.ProductInfoPayload;

import java.util.List;

public record ProductUpdatedEvent(
        ProductInfoPayload productInfo,
        List<OptionPayload> options,
        String thumbnailUrl
) implements EventName {
}
