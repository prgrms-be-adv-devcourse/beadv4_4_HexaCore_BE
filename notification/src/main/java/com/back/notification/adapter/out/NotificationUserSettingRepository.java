package com.back.notification.adapter.out;

import com.back.notification.domain.NotificationUserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationUserSettingRepository extends JpaRepository<NotificationUserSetting, Long> {
    Optional<NotificationUserSetting> findByUser_Id(Long userId);
}
