package com.back.user.adapter.out;

import com.back.user.domain.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
}
