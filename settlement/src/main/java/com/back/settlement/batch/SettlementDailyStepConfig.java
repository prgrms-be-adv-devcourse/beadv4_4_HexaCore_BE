package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.common.dto.settlement.SettlementTargetOrder;
import com.back.settlement.batch.reader.OrderPagingItemReader;
import com.back.settlement.batch.writer.OrderItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementDailyStepConfig {

    @Bean
    public Step collectOrdersStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, OrderPagingItemReader reader, OrderItemWriter writer) {
        return new StepBuilder("collectOrdersStep", jobRepository)
                .<SettlementTargetOrder, SettlementTargetOrder>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }
}
