package com.back.notification.app.strategy;

import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.InspectionCompletedPayload;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectionCompletedStrategy implements NotificationStrategy<InspectionCompletedPayload> {
    private final NotificationMapper mapper;

    @Override
    public Type type(){
        return Type.INSPECTION_COMPLETED;
    }

    public List<Notification> create(InspectionCompletedPayload payload){
        return List.of(
                mapper.toInspectionCompletedNotification(type(), payload, findTarget(payload))
        );
    }

    public Long findTarget(InspectionCompletedPayload payload) {
        return payload.sellerId();
    }
}
