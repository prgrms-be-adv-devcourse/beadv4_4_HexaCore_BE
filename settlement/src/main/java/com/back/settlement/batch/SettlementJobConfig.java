package com.back.settlement.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SettlementJobConfig {

    @Bean
    public Job dailySettlementJob(JobRepository jobRepository, Step collectOrdersStep) {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .start(collectOrdersStep)
                .build();
    }

    @Bean
    public Job monthlySettlementJob(JobRepository jobRepository, Step createSettlementsStep) {
        return new JobBuilder("monthlySettlementJob", jobRepository)
                .start(createSettlementsStep)
                .build();
    }
}
