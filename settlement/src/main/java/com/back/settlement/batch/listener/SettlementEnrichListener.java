package com.back.settlement.batch.listener;

import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.usecase.SettlementCreateUseCase;
import com.back.settlement.batch.PayeeSettlementWriteItem;
import com.back.settlement.batch.SettlementWithItems;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class SettlementEnrichListener implements ItemWriteListener<PayeeSettlementWriteItem> {
    private final SettlementCreateUseCase settlementCreateUseCase;

    @Value("#{jobParameters['targetMonth']}")
    private String targetMonthStr;

    public SettlementEnrichListener(SettlementCreateUseCase settlementCreateUseCase) {
        this.settlementCreateUseCase = settlementCreateUseCase;
    }

    @Override
    public void beforeWrite(Chunk<? extends PayeeSettlementWriteItem> items) {
        List<Long> payeeIds = items.getItems().stream()
                .map(PayeeSettlementWriteItem::getPayeeId)
                .toList();

        YearMonth targetMonth = LocalDateUtils.parseYearMonthOrDefault(targetMonthStr);
        List<SettlementWithItems> settlements = settlementCreateUseCase.createSettlements(payeeIds, targetMonth);

        Map<Long, SettlementWithItems> byPayee = settlements.stream()
                .collect(Collectors.toMap(
                        swi -> swi.items().get(0).getPayeeId(),
                        swi -> swi
                ));

        items.getItems().forEach(writeItem -> {
            SettlementWithItems swi = byPayee.get(writeItem.getPayeeId());
            if (swi != null) {
                writeItem.enrich(swi.settlement(), swi.items());
            }
        });

        log.info("정산 데이터 보강 완료. payeeIds={}, enriched={}", payeeIds.size(), byPayee.size());
    }
}
