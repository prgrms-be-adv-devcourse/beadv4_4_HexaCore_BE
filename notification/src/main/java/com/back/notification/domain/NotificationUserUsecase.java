package com.back.notification.domain;

import com.back.notification.adapter.out.NotificationUserRepository;
import com.back.notification.exception.NotificationUserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationUserUsecase {
    private final NotificationUserRepository notificationUserRepository;

    public String findFcmTokenByUserId(Long userId){
        return notificationUserRepository.findFcmTokenByUserId(userId)
                .orElse(null);
    }

    public NotificationUser findNotificationUserById(Long userId){
        return notificationUserRepository.findById(userId)
                .orElseThrow(NotificationUserNotFoundException::new);
    }
}
