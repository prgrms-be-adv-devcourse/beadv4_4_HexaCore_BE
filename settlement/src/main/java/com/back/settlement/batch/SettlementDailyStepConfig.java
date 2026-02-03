package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.settlement.adapter.out.feign.market.OrderClient;
import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.usecase.SettlementItemAddUseCase;
import java.time.LocalDate;
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
public class SettlementDailyStepConfig {
    private final OrderClient orderClient;
    private final SettlementItemAddUseCase settlementItemAddUseCase;

    @Bean
    public Step collectOrdersStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<SettlementTargetOrder> orderReader,
            ItemProcessor<SettlementTargetOrder, SettlementTargetOrder> orderProcessor,
            ItemWriter<SettlementTargetOrder> orderWriter
    ) {
        return new StepBuilder("collectOrdersStep", jobRepository)
                .<SettlementTargetOrder, SettlementTargetOrder>chunk(CHUNK_SIZE, transactionManager)
                .reader(orderReader)
                .processor(orderProcessor)
                .writer(orderWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<SettlementTargetOrder> orderReader(@Value("#{jobParameters['targetDate']}") String targetDateStr) {
        return new ItemReader<>() {
            private int page = 0;
            private Iterator<SettlementTargetOrder> iterator;
            private boolean exhausted = false;

            @Override
            public SettlementTargetOrder read() {
                if (exhausted) {
                    return null;
                }
                if (iterator != null && iterator.hasNext()) {
                    return iterator.next();
                }

                LocalDate targetDate = LocalDateUtils.parseOrDefault(targetDateStr);
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
        };
    }

    @Bean
    public ItemProcessor<SettlementTargetOrder, SettlementTargetOrder> orderProcessor() {
        return order -> {
            log.debug("주문 처리. orderId={}", order.orderId());
            return order;
        };
    }

    @Bean
    public ItemWriter<SettlementTargetOrder> orderWriter() {
        return chunk -> {
            for (SettlementTargetOrder order : chunk) {
                settlementItemAddUseCase.add(order);
            }
            log.info("정산 항목 생성 완료. 건수={}", chunk.size());
        };
    }
}
