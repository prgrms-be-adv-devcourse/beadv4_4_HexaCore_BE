package com.back.notification.app.strategy;

import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.BiddingFailedPayload;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BiddingFailedStrategy implements NotificationStrategy<BiddingFailedPayload> {
    private final NotificationMapper mapper;

    public Type type(){
        return Type.BID_FAILED;
    }

    public List<Notification> create(BiddingFailedPayload payload){
        return List.of(
                mapper.toBidFailedNotification(type(), payload, findTarget(payload), NotificationTargetRole.SELLER)
        );
    }

    public Long findTarget(BiddingFailedPayload payload) {
        return payload.sellerUserId();
    }
}
