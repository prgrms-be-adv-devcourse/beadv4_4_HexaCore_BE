package com.back.user.app;

import com.back.user.domain.UserSetting;
import com.back.user.dto.response.NotificationSettingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetNotificationSettingsUseCase {
    private final UserSupport userSupport;

    @Transactional(readOnly = true)
    public NotificationSettingResponse execute(Long userId) {
        UserSetting setting = userSupport.findUserSettingById(userId);
        
        return new NotificationSettingResponse(
                setting.isBidStatusEnabled(),
                setting.isProductStatusEnabled(),
                setting.isPriceEnabled(),
                setting.isSettlementEnabled()
        );
    }
}
