package com.back.common.dto.settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 정산 완료 후 캐시 지급 요청. Settlement 모듈 → Cash 모듈로 전달
public record SettlementPayoutRequest(
        Long settlementId,
        Long payeeId,
        BigDecimal amount,
        LocalDateTime completedAt
) {
}
