package com.back.user.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserIdempotencyService {

    private final UserConsumedEventRepository userConsumedEventRepository;

    // true면 최초 처리, false면 이미 처리됨.
    @Transactional
    public boolean tryMarkConsumed(UUID eventId, String eventType, LocalDateTime now) {
        if (userConsumedEventRepository.existsById(eventId)) {
            return false;
        }
        userConsumedEventRepository.saveAndFlush(
                UserConsumedEvent.create(eventId, eventType, now)
        );
        return true;
    }
}
