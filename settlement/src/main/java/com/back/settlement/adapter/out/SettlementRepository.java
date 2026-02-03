package com.back.settlement.adapter.out;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long>, SettlementCustomRepository {

    List<Settlement> findBySellerId(Long sellerId);

    List<Settlement> findByStatusAndStartAtBetween(SettlementStatus status, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable);

    long countByStatus(SettlementStatus status);

    Optional<Settlement> findBySellerIdAndStartAt(Long sellerId, LocalDateTime startAt);
}
