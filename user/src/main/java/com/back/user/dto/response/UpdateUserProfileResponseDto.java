package com.back.user.dto.response;

public record UpdateUserProfileResponseDto(Long userId, String email, String nickname, String name, String phone, String address) {
}
