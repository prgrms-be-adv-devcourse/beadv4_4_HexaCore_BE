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

    // Spring Data JPA의 Repository는 clear()를 제공하지 않으므로 EntityManager를 직접 사용한다.
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
        List<SettlementWithItems> settlements = settlementCreateUseCase.createSettlements(payeeIds, targetMonth);

        for (SettlementWithItems swi : settlements) {
            settlementCreateUseCase.saveSettlement(swi.settlement(), swi.items());
        }

        entityManager.flush();
        entityManager.clear();

        log.info("정산 청크 완료. payeeIds={}, settlements={}", payeeIds.size(), settlements.size());
    }
}
