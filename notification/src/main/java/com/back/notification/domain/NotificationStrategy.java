package com.back.notification.domain;

import com.back.notification.domain.enums.Type;

import java.util.List;

public interface NotificationStrategy<T> {
    Type type();
    List<Notification> create(T dto);
}
