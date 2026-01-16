package com.back.notification.app;

import com.back.notification.adapter.out.NotificationRepository;
import com.back.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class NotificationSaveUsecase {
    private final NotificationRepository notificationRepository;

    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
}
