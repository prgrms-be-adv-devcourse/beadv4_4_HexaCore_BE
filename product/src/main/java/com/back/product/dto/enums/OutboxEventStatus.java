package com.back.product.dto.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum OutboxEventStatus {
    INIT,
    SUCCEEDED,
    FAILED,
    PROCESSING,
    DEAD
}
