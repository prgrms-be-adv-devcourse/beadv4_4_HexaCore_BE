package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PayoutRepository;
import com.back.cash.adapter.out.WalletRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.domain.Payout;
import com.back.cash.domain.Wallet;
import com.back.cash.dto.request.SettlementPayoutRequest;
import com.back.cash.mapper.PayoutMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashPayoutUseCase {
    private final PayoutRepository payoutRepository;
    private final WalletRepository walletRepository;
    private final CashLogSupport cashLogSupport;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(SettlementPayoutRequest req) {
        // 금액 검증
        if (req.amount() == null || req.amount().signum() <= 0) {
            throw new BadRequestException(FailureCode.INVALID_AMOUNT);
        }

        if (payoutRepository.existsBySettlementId(req.settlementId())) {
            log.info("중복 정산 요청 - settlementId={}", req.settlementId());
            return;
        }

        Payout payout;
        try {
            payout = payoutRepository.save(
                    PayoutMapper.toPayout(req.settlementId(), req.payeeId(), req.amount(), req.completedAt())
            );

        } catch (DataIntegrityViolationException e) {
            log.warn("중복 정산 스킵 - settlementId: {}", req.settlementId());
            return;
        }

        try {
            // 지갑 입금
            Wallet wallet = walletRepository.findByUserId(req.payeeId())
                    .orElseThrow(() -> new EntityNotFoundException(FailureCode.WALLET_NOT_FOUND));
            wallet.deposit(req.amount());

            // 로그 기록
            cashLogSupport.recordSettlementPayoutLog(wallet, req.amount(), req.settlementId());

            payout.markDone();

        } catch (Exception ex) {
            payout.markFailed(ex.getMessage());
            throw ex;
        }
    }
}

