package com.back.user.app;

import com.back.user.domain.User;
import com.back.user.domain.UserSetting;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.request.UpdateNotificationSettingsRequest;
import com.back.user.dto.request.UpdateUserProfileRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUpdateUseCase {
    private final UserSupport userSupport;

    public void updateFcmToken(User user, UpdateFcmTokenRequest request) {
        user.updateFcmToken(request.fcmToken());
    }

    public void updateNotificationSettings(User user, UpdateNotificationSettingsRequest request) {
        UserSetting setting = userSupport.findUserSettingById(user.getId());
        if (request.bidStatusEnabled() != null) {
            setting.setBidStatusEnabled(request.bidStatusEnabled());
        }
        if (request.productStatusEnabled() != null) {
            setting.setProductStatusEnabled(request.productStatusEnabled());
        }
        if (request.priceEnabled() != null) {
            setting.setPriceEnabled(request.priceEnabled());
        }
        if (request.settlementEnabled() != null) {
            setting.setSettlementEnabled(request.settlementEnabled());
        }
    }

    public void updateUserProfile(User user, UpdateUserProfileRequestDto request) {
        userSupport.validateNicknameAvailable(request.nickname(), user.getNickname());
        user.updateProfile(request);
    }
}
