package com.back.notification.domain;

import com.back.common.Settlement.event.SettlementCompletedEvent;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SettlementCompletedStrategy implements NotificationStrategy<SettlementCompletedEvent>{
    private final NotificationMapper mapper;

    public Type type(){
        return Type.SETTLEMENT_COMPLETED;
    }

    public List<Notification> create(SettlementCompletedEvent event){
        return List.of(
                mapper.toSettlementCompletedNotification(type(), event, findTarget(event))
        );
    }

    public Long findTarget(SettlementCompletedEvent event) {
        return event.sellerId();
    }
}
