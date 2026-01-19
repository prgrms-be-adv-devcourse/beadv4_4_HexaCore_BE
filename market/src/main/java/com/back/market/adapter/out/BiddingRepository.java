package com.back.market.adapter.out;

import com.back.market.domain.Bidding;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiddingRepository extends JpaRepository<Bidding, Long> {

    /**
     * 즉시 구매가 조회(최저가 판매 입찰 조회)
     * <p>
     * 특정 상품에 대해 '판매(SELL)' 포지션으로 등록된 입찰 중,
     * 가장 낮은 가격(오름차순, ASC)의 입찰 건을 1개 조회한다.
     * </p>
     * @param productId 조회할 상품의 ID
     * @param biddingPosition 입찰 포지션 (반드시 BiddingPosition.SELL 이어야 함)
     * @param biddingStatus 입찰 상태 (반드시 BiddingStatus.PROCESS 이어야 함)
     * @return 특정 상품의 즉시 구매가
     */
    Optional<Bidding> findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(Long productId, BiddingPosition biddingPosition, BiddingStatus biddingStatus);

    /**
     * 즉시 판매가 조회(최고가 구매 입찰 조회)
     * <p>
     * 특정 상품에 대해 '구매(BUY)' 포지션으로 등록된 입찰 중,
     * 가장 높은 가격(내림차순, DESC)의 입찰 건을 1개 조회한다.
     * </p>
     *
     * @param productId 조회할 상품의 ID
     * @param biddingPosition 입찰 포지션 (반드시 BiddingPosition.BUY 이어야 함)
     * @param biddingStatus 입찰 상태 (반드시 BiddingStatus.PROCESS 이어야 함)
     * @return 특정 상품의 즉시 판매가
     */
    Optional<Bidding> findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(Long productId, BiddingPosition biddingPosition, BiddingStatus biddingStatus);
}
