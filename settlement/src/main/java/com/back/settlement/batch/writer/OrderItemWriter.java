package com.back.settlement.batch.writer;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.settlement.app.usecase.SettlementItemAddUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderItemWriter implements ItemWriter<SettlementTargetOrder> {
    private final SettlementItemAddUseCase settlementItemAddUseCase;

    @Override
    public void write(Chunk<? extends SettlementTargetOrder> chunk) {
        for (SettlementTargetOrder order : chunk) {
            settlementItemAddUseCase.add(order);
        }
        log.info("정산 항목 생성 완료. 건수={}", chunk.size());
    }
}
