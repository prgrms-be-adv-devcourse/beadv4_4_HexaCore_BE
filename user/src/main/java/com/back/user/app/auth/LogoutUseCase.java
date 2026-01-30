package com.back.user.app.auth;

import com.back.user.adapter.out.RefreshStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {
    private final RefreshStore refreshStore;

    public void logout(Long userId) {
        refreshStore.revoke(userId);
    }
}
