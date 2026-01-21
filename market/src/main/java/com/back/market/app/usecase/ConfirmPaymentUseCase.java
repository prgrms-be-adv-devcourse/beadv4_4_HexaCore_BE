package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.domain.Bidding;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingStatus;
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
    private final BiddingRepository biddingRepository;

    /**
     * Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정 or 입찰 상태를 PROCESS로 변경
     * @param requestDto PaymentCompletedRequestDto
     */
    @Transactional
    public void confirmPayment(PaymentCompletedRequestDto requestDto) {
        log.info("[Market(Internal)] 결제 완료 통지 수신 - Type: {}, Id: {}, Amount: {}", requestDto.relType(), requestDto.relId(), requestDto.totalAmount());
        if (requestDto.relType() == RelType.ORDER) {
            processOrderPayment(requestDto);
        } else if (requestDto.relType() == RelType.BIDDING) {
            processBiddingPayment(requestDto);
        }
    }

    /**
     * 주문 조회 및 금액 검증 후 주문 상태를 변경하는 메서드
     * @param requestDto PaymentCompletedRequestDto
     */
    private void processOrderPayment(PaymentCompletedRequestDto requestDto) {
        // 1. 주문 조회
        Order order = orderRepository.findById(requestDto.relId()).orElseThrow(() -> new BadRequestException(FailureCode.ORDER_NOT_FOUND));

        // 2. 금액 검증 (DB가격 & totalAmount)
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

    /**
     * 구매 입찰 조회 및 금액 검증 후 입찰 상태를 변경하는 메서드
     * @param requestDto PaymentCompletedRequestDto
     */
    private void processBiddingPayment(PaymentCompletedRequestDto requestDto) {
        // 1. 입찰 조회
        Bidding bidding = biddingRepository.findById(requestDto.relId())
                .orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

        // 2. 금액 검증 (입찰희망가 vs 결제된 금액)
        if (bidding.getPrice().compareTo(requestDto.totalAmount()) != 0) {
            log.error("[Market(Internal)] 입찰 보증금 금액 불일치 - BidPrice: {}, PaidAmount: {}", bidding.getPrice(), requestDto.totalAmount());
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        // 3. 입찰 상태 변경 (HOLD -> PROCESS)
        // HOLD: 예치금 결제 대기 상태 -> PROCESS: 예치금 납부 완료, 정식 입찰 등록 상태
        if (bidding.getStatus() == BiddingStatus.HOLD) {
            bidding.changeStatus(BiddingStatus.PROCESS);
            log.info("[Market] 구매 입찰 등록 완료 (HOLD -> PROCESS) - BiddingId: {}", bidding.getId());
        } else if (bidding.getStatus() == BiddingStatus.PROCESS) {
            log.info("[Market] 이미 등록된 입찰입니다.");
        } else {
            log.warn("[Market] 입찰 처리가 불가능한 상태입니다. Status: {}", bidding.getStatus());
        }
    }
}
