package com.back.user.mapper;

import com.back.user.domain.User;
import com.back.user.dto.response.UserProfileResponseDto;

public class UserMapper {
    public static UserProfileResponseDto toUserProfileResponseDto(User user) {
        return new UserProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
    }
}
