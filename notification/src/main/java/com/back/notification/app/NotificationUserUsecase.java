package com.back.notification.app;

import com.back.notification.adapter.out.NotificationUserRepository;
import com.back.notification.domain.NotificationUser;
import com.back.notification.exception.NotificationUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    /**
     * 유저 생성 (user-account-created 이벤트 수신 시 복제)
     * @param id        사용자 ID
     * @param nickname  닉네임
     * @param email     이메일
     */
    @Transactional
    public void createUser(Long id, String nickname, String email) {
        Optional<NotificationUser> existing = notificationUserRepository.findById(id);
        if (existing.isPresent()) {
            log.warn("[NotificationUserUsecase] 이미 존재하는 userId로 생성 요청 무시 : userId = {}", id);
            return;
        }
        NotificationUser user = NotificationUser.builder()
                .id(id)
                .nickname(nickname)
                .email(email)
                .build();
        notificationUserRepository.save(user);
    }

    /**
     * 유저 정보 업데이트 (user-account-updated 이벤트 수신 시 동기화)
     * @param id        사용자 ID
     * @param nickname  닉네임
     */
    @Transactional
    public void updateUser(Long id, String nickname) {
        NotificationUser user = notificationUserRepository.findById(id)
                .orElseThrow(NotificationUserNotFoundException::new);
        user.update(nickname);
    }
}
