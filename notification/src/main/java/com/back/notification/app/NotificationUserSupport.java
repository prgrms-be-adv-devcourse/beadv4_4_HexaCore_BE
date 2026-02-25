package com.back.notification.app;

import com.back.notification.adapter.out.NotificationUserRepository;
import com.back.notification.domain.NotificationUser;
import com.back.notification.exception.NotificationUserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NotificationUserSupport {
    private final NotificationUserRepository notificationUserRepository;

    public String findFcmToken(Long userId) {
        return notificationUserRepository.findFcmTokenByUserId(userId)
                .orElse(null);
    }

    public NotificationUser findById(Long userId) {
        return notificationUserRepository.findById(userId)
                .orElseThrow(NotificationUserNotFoundException::new);
    }

    public Map<Long, NotificationUser> findAllByIdAsMap(List<Long> userIds) {
        List<NotificationUser> result = new ArrayList<>();
        int chunkSize = 1000;
        for (int i = 0; i < userIds.size(); i += chunkSize) {
            List<Long> chunk = userIds.subList(i, Math.min(i + chunkSize, userIds.size()));
            result.addAll(notificationUserRepository.findAllById(chunk));
        }
        return result.stream()
                .collect(Collectors.toMap(NotificationUser::getId, u -> u));
    }
}
