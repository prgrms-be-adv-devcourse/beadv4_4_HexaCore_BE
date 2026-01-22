package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Bidding;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.common.feign.cash.enums.RelType;
import com.back.common.feign.cash.request.PaymentCancelRequestDto;
import com.back.common.feign.cash.response.PaymentCancelResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancelBidUseCase {
    private final BiddingRepository biddingRepository;
    private final MarketSupport marketSupport;

    @Transactional
    public PaymentCancelResponseDto cancelBid(Long userId, Long biddingId) {
        //입찰 조회
        Bidding bidding = biddingRepository.findById(biddingId)
                .orElseThrow(() -> new BadRequestException(FailureCode.BIDDING_NOT_FOUND));

        //권한 검증: 본인 입찰 아니면 에러
        if(!bidding.getMarketUser().getId().equals(userId)){
            throw new BadRequestException(FailureCode.FORBIDDEN);
        }

        //상태 검증
        if(bidding.getStatus() == BiddingStatus.MATCHED || bidding.getStatus() == BiddingStatus.CANCELLED ||bidding.getStatus() == BiddingStatus.EXPIRED) {
            throw new BadRequestException(FailureCode.CANNOT_CANCEL_BID);
        }

        //입찰 상태 변경(cancelled)
        bidding.changeStatus(BiddingStatus.CANCELLED);
        log.info("[CancelBid] 입찰 취소 상태 변경 완료 - BiddingId: {}", biddingId);

        //환불 처리(구매 입찰인 경우)
        if (bidding.getPosition() == BiddingPosition.BUY) {
            PaymentCancelRequestDto refundRequest = PaymentCancelRequestDto.of(
                    userId,
                    RelType.BIDDING,
                    bidding.getId(),
                    bidding.getPrice() //예치금 전액 환불
            );
            //MarketSupport -> cashclient -> 결과반환
            return marketSupport.refundBidPayment(refundRequest);
        } else {
            //판매입찰인 경우 환불금액 0원
            return PaymentCancelResponseDto.of(userId, BigDecimal.ZERO);
        }
    }
}
