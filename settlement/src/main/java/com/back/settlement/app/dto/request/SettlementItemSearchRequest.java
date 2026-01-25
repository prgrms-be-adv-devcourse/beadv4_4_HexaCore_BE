package com.back.settlement.app.dto.request;

import com.back.settlement.domain.SettlementItemStatus;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record SettlementItemSearchRequest(
        Long orderId,
        Long productId,
        SettlementItemStatus status,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime endDate
) {
}
