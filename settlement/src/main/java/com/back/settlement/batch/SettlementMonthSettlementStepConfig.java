package com.back.settlement.batch;

import static com.back.settlement.domain.SettlementPolicy.CHUNK_SIZE;

import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.app.usecase.SettlementCompleteUseCase;
import com.back.settlement.domain.Settlement;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 정산 완료 처리 및 캐시 지급 요청 Step 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementMonthSettlementStepConfig {
    private final SettlementSupport settlementSupport;
    private final SettlementCompleteUseCase settlementCompleteUseCase;

    @Bean
    public Step monthSettlementStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("monthSettlementStep", jobRepository)
                .<Settlement, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(pendingSettlementReader())
                .processor(settlementCompleteProcessor())
                .writer(settlementWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Settlement> pendingSettlementReader() {
        return new ItemReader<>() {
            private int page = 0;
            private List<Settlement> currentPage;
            private int index = 0;

            @Override
            public Settlement read() {
                if (currentPage == null || index >= currentPage.size()) {
                    currentPage = settlementSupport.findPendingSettlements(PageRequest.of(page++, CHUNK_SIZE));
                    index = 0;

                    if (currentPage.isEmpty()) {
                        return null;
                    }
                }
                return currentPage.get(index++);
            }
        };
    }

    /**
     * 정산서를 완료 상태로 변경
     */
    @Bean
    public ItemProcessor<Settlement, Settlement> settlementCompleteProcessor() {
        return settlementCompleteUseCase::completeSettlement;
    }

    /**
     * 정산서를 저장하고 도메인 이벤트 발행 (캐시 지급은 이벤트 핸들러에서 처리)
     */
    @Bean
    public ItemWriter<Settlement> settlementWriter() {
        return chunk -> {
            List<Settlement> settlements = chunk.getItems().stream()
                    .map(item -> (Settlement) item)
                    .toList();
            settlementCompleteUseCase.saveAndPublishEvents(settlements);
        };
    }
}
