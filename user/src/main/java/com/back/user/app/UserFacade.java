package com.back.user.app;

import com.back.user.domain.User;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.response.UserIdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserSupport userSupport;
    private final UserUpdateUsecase userUpdateUsecase;
    private final UserIncrementBlindCountUseCase userIncrementBlindCountUseCase;

    @Transactional
    public UserIdResponse registerOrUpdateFcmToken(Long userId, UpdateFcmTokenRequest request) {
        User user = userSupport.findById(userId);

        userUpdateUsecase.updateFcmToken(user, request);

        return UserIdResponse.of(user);
    }

    public void incrementBlindCount(Long userId) {
        userIncrementBlindCountUseCase.incrementBlindCount(userId);
    }

    public LocalDateTime getChatRestrictedUntil(Long userId){
        User user = userSupport.findById(userId);
        return user.getChatRestrictedUntil();
    }

}
