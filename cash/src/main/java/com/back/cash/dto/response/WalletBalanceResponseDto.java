package com.back.cash.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record WalletBalanceResponseDto(
        Long walletId,
        BigDecimal balance,
        LocalDateTime lastModifiedAt
) {
}
