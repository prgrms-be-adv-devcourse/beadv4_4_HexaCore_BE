package com.back.user.mapper;

import com.back.user.domain.User;
import com.back.user.dto.response.UpdateUserProfileResponseDto;

public class UserMapper {
    public static UpdateUserProfileResponseDto toUpdateUserProfileResponseDto(User user) {
        return new UpdateUserProfileResponseDto(
                user.getId(),
                user.getNickname(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
    }
}
