package com.back.notification.app.strategy;

import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.SettlementCompletedPayload;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementCompletedStrategy implements NotificationStrategy<SettlementCompletedPayload> {
    private final NotificationMapper mapper;

    @Override
    public Type type(){
        return Type.SETTLEMENT_COMPLETED;
    }

    public List<Notification> create(SettlementCompletedPayload payload){
        return List.of(
                mapper.toSettlementCompletedNotification(type(), payload, findTarget(payload))
        );
    }

    public Long findTarget(SettlementCompletedPayload payload) {
        return payload.sellerId();
    }
}
