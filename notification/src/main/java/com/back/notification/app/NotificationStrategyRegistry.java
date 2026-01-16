package com.back.notification.app;

import com.back.notification.domain.NotificationStrategy;
import com.back.notification.domain.enums.Type;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationStrategyRegistry {
    private final Map<Type, NotificationStrategy<?>> strategies = new HashMap<>();

    public NotificationStrategyRegistry(List<NotificationStrategy<?>> strategies) {
        strategies.forEach(strategy -> this.strategies.put(strategy.type(), strategy));
    }

    @SuppressWarnings("unchecked")
    public <T> NotificationStrategy<T> get(Type type) {
        return (NotificationStrategy<T>) strategies.get(type);
    }
}
