package com.back.settlement.adapter.out;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long>, SettlementCustomRepository {

    List<Settlement> findBySellerId(Long sellerId);

    long countByStatus(SettlementStatus status);
}
