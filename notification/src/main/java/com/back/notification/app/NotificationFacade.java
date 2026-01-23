package com.back.notification.app;

import com.back.notification.app.strategy.NotificationStrategyRegistry;
import com.back.notification.domain.Notification;
import com.back.notification.app.strategy.NotificationStrategy;
import com.back.notification.domain.NotificationUser;
import com.back.notification.dto.NotificationCreatedEvent;
import com.back.notification.dto.NotificationIdResponseDto;
import com.back.notification.dto.NotificationMessage;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.response.NotificationListResponseDto;
import com.back.notification.exception.NotificationAccessDeniedException;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationSaveUsecase notificationSaveUsecase;
    private final NotificationSupport notificationSupport;
    private final NotificationUserSupport notificationUserSupport;
    private final NotificationUpdateUsecase notificationUpdateUsecase;

    private final NotificationStrategyRegistry strategyRegistry;
    private final NotificationMessageFactory notificationMessageFactory;

    private final NotificationMapper mapper;

    private final ApplicationEventPublisher eventPublisher;

    public <T> void notify(Type type, T event) {
        NotificationStrategy<T> strategy = strategyRegistry.get(type);

        List<Notification> notifications = strategy.create(event);

        Locale locale = Locale.KOREA;
        notifications.forEach(notification -> {
            NotificationMessage message = notificationMessageFactory.create(notification, locale);
            notification.applyMessage(message, locale);
        });

        notificationSaveUsecase.saveAll(notifications);

        NotificationCreatedEvent createdEvent = mapper.toNotificationCreatedEvent(notifications);
        eventPublisher.publishEvent(createdEvent);
    }

    @Transactional
    public NotificationIdResponseDto markUserNotificationAsRead(Long userId, String notificationId) {
        Notification notification = notificationSupport.findById(notificationId);
        NotificationUser user = notificationUserSupport.findById(userId);

        validateOwnership(notification, user);

        if(!notification.isRead())
            notificationUpdateUsecase.markUserNotificationAsRead(notification);

        return mapper.toNotificationIdResponseDto(notification);
    }

    private void validateOwnership(Notification notification, NotificationUser user) {
        if(!notification.getUserId().equals(user.getId())) {
            throw new NotificationAccessDeniedException();
        }
    }

    public NotificationListResponseDto getRecentNotifications(Long userId, int pageNumber, int pageSize) {
        NotificationUser user = notificationUserSupport.findById(userId);
        Pageable pageable = PageRequest.of(pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Slice<Notification> notifications = notificationSupport.findRecentNotifications(user, pageable);

        return mapper.toNotificationListResponseDto(notifications);
    }
}
