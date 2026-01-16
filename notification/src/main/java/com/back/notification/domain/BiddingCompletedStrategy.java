package com.back.notification.domain;

import com.back.common.market.event.BiddingCompletedEvent;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BiddingCompletedStrategy implements NotificationStrategy<BiddingCompletedEvent>{
    private final NotificationMapper mapper;

    @Override
    public Type type() {
        return Type.BID_COMPLETED;
    }

    @Override
    public List<Notification> create(BiddingCompletedEvent event) {
        return findTargets(event).entrySet().stream()
                .map(entry ->
                        mapper.toBidCompletedNotification(type(),
                                event,
                                entry.getValue(),   // userId
                                entry.getKey())     // BUYER / SELLER
                )
                .toList();
    }

    private Map<NotificationTargetRole, Long> findTargets(BiddingCompletedEvent event) {
        return Map.of(
                NotificationTargetRole.BUYER, event.buyerUserId(),
                NotificationTargetRole.SELLER, event.sellerUserId()
        );
    }
}
