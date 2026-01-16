package com.back.notification.domain;

import com.back.common.product.event.InspectionCompletedEvent;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectionCompletedStrategy implements NotificationStrategy<InspectionCompletedEvent> {
    private final NotificationMapper mapper;

    public Type type(){
        return Type.INSPECTION_COMPLETED;
    }

    public List<Notification> create(InspectionCompletedEvent event){
        return List.of(
                mapper.toInspectionCompletedNotification(type(), event, findTarget(event))
        );
    }

    public Long findTarget(InspectionCompletedEvent event) {
        return event.sellerId();
    }
}
