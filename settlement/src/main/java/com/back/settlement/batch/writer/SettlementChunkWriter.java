package com.back.settlement.batch.writer;

import com.back.settlement.app.usecase.SettlementCreateUseCase;
import com.back.settlement.batch.PayeeSettlementWriteItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class SettlementChunkWriter implements ItemWriter<PayeeSettlementWriteItem> {
    private final SettlementCreateUseCase settlementCreateUseCase;

    @PersistenceContext
    private EntityManager entityManager;

    public SettlementChunkWriter(SettlementCreateUseCase settlementCreateUseCase) {
        this.settlementCreateUseCase = settlementCreateUseCase;
    }

    @Override
    public void write(Chunk<? extends PayeeSettlementWriteItem> chunk) {
        int count = 0;
        for (PayeeSettlementWriteItem writeItem : chunk.getItems()) {
            if (writeItem.isEnriched()) {
                settlementCreateUseCase.saveSettlement(writeItem.getSettlement(), writeItem.getItems());
                count++;
            }
        }

        entityManager.flush();
        entityManager.clear();

        log.info("정산 청크 저장 완료. settlements={}", count);
    }
}
