package com.back.user.app;

import com.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserGetProfileUseCase {

    private final UserSupport userSupport;

    public User getUserProfile(Long userId) {
        return userSupport.findById(userId);
    }
}
