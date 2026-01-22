package com.back.settlement.adapter.out;

import com.back.settlement.domain.SettlementLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {
    // 특정 정산서의 모든 상태 변경 로그를 조회
    List<SettlementLog> findBySettlementIdOrderByCreatedAtAsc(Long settlementId);
}
