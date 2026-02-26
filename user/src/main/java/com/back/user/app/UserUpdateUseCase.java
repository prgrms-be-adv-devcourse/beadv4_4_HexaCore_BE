package com.back.user.app;

import com.back.user.domain.User;
import com.back.user.domain.UserSetting;
import com.back.user.domain.event.UserSettingUpdatedEvent;
import com.back.user.domain.event.UserUpdatedEvent;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.request.UpdateNotificationSettingsRequest;
import com.back.user.dto.request.UpdateUserProfileRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUpdateUseCase {
    private final UserSupport userSupport;
    private final ApplicationEventPublisher eventPublisher;

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
        eventPublisher.publishEvent(new UserSettingUpdatedEvent(
                user.getId(),
                setting.isBidStatusEnabled(),
                setting.isProductStatusEnabled(),
                setting.isPriceEnabled(),
                setting.isSettlementEnabled()
        ));
    }

    public void updateUserProfile(User user, UpdateUserProfileRequestDto request) {
        userSupport.validateNicknameAvailable(request.nickname(), user.getNickname());
        user.updateProfile(request);
        eventPublisher.publishEvent(UserUpdatedEvent.builder()
                .id(user.getId())
                .nickname(request.nickname())
                .address(user.getAddress())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .build());
    }
}
