package com.back.market.adapter.out;

import com.back.market.domain.Bidding;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BiddingRepository extends JpaRepository<Bidding, Long> {

    Optional<Bidding> findFirstByMarketProductIdAndPositionAndStatusOrderByPriceAsc(Long productId, BiddingPosition sell, BiddingStatus process);

    Optional<Bidding> findFirstByMarketProductIdAndPositionAndStatusOrderByPriceDesc(Long productId, BiddingPosition biddingPosition, BiddingStatus biddingStatus);
}
