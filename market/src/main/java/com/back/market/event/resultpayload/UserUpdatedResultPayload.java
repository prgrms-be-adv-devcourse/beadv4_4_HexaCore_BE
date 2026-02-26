package com.back.market.event.resultpayload;

import com.back.common.event.EventName;

public record UserUpdatedResultPayload(
        Long id,
        String name,
        String address,
        String phone
) implements EventName {
    public static UserUpdatedResultPayload of(Long id, String name, String address, String phone) {
        return new UserUpdatedResultPayload(id, name, address, phone);
    }
}