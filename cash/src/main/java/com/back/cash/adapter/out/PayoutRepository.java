package com.back.cash.adapter.out;

import com.back.cash.domain.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    boolean existsBySettlementId(Long settlementId);
    Optional<Payout> findBySettlementId(Long settlementId);
}
