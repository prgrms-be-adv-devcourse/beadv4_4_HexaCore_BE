package com.back.settlement.app.usecase;

import com.back.settlement.adapter.out.SettlementLogRepository;
import com.back.settlement.app.dto.response.SettlementLogResponse;
import com.back.settlement.domain.SettlementLog;
import com.back.settlement.mapper.SettlementMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementLogReadUseCase {
    private final SettlementLogRepository settlementLogRepository;
    private final SettlementMapper settlementMapper;

    public List<SettlementLogResponse> getLogsBySettlementId(Long settlementId) {
        List<SettlementLog> settlementLogs = settlementLogRepository.findBySettlementIdOrderByCreatedAtAsc(settlementId);
        return settlementMapper.toSettlementLogResponseList(settlementLogs);
    }

    public List<SettlementLogResponse> getAllLogs() {
        List<SettlementLog> settlementLogs = settlementLogRepository.findAll();
        return settlementMapper.toSettlementLogResponseList(settlementLogs);
    }
}
