package com.back.settlement.batch.reader;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.settlement.adapter.out.feign.market.OrderClient;
import com.back.settlement.app.support.LocalDateUtils;
import java.time.LocalDate;
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
public class OrderPagingItemReader implements ItemReader<SettlementTargetOrder> {
    private int page = 0;
    private Iterator<SettlementTargetOrder> iterator;
    private boolean exhausted = false;
    private final OrderClient orderClient;
    private final LocalDate targetDate;

    public OrderPagingItemReader(OrderClient orderClient, @Value("#{jobParameters['targetDate']}") String targetDateStr) {
        this.orderClient = orderClient;
        this.targetDate = LocalDateUtils.parseOrDefault(targetDateStr);
    }

    @Override
    public SettlementTargetOrder read() {
        if (exhausted) {
            return null;
        }
        if (iterator != null && iterator.hasNext()) {
            return iterator.next();
        }

        List<SettlementTargetOrder> pageData = orderClient.findSettlementTargetOrders(targetDate, page++, CHUNK_SIZE);

        if (pageData.isEmpty()) {
            exhausted = true;
            log.info("주문 수집 완료. 총 페이지: {}", page - 1);
            return null;
        }
        log.info("주문 조회. page={}, size={}", page - 1, pageData.size());
        iterator = pageData.iterator();
        return iterator.next();
    }
}
