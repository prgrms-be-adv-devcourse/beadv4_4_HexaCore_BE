package com.back.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequestDto(
        @Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다.")
        String nickname,
        @Size(min = 2, max = 20)
        @Pattern(regexp = "^[a-zA-Z가-힣\\s]+$", message = "이름은 한글 또는 영문만 가능합니다.")
        String name,
        @Pattern(regexp = "^01[0-9]\\d{7,8}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phone,
        @Size(min = 1, max = 250, message = "주소가 너무 짧거나 깁니다.")
        String address) {
    public UpdateUserProfileRequestDto {
        if (nickname != null) nickname = nickname.trim();
        if (name != null) name = name.trim();
        if (phone != null) phone = phone.trim();
        if (address != null) address = address.trim();
    }
}
