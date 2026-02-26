package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PayoutRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payout;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PayoutStatus;
import com.back.cash.domain.event.CashPayoutRequestedCommand;
import com.back.cash.domain.event.PayoutFailedEvent;
import com.back.cash.mapper.PayoutMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashPayoutUseCase {
    private final PayoutRepository payoutRepository;
    private final WalletSupport walletSupport;
    private final CashLogSupport cashLogSupport;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(CashPayoutRequestedCommand command) {

        // 금액 검증
        validateAmounts(command);

        if (payoutRepository.existsBySettlementId(command.settlementId())) {
            log.info("중복 정산 요청 - settlementId={}", command.settlementId());
            return;
        }

        Payout payout = payoutRepository.save(PayoutMapper.toPayout(command));

        // 지갑 입금
        Wallet wallet = walletSupport.getUserWallet(command.payeeId());

        wallet.deposit(command.totalNetAmount());

        Wallet systemWallet = walletSupport.getSystemWallet();
        systemWallet.deposit(command.totalFeeAmount());

        // 로그 기록
        cashLogSupport.recordSettlementPayoutLog(wallet, command.totalNetAmount(), command.settlementId());
        cashLogSupport.recordSystemSettlementPayoutLog(systemWallet, command.totalFeeAmount(), command.settlementId());

        payout.markDone();
    }


    private void validateAmounts(CashPayoutRequestedCommand command) {
        BigDecimal gross = command.totalGrossAmount();
        BigDecimal net = command.totalNetAmount();
        BigDecimal fee = command.totalFeeAmount();

        if (isInvalid(gross) || isInvalid(net) || isInvalid(fee)) {
            throw new BadRequestException(FailureCode.INVALID_AMOUNT);
        }

        BigDecimal calculatedGross = net.add(fee);

        if (gross.compareTo(calculatedGross) != 0) {
            log.warn("정산 금액 불일치 - 검증대상(gross): {}, 기대합계(net+fee): {} [net: {}, fee: {}]",
                    gross, calculatedGross, net, fee);
            throw new BadRequestException(FailureCode.INVALID_AMOUNT);
        }
    }

    private boolean isInvalid(BigDecimal amount) {
        return amount == null || amount.signum() < 0;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedPayout(CashPayoutRequestedCommand event, String reason) {
        try {
            Payout payout = payoutRepository.findBySettlementId(event.settlementId())
                    .orElseGet(() -> payoutRepository.save(PayoutMapper.toPayout(event)));

            if (payout.getStatus() == PayoutStatus.DONE) {
                return;
            }

            payout.markFailed(reason);
            eventPublisher.publishEvent(new PayoutFailedEvent(event.settlementId(), reason));
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 insert한 경우 재조회 후 상태 판단
            payoutRepository.findBySettlementId(event.settlementId())
                    .ifPresent(existing -> {
                        if (existing.getStatus() != PayoutStatus.DONE) {
                            existing.markFailed(reason);
                            eventPublisher.publishEvent(new PayoutFailedEvent(event.settlementId(), reason));
                        }
                    });
        }
    }

}

