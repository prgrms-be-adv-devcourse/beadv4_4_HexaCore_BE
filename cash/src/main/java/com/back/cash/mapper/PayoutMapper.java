package com.back.cash.mapper;

import com.back.cash.app.event.CashPayoutRequestedPayload;
import com.back.cash.domain.Payout;
import com.back.cash.domain.enums.PayoutStatus;
import com.back.cash.domain.event.CashPayoutRequestedCommand;

public class PayoutMapper {
    public static Payout toPayout(CashPayoutRequestedCommand command) {
        return Payout.builder()
                .settlementId(command.settlementId())
                .payeeId(command.payeeId())
                .totalGrossAmount(command.totalGrossAmount())
                .totalNetAmount(command.totalNetAmount())
                .totalFeeAmount(command.totalFeeAmount())
                .status(PayoutStatus.PROCESSING)
                .build();
    }

    public static CashPayoutRequestedCommand toCashPayoutRequestedCommand(CashPayoutRequestedPayload payload) {
        return CashPayoutRequestedCommand.builder()
                .payeeId(payload.payeeId())
                .settlementId(payload.settlementId())
                .totalFeeAmount(payload.totalFeeAmount())
                .totalGrossAmount(payload.totalGrossAmount())
                .totalNetAmount(payload.totalNetAmount())
                .build();
    }
}
