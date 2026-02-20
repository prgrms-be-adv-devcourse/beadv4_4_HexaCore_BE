package com.back.chat.adapter.out.idempotency;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final ConsumedEventRepository consumedEventRepository;

    @Transactional
    public boolean tryAcquire(UUID eventId, String eventType, LocalDateTime consumedAt) {
        // insert 성공(1)이면 처음 소비, 0이면 이미 존재(중복)
        return consumedEventRepository.insertIfAbsent(eventId, eventType, consumedAt) == 1;
    }
}
