package com.back.notification.app;

import com.back.notification.adapter.out.NotificationUserSettingRepository;
import com.back.notification.domain.NotificationUserSetting;
import com.back.notification.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationUserSettingUsecase {
    private final NotificationUserSettingRepository notificationUserSettingRepository;

    public NotificationUserSetting findById(Long userId) {
        return notificationUserSettingRepository.findByUser_Id(userId)
                .orElseThrow(NotificationNotFoundException::new);
    }
}
