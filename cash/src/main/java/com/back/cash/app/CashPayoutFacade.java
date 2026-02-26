package com.back.cash.app;

import com.back.cash.adapter.out.PayoutRepository;
import com.back.cash.app.usecase.CashPayoutUseCase;
import com.back.cash.domain.event.CashPayoutRequestedCommand;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashPayoutFacade {
    private final CashPayoutUseCase cashPayoutUseCase;
    private final PayoutRepository payoutRepository;

    public void requestPayout(CashPayoutRequestedCommand command) {
        try {
            cashPayoutUseCase.execute(command);
        } catch (DataIntegrityViolationException e) {
            if (payoutRepository.existsBySettlementId(command.settlementId())) {
                log.info("이미 처리된 정산 요청입니다. settlementId={}", command.settlementId());
                return;
            }
            throw e; // 중복 아닌 db에러 -> 예외 던짐
        } catch (BadRequestException | EntityNotFoundException e) {
            log.error("정산 지급 실패. settlementId={}", command.settlementId(), e);
            cashPayoutUseCase.saveFailedPayout(command, e.getMessage());
        }
        // 나머지 Exception은 catch 하지 않고 예외 던짐
    }
}
