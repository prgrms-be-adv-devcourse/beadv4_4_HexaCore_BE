package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.domain.Order;
import com.back.market.domain.enums.OrderStatus;
import com.back.market.dto.enums.RelType;
import com.back.market.dto.request.PaymentCompletedRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final OrderRepository orderRepository;

    /**
     * Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정
     * @param requestDto PaymentCompletedRequestDto
     */
    @Transactional
    public void confirmPayment(PaymentCompletedRequestDto requestDto) {
        log.info("[Market(Internal)] 결제 완료 통지 수신 - Type: {}, Id: {}, Amount: {}", requestDto.relType(), requestDto.relId(), requestDto.totalAmount());
        if (requestDto.relType() == RelType.ORDER) {
            processOrderPayment(requestDto);
        }
    }

    /**
     * 주문 조회 및 금액 검증 후 주문 상태를 변경하는 메서드
     * @param requestDto PaymentCompletedRequestDto
     */
    private void processOrderPayment(PaymentCompletedRequestDto requestDto) {
        // 1. 주문 조회
        Order order = orderRepository.findById(requestDto.relId()).orElseThrow(() -> new BadRequestException(FailureCode.ORDER_NOT_FOUND));

        // 2. 금액 검증(DB가격 & totalAmount)
        if (order.getPrice().compareTo(requestDto.totalAmount()) != 0) {
            log.error("[Market(Internal)] 결제 금액 불일치 - OrderPrice: {}, PaidAmount: {}", order.getPrice(), requestDto.totalAmount());
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        // 3. 주문 상태 변경 (HOLD -> PAID)
        if (order.getOrderStatus() == OrderStatus.HOLD) {
            order.changeStatus(OrderStatus.PAID);
            log.info("[Market] 주문 상태 변경 완료 (HOLD -> PAID) - OrderId: {}", order.getId());
        } else if (order.getOrderStatus() == OrderStatus.PAID) {
            log.info("[Market] 이미 결제 완료된 주문입니다.");
        } else {
            log.warn("[Market] 결제 처리가 불가능한 상태입니다. Status: {}", order.getOrderStatus());
        }

    }
}
