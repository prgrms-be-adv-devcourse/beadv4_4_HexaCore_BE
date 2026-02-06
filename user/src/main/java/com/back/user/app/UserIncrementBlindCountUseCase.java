package com.back.user.app;

import com.back.user.domain.User;
import com.back.user.domain.policy.ChatRestrictionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserIncrementBlindCountUseCase {

    private final UserSupport userSupport;

    public void incrementBlindCount(Long userId){
        User user = userSupport.findById(userId);

        user.incrementBlindCount();

        if(user.getBlindCount()>= ChatRestrictionPolicy.BLIND_THRESHOLD){
            // user.restrictChatForDays(ChatRestrictionPolicy.RESTRICT_DAYS);
            user.resetBlindCount();
        }
    }
}
