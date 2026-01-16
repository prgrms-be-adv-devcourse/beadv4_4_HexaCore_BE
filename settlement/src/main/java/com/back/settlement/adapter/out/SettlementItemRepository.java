package com.back.settlement.adapter.out;

import com.back.settlement.domain.SettlementItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementItemRepository extends JpaRepository<SettlementItem, Long> {
}
