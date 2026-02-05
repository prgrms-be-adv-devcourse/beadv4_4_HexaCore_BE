package com.back.common.product.event.payload;

import java.util.List;

public record OptionPayload(
        GroupPayload group,
        List<ValuePayload> values
) {
    public record GroupPayload(
            Long groupId,
            String groupName
    ) {
    }

    public record ValuePayload(
            Long valueId,
            String valueName
    ) {
    }
}
