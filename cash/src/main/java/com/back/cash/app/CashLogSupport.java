package com.back.cash.app;

import com.back.cash.domain.CashLog;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.RelType;
import com.back.cash.domain.enums.Type;
import com.back.cash.adapter.out.CashLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CashLogSupport {
    private final CashLogRepository cashLogRepository;

    public void recordHoldingLog(
            Wallet buyerWallet,
            Wallet systemWallet,
            BigDecimal amount,
            RelType relType,
            Long relId
    ) {
        // 구매자 지갑 로그 (돈이 나감 -> 음수)
        CashLog buyerLog = CashLog.builder()
                .wallet(buyerWallet)
                .amount(amount.negate())
                .balance(buyerWallet.getBalance())
                .type(Type.HOLD)
                .relType(relType)
                .relId(relId)
                .build();

        // 시스템 지갑 로그 (돈이 들어옴 -> 양수)
        CashLog systemLog = CashLog.builder()
                .wallet(systemWallet)
                .amount(amount)
                .balance(systemWallet.getBalance())
                .type(Type.HOLD)
                .relType(relType)
                .relId(relId)
                .build();

        cashLogRepository.save(buyerLog);
        cashLogRepository.save(systemLog);
    }

    public void recordUserPgTopUpLog(
            Wallet buyerWallet,
            BigDecimal amount,
            RelType relType,
            Long relId
    ) {
        CashLog userTopUp = CashLog.builder()
                .wallet(buyerWallet)
                .amount(amount)
                .balance(buyerWallet.getBalance())
                .type(Type.TOPUP_PG)
                .relType(relType)
                .relId(relId)
                .build();

        cashLogRepository.save(userTopUp);
    }

    public void recordReleaseOnPaymentFail(Wallet buyerWallet, Wallet systemWallet,
                                           BigDecimal amount, RelType relType, Long relId) {
        recordRelease(buyerWallet, systemWallet, amount, relType, relId, Type.RELEASE_ON_FAIL);
    }

    public void recordCancelRefund(Wallet buyerWallet, Wallet systemWallet,
                                   BigDecimal amount, RelType relType, Long relId) {
        recordRelease(buyerWallet, systemWallet, amount, relType, relId, Type.REFUND_ON_CANCEL);
    }

    private void recordRelease(Wallet buyerWallet, Wallet systemWallet,
                               BigDecimal amount, RelType relType, Long relId, Type type) {
        CashLog buyerLog = CashLog.builder()
                .wallet(buyerWallet)
                .amount(amount)
                .balance(buyerWallet.getBalance())
                .type(type)
                .relType(relType)
                .relId(relId)
                .build();

        CashLog systemLog = CashLog.builder()
                .wallet(systemWallet)
                .amount(amount.negate())
                .balance(systemWallet.getBalance())
                .type(type)
                .relType(relType)
                .relId(relId)
                .build();

        cashLogRepository.save(buyerLog);
        cashLogRepository.save(systemLog);
    }
}
