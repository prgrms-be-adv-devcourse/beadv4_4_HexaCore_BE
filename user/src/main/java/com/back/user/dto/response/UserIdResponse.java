package com.back.user.dto.response;

import com.back.user.domain.User;
import lombok.Builder;

@Builder
public record UserIdResponse(
        Long userId
) {
    public static UserIdResponse of(User user) {
        return UserIdResponse.builder()
                .userId(user.getId())
                .build();
    }
}
