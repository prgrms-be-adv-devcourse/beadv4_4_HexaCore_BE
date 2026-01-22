package com.back.cash.adapter.out;

import com.back.cash.domain.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    boolean existsBySettlementId(Long settlementId);
}
