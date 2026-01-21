package com.back.market.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.market.adapter.out.BiddingRepository;
import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.adapter.out.MarketUserRepository;
import com.back.market.app.MarketSupport;
import com.back.market.domain.Bidding;
import com.back.market.domain.MarketProduct;
import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import com.back.market.dto.enums.PayAndHoldStatus;
import com.back.market.dto.enums.RelType;
import com.back.market.dto.request.BiddingRequestDto;
import com.back.market.dto.request.PayAndHoldRequestDto;
import com.back.market.dto.response.PayAndHoldResponseDto;
import com.back.market.mapper.BiddingMapper;
import com.back.market.mapper.CashRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 구매/판매입찰 등록 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterBidUseCase {
    private final BiddingRepository biddingRepository;
    private final MarketUserRepository marketUserRepository;
    private final MarketProductRepository marketProductRepository;
    private final BiddingMapper biddingMapper;
    private final CashRequestMapper cashRequestMapper;
    private final MarketSupport marketSupport;

    /**
     * MARKET-010: 구매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return 저장된 구매 입찰의 PK
     */
    @Transactional
    public PayAndHoldResponseDto registerBuyBid(Long userId, BiddingRequestDto requestDto) {
        // 가격 유효성 검사(1000원단위인지 아닌지)
        validatePriceUnit(requestDto.price());

        // 가격 정책 검사: 내 입찰가 >= 최저 판매가(즉시구매가)일 경우 에러
        // 에러 발생 시 즉시 구매로 유도
        checkBuyPricePolicy(userId, requestDto);

        // 엔티티 조회 및 예외 처리
        MarketUser user = marketUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(FailureCode.USER_NOT_FOUND));
        MarketProduct product = marketProductRepository.findById(requestDto.productId())
                .orElseThrow(() -> new BadRequestException(FailureCode.PRODUCT_NOT_FOUND));

        // 구매 입찰 엔티티 생성 및 저장(id 생성을 위해 먼저 진행)
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, BiddingPosition.BUY);
        Bidding savedBidding = biddingRepository.save(bidding);

        // TODO: 구매 입찰 등록 시 포인트 잔액 확인 및 차감 로직 추가 필요. 임시로 FakeCashClient 구현해서 테스트(marketSupport 클래스 확인)
        PayAndHoldRequestDto cashRequest = cashRequestMapper.toPayAndHoldRequestForBidding(
                userId,
                requestDto.price(),
                savedBidding.getId()
        );
        PayAndHoldResponseDto responseData = marketSupport.getPayAndHoldResult(cashRequest);

        if (responseData.status() == PayAndHoldStatus.PAID) {
            savedBidding.changeStatus(BiddingStatus.PROCESS);
            log.info("[RegisterBid] 예치금 홀딩 & 입찰 등록 완료 (HOLD->PROCESS) - BiddingId: {}", savedBidding.getId());
        } else if (responseData.status() == PayAndHoldStatus.REQUIRES_PG) {
            log.info("[RegisterBid] PG 결제 필요, HOLD 상태 유지- relId: {}", responseData.relId());
        }

        return responseData;
    }

    /**
     * MARKET-012: 판매 입찰 등록
     * @param userId 사용자 ID
     * @param requestDto BiddingRequestDto
     * @return 저장된 판매 입찰의 PK
     */
    @Transactional
    public PayAndHoldResponseDto registerSellBid(Long userId, BiddingRequestDto requestDto) {
        // 가격 유효성 검사(1000원단위인지 아닌지)
        validatePriceUnit(requestDto.price());

        // 가격 정책 검사: 내 판매가 <= 최고 구매가(즉시판매가)일 경우 에러
        // 에러 발생 시 즉시 판매로 유도
        checkSellPricePolicy(userId, requestDto);

        // 엔티티 조회 및 예외 처리
        MarketUser user = marketUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(FailureCode.USER_NOT_FOUND));
        MarketProduct product = marketProductRepository.findById(requestDto.productId())
                .orElseThrow(() -> new BadRequestException(FailureCode.PRODUCT_NOT_FOUND));

        // 판매 입찰 저장
        Bidding bidding = biddingMapper.toEntity(requestDto, user, product, BiddingPosition.SELL);
        bidding.changeStatus(BiddingStatus.PROCESS); // 판매 입찰은 결제 과정이 없으므로 즉시 활성화
        Bidding savedBidding = biddingRepository.save(bidding);
        return PayAndHoldResponseDto.of(
                PayAndHoldStatus.PAID, // 판매 입찰은 결제가 필요없으므로 완료 상태로 생성
                RelType.BIDDING,
                savedBidding.getId(),
                BigDecimal.ZERO, // 사용된 예치금 없음
                BigDecimal.ZERO, // 필요한 PG 금액 없음
                null
        );
    }

    /**
     * 유효성 검사(금액이 천원 단위인지 확인)
     * @param price 물품 가격
     */
    private void validatePriceUnit(BigDecimal price) {
        if(price.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO)!=0) {
            throw new BadRequestException(FailureCode.INVALID_PRICE_UNIT);
        }
    }

    /**
     * 구매 시 가격 정책 검사
     * @param userId 사용자ID
     * @param requestDto BiddingRequestDto
     */
    private void checkBuyPricePolicy(Long userId, BiddingRequestDto requestDto) {
        biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(
                requestDto.productId(),
                BiddingPosition.SELL,
                BiddingStatus.PROCESS
        ).ifPresent(minSellingBid -> {
            if(requestDto.price().compareTo(minSellingBid.getPrice()) >= 0) {
                // 본인이 올린 상품의 거래를 막음
                if (minSellingBid.getMarketUser().getId().equals(userId)) {
                    throw new BadRequestException(FailureCode.SELF_TRADING_NOT_ALLOWED);
                }

                throw new BadRequestException(FailureCode.INVALID_BID_PRICE_BUY);
            }
        });
    }

    /**
     * 판매 시 가격 정책 검사
     * @param userId 사용자ID
     * @param requestDto BiddingRequestDto
     */
    private void checkSellPricePolicy(Long userId, BiddingRequestDto requestDto) {
        biddingRepository.findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(
                requestDto.productId(),
                BiddingPosition.BUY,
                BiddingStatus.PROCESS
        ).ifPresent(maxBuyingBid -> {
            if(requestDto.price().compareTo(maxBuyingBid.getPrice()) <= 0) {
                // 본인이 올린 상품의 거래를 막음
                if (maxBuyingBid.getMarketUser().getId().equals(userId)) {
                    throw new BadRequestException(FailureCode.SELF_TRADING_NOT_ALLOWED);
                }

                throw new BadRequestException(FailureCode.INVALID_BID_PRICE_SELL);
            }
        });
    }
}
