package com.back.settlement.app.dto.internal;

import com.back.common.dto.settlement.SettlementPayoutRequest;
import com.back.settlement.domain.Settlement;

public record SettlementWithPayout(
        Settlement settlement,
        SettlementPayoutRequest payoutRequest
) {
}
