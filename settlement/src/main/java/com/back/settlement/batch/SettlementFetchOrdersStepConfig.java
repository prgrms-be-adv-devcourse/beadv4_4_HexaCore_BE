package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.settlement.adapter.out.feign.market.OrderClient;
import com.back.settlement.app.support.YearMonthUtils;
import com.back.settlement.app.usecase.SettlementItemAddUseCase;
import java.time.YearMonth;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementFetchOrdersStepConfig {
    private final OrderClient orderClient;
    private final SettlementItemAddUseCase settlementItemAddUseCase;

    @Bean
    public Step fetchOrdersAndCreateItemsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("fetchOrdersAndCreateItemsStep", jobRepository)
                .<SettlementTargetOrder, SettlementTargetOrder>chunk(CHUNK_SIZE, transactionManager)
                .reader(orderItemReader(null))
                .processor(orderItemProcessor())
                .writer(orderItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<SettlementTargetOrder> orderItemReader(@Value("#{jobParameters['targetMonth']}") String targetMonthStr) {
        return new ItemReader<>() {
            private int page = 0;
            private Iterator<SettlementTargetOrder> currentPageIterator;
            private boolean exhausted = false;

            @Override
            public SettlementTargetOrder read() {
                if (exhausted) {
                    return null;
                }
                if (currentPageIterator != null && currentPageIterator.hasNext()) {
                    return currentPageIterator.next();
                }

                YearMonth targetMonth = YearMonthUtils.parseOrDefault(targetMonthStr);
                List<SettlementTargetOrder> pageData = orderClient.findSettlementTargetOrders(targetMonth, page++, CHUNK_SIZE);

                if (pageData.isEmpty()) {
                    exhausted = true;
                    log.info("Order API에서 모든 데이터를 읽었습니다. 총 페이지 수: {}", page - 1);
                    return null;
                }
                log.info("Order API 페이지 조회. page={}, size={}", page - 1, pageData.size());
                currentPageIterator = pageData.iterator();
                return currentPageIterator.next();
            }
        };
    }

    @Bean
    public ItemProcessor<SettlementTargetOrder, SettlementTargetOrder> orderItemProcessor() {
        return request -> {
            log.debug("주문 처리 중. orderId={}", request.orderId());
            return request;
        };
    }

    @Bean
    public ItemWriter<SettlementTargetOrder> orderItemWriter() {
        return chunk -> {
            for (SettlementTargetOrder request : chunk) {
                settlementItemAddUseCase.add(request);
            }
            log.info("SettlementItem 생성 완료. 처리 건수: {}", chunk.size());
        };
    }
}
