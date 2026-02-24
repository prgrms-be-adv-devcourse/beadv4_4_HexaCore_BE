package com.back.settlement.batch.writer;

import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.usecase.SettlementCreateUseCase;
import com.back.settlement.batch.SettlementWithItems;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class SettlementChunkWriter implements ItemWriter<Long> {
    private final SettlementCreateUseCase settlementCreateUseCase;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("#{jobParameters['targetMonth']}")
    private String targetMonthStr;

    public SettlementChunkWriter(SettlementCreateUseCase settlementCreateUseCase) {
        this.settlementCreateUseCase = settlementCreateUseCase;
    }

    @Override
    public void write(Chunk<? extends Long> chunk) {
        YearMonth targetMonth = LocalDateUtils.parseYearMonthOrDefault(targetMonthStr);
        List<Long> payeeIds = new ArrayList<>(chunk.getItems());

        // 1) IN절 배치 조회: 청크 내 모든 payeeId의 미정산 항목을 1회 SELECT
        List<SettlementWithItems> settlements = settlementCreateUseCase.createSettlements(payeeIds, targetMonth);

        // 2) 정산별 저장: 도메인 이벤트(SettlementLog, Outbox) 정상 발행
        for (SettlementWithItems swi : settlements) {
            settlementCreateUseCase.saveSettlement(swi.settlement(), swi.items());
        }

        // 3) 변경사항 DB 반영 후 1차 캐시 정리 (flush 없이 clear하면 dirty 변경 소실)
        entityManager.flush();
        entityManager.clear();

        log.info("정산 청크 완료. payeeIds={}, settlements={}", payeeIds.size(), settlements.size());
    }
}
