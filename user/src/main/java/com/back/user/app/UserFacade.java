package com.back.user.app;

import com.back.common.event.KafkaEventPublisher;
import com.back.common.user.event.FcmTokenChangedEvent;
import com.back.user.domain.User;
import com.back.user.dto.request.UpdateFcmTokenRequest;
import com.back.user.dto.request.UpdateNotificationSettingsRequest;
import com.back.user.dto.request.UpdateUserProfileRequestDto;
import com.back.user.dto.response.NotificationSettingResponse;
import com.back.user.dto.response.UserProfileResponseDto;
import com.back.user.dto.response.UserIdResponse;
import com.back.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFacade {
    private final UserSupport userSupport;
    private final UserUpdateUseCase userUpdateUsecase;
    private final UserGetProfileUseCase userGetProfileUseCase;
    private final UserIncrementBlindCountUseCase userIncrementBlindCountUseCase;
    private final GetNotificationSettingsUseCase getNotificationSettingsUseCase;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Transactional
    public UserIdResponse registerOrUpdateFcmToken(Long userId, UpdateFcmTokenRequest request) {
        User user = userSupport.findById(userId);

        userUpdateUsecase.updateFcmToken(user, request);

        // FCM 토큰 변경 이벤트 발행
        FcmTokenChangedEvent event = new FcmTokenChangedEvent(userId, request.fcmToken());
        kafkaEventPublisher.publish(event);
        log.info("[UserFacade] FCM 토큰 변경 이벤트 발행 - UserId: {}", userId);

        return UserIdResponse.of(user);
    }

    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSettings(Long userId) {
        return getNotificationSettingsUseCase.execute(userId);
    }

    @Transactional
    public UserIdResponse updateNotificationSettings(Long userId, UpdateNotificationSettingsRequest request) {
        User user = userSupport.findById(userId);

        userUpdateUsecase.updateNotificationSettings(user, request);

        return UserIdResponse.of(user);
    }

    @Transactional
    public void incrementBlindCount(Long userId) {
        userIncrementBlindCountUseCase.incrementBlindCount(userId);
    }

    @Transactional
    public void incrementBlindCount(Long userId, LocalDateTime now) {
        userIncrementBlindCountUseCase.incrementBlindCount(userId, now);
    }

    @Transactional
    public LocalDateTime getChatRestrictedUntil(Long userId){
        User user = userSupport.findById(userId);
        return user.getChatRestrictedUntil();
    }

    @Transactional
    public UserProfileResponseDto updateUserProfile(Long userId, UpdateUserProfileRequestDto request) {
        User user = userSupport.findById(userId);
        userUpdateUsecase.updateUserProfile(user, request);
        return UserMapper.toUserProfileResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile(Long userId) {
        User user = userGetProfileUseCase.getUserProfile(userId);
        return UserMapper.toUserProfileResponseDto(user);
    }
}
