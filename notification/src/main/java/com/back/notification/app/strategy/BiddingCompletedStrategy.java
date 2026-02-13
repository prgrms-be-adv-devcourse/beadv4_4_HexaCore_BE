package com.back.notification.app.strategy;

import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.BiddingCompletedPayload;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BiddingCompletedStrategy implements NotificationStrategy<BiddingCompletedPayload> {
    private final NotificationMapper mapper;

    @Override
    public Type type() {
        return Type.BID_COMPLETED;
    }

    @Override
    public List<Notification> create(BiddingCompletedPayload payload) {
        return findTargets(payload).entrySet().stream()
                .map(entry ->
                        mapper.toBidCompletedNotification(type(),
                                payload,
                                entry.getValue(),   // userId
                                entry.getKey())     // BUYER / SELLER
                )
                .toList();
    }

    private Map<NotificationTargetRole, Long> findTargets(BiddingCompletedPayload payload) {
        return Map.of(
                NotificationTargetRole.BUYER, payload.buyerUserId(),
                NotificationTargetRole.SELLER, payload.sellerUserId()
        );
    }
}
