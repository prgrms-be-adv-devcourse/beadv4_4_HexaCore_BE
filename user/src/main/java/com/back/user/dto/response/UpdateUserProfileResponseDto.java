package com.back.user.dto.response;

public record UpdateUserProfileResponseDto(Long userId, String nickname, String name, String phone, String address) {
}
