package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.settlement.app.dto.internal.SettlementWithItems;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.app.support.YearMonthUtils;
import com.back.settlement.app.usecase.SettlementCreateUseCase;
import java.time.LocalDateTime;
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
public class SettlementCollectItemsAndCalculateSettlementsStepConfig {
    private final SettlementSupport settlementSupport;
    private final SettlementCreateUseCase settlementCreateUseCase;

    @Bean
    public Step collectItemsAndCalculateSettlementsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("collectItemsAndCalculateSettlementsStep", jobRepository)
                .<Long, SettlementWithItems>chunk(CHUNK_SIZE, transactionManager)
                .reader(unsettledPayeeIdReader(null))      // ItemReader: 미정산 payeeId 목록 조회
                .processor(settlementProcessor(null))      // ItemProcessor: 정산서 생성
                .writer(settlementWithItemsItemWriter())   // ItemWriter: DB 저장
                .build();
    }

    /**
     * 정산되지 않은 payeeID를 읽어오는 ItemReader
     */
    @Bean
    @StepScope
    public ItemReader<Long> unsettledPayeeIdReader(@Value("#{jobParameters['targetMonth']}") String targetMonthStr) {
        return new ItemReader<>() {
            private Iterator<Long> payeeIdIterator;
            private boolean initialized = false;

            @Override
            public Long read() {
                if (!initialized) {
                    YearMonth targetMonth = YearMonthUtils.parseOrDefault(targetMonthStr);
                    LocalDateTime startAt = YearMonthUtils.startOfMonth(targetMonth);
                    LocalDateTime endAt = YearMonthUtils.endOfMonth(targetMonth);
                    List<Long> payeeIds = settlementSupport.findUnsettledPayeeIds(startAt, endAt);
                    payeeIdIterator = payeeIds.iterator();
                    initialized = true;
                    log.info("정산 대상 수취인(payee) 수: {}", payeeIds.size());
                }

                if (payeeIdIterator != null && payeeIdIterator.hasNext()) {
                    return payeeIdIterator.next();
                }
                return null;
            }
        };
    }

    /**
     * payeeId를 받아 정산서와 정산 항목을 생성하는 ItemProcessor
     */
    @Bean
    @StepScope
    public ItemProcessor<Long, SettlementWithItems> settlementProcessor(@Value("#{jobParameters['targetMonth']}") String targetMonthStr) {
        return payeeId -> settlementCreateUseCase.createSettlementForPayee(payeeId, YearMonthUtils.parseOrDefault(targetMonthStr));
    }

    /**
     * 정산서와 항목을 DB에 저장하는 ItemWriter
     */
    @Bean
    public ItemWriter<SettlementWithItems> settlementWithItemsItemWriter() {
        return chunk -> {
            for (SettlementWithItems settlementWithItems : chunk) {
                settlementCreateUseCase.saveSettlementWithItems(
                        settlementWithItems.settlement(),
                        settlementWithItems.items()
                );
            }
            log.info("정산 저장 완료. 건수={}", chunk.size());
        };
    }
}
