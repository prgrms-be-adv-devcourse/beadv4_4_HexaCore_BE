package com.back.user.app;

import com.back.common.code.FailureCode;
import com.back.common.exception.EntityNotFoundException;
import com.back.user.adapter.out.UserRepository;
import com.back.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSupport {
    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.USER_NOT_FOUND));
    }

}
