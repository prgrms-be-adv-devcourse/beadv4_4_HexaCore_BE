package com.back.cash.dto.request;

import com.back.cash.domain.enums.RelType;

public record PaymentFailedRequestDto(
        RelType relType,
        Long relId
) {}
