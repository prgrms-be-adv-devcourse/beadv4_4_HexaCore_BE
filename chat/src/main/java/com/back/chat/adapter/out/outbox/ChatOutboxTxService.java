package com.back.chat.adapter.out.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatOutboxTxService {

    private final ChatOutboxRepository chatOutboxRepository;

    @Transactional
    public List<ChatOutbox> claim(LocalDateTime now, int batchSize, int maxRetry) {
        return chatOutboxRepository.claimBatch(now, batchSize, maxRetry);
    }

    @Transactional
    public int recoverStuckProcessing(LocalDateTime cutoff, LocalDateTime now) {
        return chatOutboxRepository.recoverStuckProcessing(cutoff, now);
    }
}
