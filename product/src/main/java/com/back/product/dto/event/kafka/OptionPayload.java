package com.back.product.dto.event.kafka;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionPayload(
        GroupPayload group,
        List<ValuePayload> values
) {
    @Builder
    public record GroupPayload(
            Long groupId,
            String groupName
    ) {
    }

    @Builder
    public record ValuePayload(
            Long valueId,
            String valueName
    ) {
    }
}
