package com.back.settlement.adapter.out;

import com.back.settlement.domain.Settlement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findBySellerId(Long sellerId);
}
