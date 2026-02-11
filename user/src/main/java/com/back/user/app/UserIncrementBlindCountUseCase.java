package com.back.user.app;

import com.back.user.adapter.in.chat.TxAfterCommit;
import com.back.user.domain.policy.ChatRestrictionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserIncrementBlindCountUseCase {

    private final UserSupport userSupport;

    private final RedisChatRestrictionCache redisChatRestrictionCache;

    @Transactional
    public void incrementBlindCount(Long userId, LocalDateTime now) {
        int updated =  userSupport.incrementBlindCount(
                userId,
                now,
                ChatRestrictionPolicy.BLIND_THRESHOLD,
                ChatRestrictionPolicy.RESTRICT_DURATION.getSeconds()
        );

        if (updated == 0) {
            throw new IllegalArgumentException("user not found: " + userId);
        }

        LocalDateTime restrictedUntil = userSupport.getRestrictedUntil(userId);

        if (restrictedUntil != null && restrictedUntil.isAfter(now)) {
            TxAfterCommit.run(() -> redisChatRestrictionCache.put(userId, restrictedUntil, now));
        }
    }
}
