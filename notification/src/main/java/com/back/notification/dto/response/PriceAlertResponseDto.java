package com.back.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertResponseDto {
    private Long id;
    private Long productId;
    private BigDecimal targetPrice;
    private LocalDateTime triggeredAt;
    private LocalDateTime createdAt;
}
