package com.back.notification.app;

import com.back.notification.domain.Notification;
import com.back.notification.domain.NotificationStrategy;
import com.back.notification.domain.enums.Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationSaveUsecase notificationSaveUsecase;

    private final NotificationStrategyRegistry strategyRegistry;

    public <T> void notify(Type type, T event) {
        NotificationStrategy<T> strategy = strategyRegistry.get(type);

        List<Notification> notifications = strategy.create(event);

        notificationSaveUsecase.saveAll(notifications);
    }

}
