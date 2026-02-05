package com.back.market.app;

import com.back.detector.annotation.CheckBidSpam;
import com.back.market.app.usecase.CancelBidUseCase;
import com.back.market.app.usecase.GetInstantPriceUseCase;
import com.back.market.app.usecase.MatchInstantTradeUseCase;
import com.back.market.app.usecase.RegisterBidUseCase;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.market.event.OrderCompletedEvent;
import com.back.market.app.usecase.*;
import com.back.market.domain.Order;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.response.InstantBuyPriceResponseDto;
import com.back.market.dto.response.InstantSellPriceResponseDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import com.back.market.dto.response.MarketPaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarketFacade {
    private final RegisterBidUseCase registerBidUseCase;
    private final GetInstantPriceUseCase getInstantPriceUseCase;
    private final MatchInstantTradeUseCase matchInstantTradeUseCase;
    private final CancelBidUseCase cancelBidUseCase;
    private final CompleteOrderUseCase completeOrderUseCase;

    @Value("${custom.kafka.topic.order-completed}")
    private String orderCompletedTopic;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * MARKET-010: 구매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return PayAndHoldResponseDto (결제/홀딩 상태 포함)
     */
    @CheckBidSpam
    @Transactional
    public MarketPaymentResponseDto registerBuyBid(Long userId, BiddingRequestDto requestDto) {
        return registerBidUseCase.registerBuyBid(userId, requestDto);
    }

    /**
     * MARKET-012: 판매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return PayAndHoldResponseDto (결제 불필요, PAID 상태)
     */
    @CheckBidSpam
    @Transactional
    public MarketPaymentResponseDto registerSellBid(Long userId, BiddingRequestDto requestDto) {
        return registerBidUseCase.registerSellBid(userId, requestDto);
    }

    /**
     * MARKET-004: 즉시 구매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantBuyPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantBuyPriceResponseDto getBuyNowPrice(Long productId) {
        return getInstantPriceUseCase.getBuyNowPrice(productId);
    }

    /**
     * MARKET-005: 즉시 판매가 조회
     * @param productId 조회할 상품 ID
     * @return InstantSellPriceResponseDto
     */
    @Transactional(readOnly = true)
    public InstantSellPriceResponseDto getSellNowPrice(Long productId) {
        return getInstantPriceUseCase.getSellNowPrice(productId);
    }

    /**
     * MARKET-009: 즉시 구매
     * @param buyerId 구매자 ID
     * @param requestDto BiddingRequestDto
     * @return 생성된 주문(Order)의 ID
     */
    @CheckBidSpam
    public MarketPaymentResponseDto purchaseNow(Long buyerId, BiddingRequestDto requestDto) {
        return matchInstantTradeUseCase.buyNow(buyerId, requestDto);
    }

    /**
     * MARKET-011: 즉시 판매
     * @param sellerId 판매자 ID
     * @param requestDto BiddingRequestDto
     * @return 생성된 주문(Order)의 ID
     */
    @CheckBidSpam
    public MarketPaymentResponseDto sellNow(Long sellerId, BiddingRequestDto requestDto) {
        return matchInstantTradeUseCase.sellNow(sellerId, requestDto);
    }

    /**
     * MARKET-013: 입찰 취소
     * @param userId 사용자ID
     * @param biddingId 입찰ID
     * @return PaymentCancelResponseDto
     */
    public PaymentCancelResponseDto cancelBid(Long userId, Long biddingId) {
        return cancelBidUseCase.cancelBid(userId, biddingId);
    }

    /**
     * MARKET: 구매 확정 + 정산 모듈에 이벤트 전송
     * @param userId 사용자ID
     * @param orderId 주문ID
     */
    public void completeOrder(Long userId, Long orderId) {

        Order order = completeOrderUseCase.completeOrder(userId, orderId);

        OrderCompletedEvent event = new OrderCompletedEvent(
                order.getId(),
                order.getSellBidding().getMarketProduct().getId(),
                order.getBuyBidding().getMarketUser().getId(),
                order.getSellBidding().getMarketUser().getId(),
                order.getSellBidding().getMarketUser().getName(),
                order.getPrice(),
                LocalDateTime.now()
        );
        kafkaEventPublisher.publish(orderCompletedTopic, event);
    }
}
