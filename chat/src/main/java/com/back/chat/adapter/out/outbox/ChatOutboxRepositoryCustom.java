package com.back.chat.adapter.out.outbox;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatOutboxRepositoryCustom {
    List<ChatOutbox> claimBatch(LocalDateTime now,int batchSize, int maxRetry);

    int recoverStuckProcessing(LocalDateTime cutoff, LocalDateTime now);
}
