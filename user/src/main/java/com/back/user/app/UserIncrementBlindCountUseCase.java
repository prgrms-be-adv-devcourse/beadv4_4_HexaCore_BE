package com.back.user.app;

import com.back.user.domain.User;
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

    public void incrementBlindCount(Long userId){
        User user = userSupport.findById(userId);

        user.incrementBlindCount();

        if(user.getBlindCount()>= ChatRestrictionPolicy.BLIND_THRESHOLD){
            // user.restrictChatForDays(ChatRestrictionPolicy.RESTRICT_DAYS);
            user.resetBlindCount();
        }
    }

    @Transactional
    public void incrementBlindCount(Long userId, LocalDateTime now) {
        LocalDateTime restrictedUntil = userSupport.incrementBlindAndReturnRestrictedUntil(
                userId,
                now,
                ChatRestrictionPolicy.BLIND_THRESHOLD,
                ChatRestrictionPolicy.RESTRICT_DURATION.getSeconds()
        );

        if (restrictedUntil != null && restrictedUntil.isAfter(now)) {
            redisChatRestrictionCache.put(userId, restrictedUntil, now);
        }
    }
}
