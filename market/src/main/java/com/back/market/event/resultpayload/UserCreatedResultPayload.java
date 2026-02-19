package com.back.market.event.resultpayload;

import com.back.common.event.EventName;

/**
 * 이벤트 수신 시 사용
 * @param id
 * @param name
 * @param email
 * @param address
 * @param phone
 */
public record UserCreatedResultPayload(
        Long id,
        String name,
        String email,
        String address,
        String phone
) implements EventName {
    public static UserCreatedResultPayload of(Long id, String name, String email, String address, String phone) {
        return new UserCreatedResultPayload(id, name, email, address, phone);
    }
}
