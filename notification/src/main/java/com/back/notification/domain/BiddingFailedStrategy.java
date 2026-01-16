package com.back.notification.domain;

import com.back.common.market.event.BiddingFailedEvent;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BiddingFailedStrategy implements NotificationStrategy<BiddingFailedEvent> {
    private final NotificationMapper mapper;

    public Type type(){
        return Type.BID_FAILED;
    }

    public List<Notification> create(BiddingFailedEvent event){
        return List.of(
                mapper.toBidFailedNotification(type(), event, findTarget(event), NotificationTargetRole.SELLER)
        );
    }

    public Long findTarget(BiddingFailedEvent event) {
        return event.sellerUserId();
    }
}
