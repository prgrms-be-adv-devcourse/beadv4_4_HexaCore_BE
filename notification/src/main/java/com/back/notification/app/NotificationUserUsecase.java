package com.back.notification.app;

import com.back.notification.adapter.out.NotificationUserRepository;
import com.back.notification.domain.NotificationUser;
import com.back.notification.exception.NotificationUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    /**
     * FCM 토큰 업데이트
     * @param userId 사용자 ID
     * @param fcmToken 새로운 FCM 토큰
     */
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        NotificationUser user = notificationUserRepository.findById(userId)
                .orElseThrow(NotificationUserNotFoundException::new);
        user.updateFcmToken(fcmToken);
    }
}
