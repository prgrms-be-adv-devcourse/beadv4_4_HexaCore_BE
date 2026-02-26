package com.back.settlement.batch.reader;

import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.batch.PayeeSettlementWriteItem;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class PayeeIdItemReader implements ItemReader<PayeeSettlementWriteItem> {
    private final SettlementSupport settlementSupport;
    private final Long systemPayeeId;
    private final YearMonth targetMonth;
    private Iterator<Long> iterator;
    private boolean initialized = false;

    public PayeeIdItemReader(SettlementSupport settlementSupport, @Value("${settlement.system-payee-id}") Long systemPayeeId, @Value("#{jobParameters['targetMonth']}") String targetMonthStr) {
        this.settlementSupport = settlementSupport;
        this.systemPayeeId = systemPayeeId;
        this.targetMonth = LocalDateUtils.parseYearMonthOrDefault(targetMonthStr);
    }

    @Override
    public PayeeSettlementWriteItem read() {
        if (!initialized) {
            LocalDateTime startAt = LocalDateUtils.startOfMonth(targetMonth);
            LocalDateTime endAt = LocalDateUtils.endOfMonth(targetMonth);

            List<Long> sellerIds = settlementSupport.findUnsettledSellerIds(startAt, endAt, systemPayeeId);
            List<Long> payeeIds = new ArrayList<>(sellerIds);
            payeeIds.add(systemPayeeId);

            iterator = payeeIds.iterator();
            initialized = true;
            log.info("정산 대상: 판매자 {}명 + 시스템계좌, targetMonth={}", sellerIds.size(), targetMonth);
        }
        return iterator != null && iterator.hasNext() ? PayeeSettlementWriteItem.create(iterator.next()) : null;
    }
}
