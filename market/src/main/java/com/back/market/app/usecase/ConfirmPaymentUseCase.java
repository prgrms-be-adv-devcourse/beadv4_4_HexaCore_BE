package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Bidding;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.OrderStatus;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.market.event.payload.PaymentCompletedPayload;
import com.back.market.event.payload.PaymentFailedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmPaymentUseCase {

    private final MarketSupport marketSupport;

    /**
     * [feign] Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정 or 입찰 상태를 PROCESS로 변경
     * @param requestDto PaymentCompletedRequestDto
     * @return true: 정상 처리됨 / false: 이미 처리된 요청(중복)
     */
    @Transactional
    public boolean confirmPayment(PaymentCompletedRequestDto requestDto) {
        log.info("[Market(Internal)] HTTP 결제 완료 통지 수신 - Type: {}, Id: {}, Amount: {}", requestDto.relType(), requestDto.relId(), requestDto.totalAmount());
        return processPaymentLogic(requestDto.relType(), requestDto.relId(), requestDto.totalAmount());
    }

    /**
     * [kafka] Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정 or 입찰 상태를 PROCESS로 변경
     * @param payload PaymentCompletedPayload
     */
    @Transactional
    public void confirmPayment(PaymentCompletedPayload payload) {
        //log.info("[Market(Internal)] Kafka 결제 완료 이벤트 수신 - Type: {}, Id: {}, Amount: {}", payload.relType(), payload.relId(), payload.totalAmount());
        processPaymentLogic(payload.relType(), payload.relId(), payload.totalAmount());
    }

    /**
     * [kafka] Cash 모듈로부터 결제 실패 통지를 수신하여 주문 상태를 취소 or 입찰 상태를 취소로 변경
     * @param payload PaymentFailedPayload
     */
    @Transactional
    public void handlePaymentFailure(PaymentFailedPayload payload) {
        //log.info("[Market(Internal)] Kafka 결제 실패 이벤트 수신 - Type: {}, Id: {}", payload.relType(), payload.relId());
        if (payload.relType() == RelType.ORDER) {
            Order order = marketSupport.findOrderById(payload.relId());
            if (order.getOrderStatus() == OrderStatus.HOLD) {
                // 1. 주문 취소
                order.changeStatus(OrderStatus.CANCELLED_PAYMENT_FAILED);
                log.info("[Market] 결제 실패로 인한 주문 취소 완료 - OrderId: {}, status: {}", order.getId(), order.getOrderStatus());

                // 2. 구매자의 입찰 취소(결제 실패 책임)
                Bidding buyBidding = marketSupport.findBiddingById(order.getBuyBidding().getId());
                buyBidding.changeStatus(BiddingStatus.CANCELLED_PAYMENT_FAILED);
                log.info("[Market] 결제 실패로 인한 구매 입찰 취소 완료 - BuyBiddingId: {}, status: {}", buyBidding.getId(), buyBidding.getStatus());

                // 3. 판매자의 입찰 롤백
                Bidding sellBidding = marketSupport.findBiddingById(order.getSellBidding().getId());
                sellBidding.changeStatus(BiddingStatus.PROCESS);
                log.info("[Market] 결제 실패로 인한 판매 입찰 롤백 완료 - SellBiddingId: {}, status: {}", sellBidding.getId(), sellBidding.getStatus());

            } else {
                log.info("[Market] 이미 취소되었거나 결제 실패 처리가 불가능한 주문입니다. - OrderId: {}, status: {}", order.getId(), order.getOrderStatus());
            }
        } else if (payload.relType() == RelType.BIDDING) {
            Bidding bidding = marketSupport.findBiddingById(payload.relId());
            if (bidding.getStatus() == BiddingStatus.HOLD) {
                bidding.changeStatus(BiddingStatus.CANCELLED_PAYMENT_FAILED);
                log.info("[Market] 결제 실패로 인한 입찰 취소 완료 - BiddingId: {}, status: {}", bidding.getId(), bidding.getStatus());
            }
        }
    }

    private boolean processPaymentLogic(RelType relType, Long relId, BigDecimal totalAmount) {
        if(relType == RelType.ORDER) {
            return processOrderPayment(relId, totalAmount);
        } else if(relType == RelType.BIDDING) {
            return processBiddingPayment(relId, totalAmount);
        }
        return false;
    }

    /**
     * 주문 조회 및 금액 검증 후 주문 상태를 변경하는 메서드
     * @param relId
     * @param totalAmount
     * @return true: 정상 처리됨 / false: 이미 처리된 요청(중복)
     */
    private boolean processOrderPayment(Long relId, BigDecimal totalAmount) {
        // 1. 주문 조회
        Order order = marketSupport.findOrderById(relId);

        // 2. 금액 검증 (DB가격 & totalAmount)
        if (order.getPrice().compareTo(totalAmount) != 0) {
            log.error("[Market(Internal)] 결제 금액 불일치 - OrderPrice: {}, PaidAmount: {}", order.getPrice(), totalAmount);
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        // 3. 주문 상태 변경 (HOLD -> PAID)
        if (order.getOrderStatus() == OrderStatus.HOLD) {
            order.changeStatus(OrderStatus.PAID);
            log.info("[Market] 주문 상태 변경 완료 (HOLD -> PAID) - OrderId: {}", order.getId());
            return true;
        } else if (order.getOrderStatus() == OrderStatus.PAID) {
            log.info("[Market] 이미 결제 완료된 주문입니다.");
            return false;
        } else {
            log.warn("[Market] 결제 처리가 불가능한 상태입니다. Status: {}", order.getOrderStatus());
            return false;
        }
    }

    /**
     * 구매 입찰 조회 및 금액 검증 후 입찰 상태를 변경하는 메서드
     * @param relId
     * @param totalAmount
     * @return true: 정상 처리됨 / false: 이미 처리된 요청(중복)
     */
    private boolean processBiddingPayment(Long relId, BigDecimal totalAmount) {
        // 1. 입찰 조회
        Bidding bidding = marketSupport.findBiddingById(relId);

        // 2. 금액 검증 (입찰희망가 vs 결제된 금액)
        if (bidding.getPrice().compareTo(totalAmount) != 0) {
            log.error("[Market(Internal)] 입찰 보증금 금액 불일치 - BidPrice: {}, PaidAmount: {}", bidding.getPrice(), totalAmount);
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        // 3. 입찰 상태 변경 (HOLD -> PROCESS)
        // HOLD: 예치금 결제 대기 상태 -> PROCESS: 예치금 납부 완료, 정식 입찰 등록 상태
        if (bidding.getStatus() == BiddingStatus.HOLD) {
            bidding.changeStatus(BiddingStatus.PROCESS);
            log.info("[Market] 구매 입찰 등록 완료 (HOLD -> PROCESS) - BiddingId: {}", bidding.getId());
            return true;
        } else if (bidding.getStatus() == BiddingStatus.PROCESS) {
            log.info("[Market] 이미 등록된 입찰입니다.");
            return false;
        } else {
            log.warn("[Market] 입찰 처리가 불가능한 상태입니다. Status: {}", bidding.getStatus());
            return false;
        }
    }
}
