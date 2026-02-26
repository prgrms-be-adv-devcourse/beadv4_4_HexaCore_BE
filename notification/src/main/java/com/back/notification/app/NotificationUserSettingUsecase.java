package com.back.notification.app;

import com.back.notification.adapter.out.NotificationUserRepository;
import com.back.notification.adapter.out.NotificationUserSettingRepository;
import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.NotificationUserSetting;
import com.back.notification.exception.NotificationNotFoundException;
import com.back.notification.exception.NotificationUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationUserSettingUsecase {
    private final NotificationUserSettingRepository notificationUserSettingRepository;
    private final NotificationUserRepository notificationUserRepository;

    public NotificationUserSetting findById(Long userId) {
        return notificationUserSettingRepository.findByUser_Id(userId)
                .orElseThrow(NotificationNotFoundException::new);
    }

    @Transactional
    public void createUserSetting(Long userId, boolean bidStatusEnabled, boolean productStatusEnabled,
                                  boolean priceEnabled, boolean settlementEnabled) {
        NotificationUser user = notificationUserRepository.findById(userId)
                .orElseThrow(NotificationUserNotFoundException::new);

        NotificationUserSetting setting = NotificationUserSetting.builder()
                .user(user)
                .bidStatusEnabled(bidStatusEnabled)
                .productStatusEnabled(productStatusEnabled)
                .priceEnabled(priceEnabled)
                .settlementEnabled(settlementEnabled)
                .build();

        notificationUserSettingRepository.save(setting);
        log.info("[NotificationUserSettingUsecase] 알림설정 생성 완료 - userId: {}", userId);
    }

    @Transactional
    public void updateUserSetting(Long userId, boolean bidStatusEnabled, boolean productStatusEnabled,
                                  boolean priceEnabled, boolean settlementEnabled) {
        NotificationUserSetting setting = notificationUserSettingRepository.findByUser_Id(userId)
                .orElseThrow(NotificationNotFoundException::new);

        setting.update(bidStatusEnabled, productStatusEnabled, priceEnabled, settlementEnabled);
        log.info("[NotificationUserSettingUsecase] 알림설정 업데이트 완료 - userId: {}", userId);
    }
}
