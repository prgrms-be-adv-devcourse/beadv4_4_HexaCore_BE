package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.app.usecase.SettlementCreateUseCase;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
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
public class SettlementMonthlyStepConfig {
    private final SettlementSupport settlementSupport;
    private final SettlementCreateUseCase settlementCreateUseCase;

    @Value("${settlement.system-payee-id}")
    private Long systemPayeeId;

    @Bean
    public Step createSettlementsStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Long> payeeIdReader,
            ItemProcessor<Long, SettlementWithItems> createSettlementProcessor,
            ItemWriter<SettlementWithItems> saveSettlementWriter
    ) {
        return new StepBuilder("createSettlementsStep", jobRepository)
                .<Long, SettlementWithItems>chunk(CHUNK_SIZE, transactionManager)
                .reader(payeeIdReader)
                .processor(createSettlementProcessor)
                .writer(saveSettlementWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Long> payeeIdReader(@Value("#{jobParameters['targetMonth']}") String targetMonthStr) {
        return new ItemReader<>() {
            private Iterator<Long> iterator;
            private boolean initialized = false;

            @Override
            public Long read() {
                if (!initialized) {
                    YearMonth targetMonth = LocalDateUtils.parseYearMonthOrDefault(targetMonthStr);
                    LocalDateTime startAt = LocalDateUtils.startOfMonth(targetMonth);
                    LocalDateTime endAt = LocalDateUtils.endOfMonth(targetMonth);

                    List<Long> sellerIds = settlementSupport.findUnsettledSellerIds(startAt, endAt, systemPayeeId);
                    List<Long> payeeIds = new ArrayList<>(sellerIds);
                    payeeIds.add(systemPayeeId);

                    iterator = payeeIds.iterator();
                    initialized = true;
                    log.info("정산 대상: 판매자 {}명 + 시스템계좌, targetMonth={}", sellerIds.size(), targetMonth);
                }
                return iterator != null && iterator.hasNext() ? iterator.next() : null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Long, SettlementWithItems> createSettlementProcessor(@Value("#{jobParameters['targetMonth']}") String targetMonthStr) {
        return payeeId -> settlementCreateUseCase.createSettlementForPayee(payeeId, LocalDateUtils.parseYearMonthOrDefault(targetMonthStr));
    }

    @Bean
    public ItemWriter<SettlementWithItems> saveSettlementWriter() {
        return chunk -> {
            for (SettlementWithItems item : chunk) {
                settlementCreateUseCase.saveSettlementWithItems(item.settlement(), item.items());
            }
            log.info("정산 완료. 건수={}", chunk.size());
        };
    }
}
