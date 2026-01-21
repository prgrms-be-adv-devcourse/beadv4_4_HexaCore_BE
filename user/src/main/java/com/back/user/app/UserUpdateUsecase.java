package com.back.user.app;

import com.back.user.domain.User;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUpdateUsecase {

    public void updateFcmToken(User user, UpdateFcmTokenRequest request) {
        user.updateFcmToken(request.fcmToken());
    }
}
