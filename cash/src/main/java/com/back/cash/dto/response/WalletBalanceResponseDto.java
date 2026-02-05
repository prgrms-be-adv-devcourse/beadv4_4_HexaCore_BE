package com.back.cash.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletBalanceResponseDto(
        Long walletId,
        BigDecimal balance,
        LocalDateTime lastModifiedAt
) {
}
