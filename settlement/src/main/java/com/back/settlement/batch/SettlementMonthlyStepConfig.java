package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.settlement.batch.listener.SettlementEnrichListener;
import com.back.settlement.batch.reader.PayeeIdItemReader;
import com.back.settlement.batch.writer.SettlementChunkWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SettlementMonthlyStepConfig {

    @Bean
    public Step createSettlementsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, PayeeIdItemReader reader, SettlementChunkWriter writer, SettlementEnrichListener enrichListener) {
        return new StepBuilder("createSettlementsStep", jobRepository)
                .<PayeeSettlementWriteItem, PayeeSettlementWriteItem>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .writer(writer)
                .listener(enrichListener)
                .build();
    }
}
