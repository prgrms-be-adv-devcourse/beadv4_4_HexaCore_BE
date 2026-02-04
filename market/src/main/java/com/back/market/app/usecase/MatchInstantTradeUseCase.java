package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.exception.BadRequestException;
import com.back.common.market.event.BiddingCompletedEvent;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketUser;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.domain.enums.OrderStatus;
import com.back.common.dto.cash.enums.PayAndHoldStatus;
import com.back.common.dto.cash.enums.RelType;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.market.dto.response.MarketPaymentResponseDto;
import com.back.market.mapper.BiddingMapper;
import com.back.market.mapper.CashRequestMapper;
import com.back.market.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchInstantTradeUseCase {
    private final BiddingRepository biddingRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final MarketUserRepository marketUserRepository;
    private final BiddingMapper biddingMapper;
    private final CashRequestMapper cashRequestMapper;
    private final MarketSupport marketSupport;
    private final KafkaEventPublisher kafkaEventPublisher;

    /**
     * MARKET-009 즉시 구매 실행
     * @param buyerId 구매자 ID
     * @param requestDto BiddingRequestDto
     * @return 결제/홀딩 결과 DTO
     * @throws BadRequestException 해당 상품의 판매 입찰(매물)이 존재하지 않을 경우 (BIDDING_NOT_FOUND)
     */
    @Transactional
    public MarketPaymentResponseDto buyNow(Long buyerId, BiddingRequestDto requestDto) {
        Bidding targetSellBid = biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(requestDto.productId(), BiddingPosition.SELL, BiddingStatus.PROCESS).orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

        // 정합성 검사 추가: 사용자가 화면에서 본 가격과 실제 조회된 가격이 다르면 예외 처리
        validatePriceMatch(targetSellBid, requestDto.price());

        return executeTrade(buyerId, requestDto, targetSellBid, BiddingPosition.BUY);
    }

    /**
     * MARKET-011 즉시 판매 실행
     * @param sellerId 판매자 ID
     * @param requestDto BiddingRequestDto
     * @return 결제/홀딩 결과 DTO
     * @throws BadRequestException 해당 상품의 판매 입찰(매물)이 존재하지 않을 경우 (BIDDING_NOT_FOUND)
     */
    @Transactional
    public MarketPaymentResponseDto sellNow(Long sellerId, BiddingRequestDto requestDto) {
        Bidding targetBuyBid = biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(
                        requestDto.productId(), BiddingPosition.BUY, BiddingStatus.PROCESS)
                .orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

        // 정합성 검사 추가: 사용자가 화면에서 본 가격과 실제 조회된 가격이 다르면 예외 처리
        validatePriceMatch(targetBuyBid, requestDto.price());

        return executeTrade(sellerId, requestDto, targetBuyBid, BiddingPosition.SELL);
    }

    private void validatePriceMatch(Bidding bidding, BigDecimal requestPrice) {
        if(bidding.getPrice().compareTo(requestPrice) != 0) {
            log.warn("[Price Mismatch] 가격 변동 감지 - 요청가: {}, 실가: {}, BiddingId: {}", requestPrice, bidding.getPrice(), bidding.getId());
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }
    }

    /**
     * 실제 체결 및 주문 생성 공통 로직
     * <p>
     * 1. 자전거래 여부 검증 (본인의 입찰과 체결되는지 확인)
     * 2. 요청자의 신규 입찰 생성 및 상태 변경 (MATCHED)
     * 3. 매칭된 상대방 입찰의 상태 변경 (MATCHED)
     * 4. 배송지 정보를 포함한 최종 주문(Order) 생성
     * </p>
     */
    private MarketPaymentResponseDto executeTrade(Long userId, BiddingRequestDto requestDto, Bidding targetBid, BiddingPosition myPosition) {
        // 1. 자전거래 검증
        if(targetBid.getMarketUser().getId().equals(userId)){
            throw new BadRequestException(FailureCode.SELF_TRADING_NOT_ALLOWED);
        }

        // 2. 입찰 생성 및 상태 변경
        MarketUser me = marketUserRepository.findById(userId).orElseThrow(() -> new BadRequestException(FailureCode.USER_NOT_FOUND));

        Bidding myBid = biddingMapper.toEntity(requestDto, me, targetBid.getMarketProduct(), myPosition);
        myBid.changeStatus(BiddingStatus.MATCHED);
        biddingRepository.save(myBid);

        // 3. 상대방 입찰 상태 변경
        targetBid.changeStatus(BiddingStatus.MATCHED);

        // 4. 주문 생성
        Order order;

        if(myPosition == BiddingPosition.BUY) {
            order = orderMapper.toEntity(myBid, targetBid, me.getAddress());
        } else {
            order = orderMapper.toEntity(targetBid, myBid, targetBid.getMarketUser().getAddress());
        }
        Order savedOrder = orderRepository.save(order);

        // 5. 실제 결제 요청(FeignClient 사용)
        if(myPosition == BiddingPosition.BUY) {
            // Cash 호출
            PayAndHoldRequestDto paymentReq = cashRequestMapper.toPayAndHoldRequestForOrder(savedOrder);
            PayAndHoldResponseDto resultData = marketSupport.getPayAndHoldResult(paymentReq);

            // 6. 결과 상태에 따른 주문 상태 업데이트
            if (resultData.status() == PayAndHoldStatus.PAID) {
                // 결제 완료 -> 주문 상태 변경
                log.info("[MatchInstantTrade] 결제 완료 (PAID) - OrderId: {}", savedOrder.getId());
                savedOrder.changeStatus(OrderStatus.PAID);
                
                // TODO 즉시 구매 완료 이벤트 발행
                publishBiddingCompletedEvent(myBid, targetBid, BiddingPosition.BUY);
            } else if (resultData.status() == PayAndHoldStatus.REQUIRES_PG) {
                // PG 결제 필요 -> 주문은 대기 상태 유지 (HOLD)
                // (Order 생성 시 기본값이 HOLD이므로 별도 상태 변경 불필요)
                log.info("[MatchInstantTrade] PG 결제 필요 (REQUIRES_PG) - OrderId: {}, TossId: {}", savedOrder.getId(), resultData.tossOrderId());
            }
            return MarketPaymentResponseDto.from(
                    resultData,
                    targetBid.getMarketProduct().getName(), // 상품명
                    me.getNickname(),                       // 구매자 이름
                    me.getEmail()                           // 구매자 이메일
            );
        } else {
            // 내가 판매자라면 홀딩할 필요가 없음(이미 구매자가 홀딩한 금액 존재)
            log.info("[MatchTrade] 즉시 판매 체결 (결제 불필요) - OrderId: {}", savedOrder.getId());
            savedOrder.changeStatus(OrderStatus.PAID);
            
            // TODO 즉시 판매 완료 이벤트 발행
            publishBiddingCompletedEvent(targetBid, myBid, BiddingPosition.SELL);

            PayAndHoldResponseDto cashResponse = PayAndHoldResponseDto.of(
                    PayAndHoldStatus.PAID,
                    RelType.ORDER,
                    savedOrder.getId(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null
            );

            return MarketPaymentResponseDto.from(
                    cashResponse,
                    targetBid.getMarketProduct().getName(),
                    me.getNickname(),
                    me.getEmail()
            );
        }
    }

    /** TODO 시현님이 만들어 둔 부분 어떻게 옮길지 생각해보기
     * 입찰 체결 완료 이벤트 발행
     * @param buyBid 구매 입찰
     * @param sellBid 판매 입찰
     * @param triggerPosition 어떤 포지션(구매/판매)에서 트리거되었는지
     */
    private void publishBiddingCompletedEvent(Bidding buyBid, Bidding sellBid, BiddingPosition triggerPosition) {
        BiddingCompletedEvent event = new BiddingCompletedEvent(
                triggerPosition == BiddingPosition.BUY ? buyBid.getId() : sellBid.getId(),
                buyBid.getMarketUser().getId(),
                sellBid.getMarketUser().getId(),
                buyBid.getMarketProduct().getId(),
                buyBid.getMarketProduct().getName(),
                buyBid.getMarketProduct().getProductOption(),
                buyBid.getMarketProduct().getThumbnailImage(),
                buyBid.getMarketProduct().getBrandName(),
                buyBid.getPrice(),
                triggerPosition.name()
        );
        
        kafkaEventPublisher.publish(event);
    }


}
