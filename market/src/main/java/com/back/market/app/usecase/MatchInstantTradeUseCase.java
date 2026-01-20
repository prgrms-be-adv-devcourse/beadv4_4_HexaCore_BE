package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.code.SuccessCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.adapter.out.OrderRepository;
import com.back.market.adapter.out.client.FakeCashClient;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketUser;
import com.back.market.domain.Order;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.enums.RelType;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.CashApiResponse;
import com.back.market.dto.response.CashHoldResponseDto;
import com.back.market.mapper.BiddingMapper;
import com.back.market.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchInstantTradeUseCase {
    private final BiddingRepository biddingRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final MarketUserRepository marketUserRepository;
    private final BiddingMapper biddingMapper;
    private final FakeCashClient fakeCashClient;

    /**
     * MARKET-009 즉시 구매 실행
     * @param buyerId 구매자 ID
     * @param requestDto BiddingRequestDto
     * @return 생성된 주문(Order)의 ID
     * @throws BadRequestException 해당 상품의 판매 입찰(매물)이 존재하지 않을 경우 (BIDDING_NOT_FOUND)
     */
    @Transactional
    public Long buyNow(Long buyerId, BiddingRequestDto requestDto) {
        Bidding targetSellBid = biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(requestDto.productId(), BiddingPosition.SELL, BiddingStatus.PROCESS).orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

        return executeTrade(buyerId, requestDto, targetSellBid, BiddingPosition.BUY);
    }

    /**
     * MARKET-011 즉시 판매 실행
     * @param sellerId 판매자 ID
     * @param requestDto BiddingRequestDto
     * @return 생성된 주문(Order)의 ID
     * @throws BadRequestException 해당 상품의 판매 입찰(매물)이 존재하지 않을 경우 (BIDDING_NOT_FOUND)
     */
    @Transactional
    public Long sellNow(Long sellerId, BiddingRequestDto requestDto) {
        Bidding targetBuyBid = biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(
                        requestDto.productId(), BiddingPosition.BUY, BiddingStatus.PROCESS)
                .orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

        return executeTrade(sellerId, requestDto, targetBuyBid, BiddingPosition.SELL);
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
    private Long executeTrade(Long userId, BiddingRequestDto requestDto, Bidding targetBid, BiddingPosition myPosition) {
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
        orderRepository.save(order);

        // 5. 실제 결제 요청(fakecashclient 사용)
        PayAndHoldRequestDto paymentReq = PayAndHoldRequestDto.of(
                order.getBuyBidding().getMarketUser().getId(),
                order.getPrice(),
                order.getBuyBidding().getMarketProduct().getName(),
                RelType.ORDER,
                order.getId()
        );

        CashApiResponse<CashHoldResponseDto> response = fakeCashClient.requestBidHold(paymentReq);
        if(!response.isSuccess()) {
            if (response.isChargeFailed()) {
                throw new BadRequestException(FailureCode.WALLET_CHARGE_FAILED);
            }
            throw new BadRequestException(FailureCode.CASH_MODULE_ERROR);
        }

        return order.getId();
    }
}
