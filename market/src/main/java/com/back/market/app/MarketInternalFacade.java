package com.back.market.app;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.common.user.event.UserCreatedEvent;
import com.back.market.app.usecase.ConfirmPaymentUseCase;
import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.market.app.usecase.CreateCartUseCase;
import com.back.market.app.usecase.CreateMarketMemberUseCase;
import com.back.market.app.usecase.GetSettlementDataUseCase;
import com.back.market.domain.MarketUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketInternalFacade {
    private final ConfirmPaymentUseCase confirmPaymentUseCase;
    private final GetSettlementDataUseCase getSettlementDataUseCase;
    private final CreateMarketMemberUseCase createMarketMemberUseCase;
    private final CreateCartUseCase createCartUseCase;

    /**
     * Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정
     * @param requestDto PaymentCompletedRequestDto
     */
    @Transactional
    public boolean confirmPayment(PaymentCompletedRequestDto requestDto) {
        return confirmPaymentUseCase.confirmPayment(requestDto);
    }

    /**
     * 주문 데이터 조회(feign)
     * @param targetDate 요청 날짜(월단위)
     * @param page 페이지
     * @param size 몇 건이나 보여줄지
     * @return SettlementTargetOrder
     */
    @Transactional(readOnly = true)
    public List<SettlementTargetOrder> getSettlementData(LocalDate targetDate, int page, int size) {
        return getSettlementDataUseCase.getSettlementData(targetDate, page, size);
    }

    /**
     * 회원 가입 이벤트 수신 시 market_member 복제와 cart 생성
     */
    @Transactional
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        // 회원 정보 복제
        MarketUser user = createMarketMemberUseCase.createMarketMember(event);

        // 해당 회원의 장바구니 생성
        if (user != null) {
            createCartUseCase.createCart(user);
        } else {
            log.warn("[MarketInternalFacade] market_member 복제 실패, cart 생성 건너뜀: userId = {}", event.id());
        }
    }
}
