package com.back.user.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.exception.EntityNotFoundException;
import com.back.user.adapter.out.UserRepository;
import com.back.user.adapter.out.UserSettingRepository;
import com.back.user.domain.User;
import com.back.user.domain.UserSetting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserSupport {
    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
    }

    public UserSetting findUserSettingById(Long userId) {
        return userSettingRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_SETTING_NOT_FOUND));
    }

    public void validateNicknameAvailable(String nickname, String userNickname) {
        if ( nickname != null && !nickname.equals(userNickname)) {
            if (userRepository.existsByNickname(nickname)) {
                throw new CustomException(FailureCode.NICKNAME_ALREADY_EXISTS);
            }
        }
    }

    public int incrementBlindCount(Long userId, LocalDateTime now, int threshold, long restrictSeconds) {
        return userRepository.incrementBlindCount(userId, now, threshold, restrictSeconds);
    }

    public LocalDateTime getRestrictedUntil(Long userId){
        return userRepository.getRestrictedUntil(userId);
    }
}
