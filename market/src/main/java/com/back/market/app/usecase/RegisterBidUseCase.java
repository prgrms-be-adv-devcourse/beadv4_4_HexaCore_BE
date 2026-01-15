package com.back.market.app.usecase;

import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.exception.InvalidBiddingPriceException;
import com.back.market.exception.MarketEntityNotFoundException;
import com.back.market.exception.code.MarketErrorCode;
import com.back.market.mapper.BiddingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 구매/판매입찰 등록 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class RegisterBidUseCase {
    private final BiddingRepository biddingRepository;
    private final MarketUserRepository marketUserRepository;
    private final MarketProductRepository marketProductRepository;
    private final BiddingMapper biddingMapper;

    /**
     * MARKET-010: 구매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return 저장된 구매 입찰의 PK
     */
    @Transactional
    public Long registerBuyBid(Long userId, BiddingRequestDto requestDto) {
        // 가격 유효성 검사(1000원단위인지 아닌지)
        validatePriceUnit(requestDto.getPrice());

        // 가격 정책 검사: 내 입찰가 >= 최저 판매가(즉시구매가)일 경우 에러
        // 에러 발생 시 즉시 구매로 유도
        biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(
                requestDto.getProductId(),
                BiddingPosition.SELL,
                BiddingStatus.PROCESS
        ).ifPresent(minSellingBid -> {
            if(requestDto.getPrice().compareTo(minSellingBid.getPrice()) >= 0) {
                throw new InvalidBiddingPriceException(MarketErrorCode.INVALID_BID_PRICE_BUY);
            }
        });

        // 엔티티 조회 및 예외 처리
        MarketUser user = marketUserRepository.findById(userId)
                .orElseThrow(() -> new MarketEntityNotFoundException(MarketErrorCode.USER_NOT_FOUND));
        MarketProduct product = marketProductRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new MarketEntityNotFoundException(MarketErrorCode.PRODUCT_NOT_FOUND));

        // TODO: 구매 입찰 등록 시 포인트 잔액 확인 및 차감 로직 추가 필요. MarketWallet 구성 이후 추가 예정

        // 구매 입찰 저장
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, BiddingPosition.BUY);

        Bidding savedBidding = biddingRepository.save(bidding);
        return savedBidding.getId();
    }

    /**
     * MARKET-012: 판매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return 저장된 판매 입찰의 PK
     */
    @Transactional
    public Long registerSellBid(Long userId, BiddingRequestDto requestDto) {
        // 가격 유효성 검사(1000원단위인지 아닌지)
        validatePriceUnit(requestDto.getPrice());

        // 가격 정책 검사: 내 판매가 <= 최고 구매가(즉시판매가)일 경우 에러
        // 에러 발생 시 즉시 판매로 유도
        biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(
                requestDto.getProductId(),
                BiddingPosition.BUY,
                BiddingStatus.PROCESS
        ).ifPresent(maxBuyingBid -> {
            if(requestDto.getPrice().compareTo(maxBuyingBid.getPrice()) <= 0) {
                throw new InvalidBiddingPriceException(MarketErrorCode.INVALID_BID_PRICE_SELL);
            }
        });

        // 엔티티 조회 및 예외 처리
        MarketUser user = marketUserRepository.findById(userId)
                .orElseThrow(() -> new MarketEntityNotFoundException(MarketErrorCode.USER_NOT_FOUND));
        MarketProduct product = marketProductRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new MarketEntityNotFoundException(MarketErrorCode.PRODUCT_NOT_FOUND));

        // 판매 입찰 저장
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, BiddingPosition.SELL);

        Bidding savedBidding = biddingRepository.save(bidding);
        return savedBidding.getId();
    }

    /**
     * 유효성 검사(금액이 천원 단위인지 확인)
     * @param price 물품 가격
     */
    private void validatePriceUnit(BigDecimal price) {
        if(price.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO)!=0) {
            throw new InvalidBiddingPriceException(MarketErrorCode.INVALID_PRICE_UNIT);
        }
    }
}
