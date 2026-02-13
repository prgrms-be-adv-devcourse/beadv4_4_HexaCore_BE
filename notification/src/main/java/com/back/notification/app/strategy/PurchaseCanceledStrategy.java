package com.back.notification.app.strategy;

import com.back.notification.domain.Notification;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.dto.payload.PurchaseCanceledPayload;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PurchaseCanceledStrategy implements NotificationStrategy<PurchaseCanceledPayload> {
    private final NotificationMapper mapper;

    @Override
    public Type type() {
        return Type.PURCHASE_CANCELED;
    }

    public List<Notification> create(PurchaseCanceledPayload payload) {
        return findTargets(payload).entrySet()
                .stream()
                .map(entry ->
                        mapper.toPurchaseCanceledNotification(type(),
                                payload,
                                entry.getValue(),
                                entry.getKey())
                ).toList();
    }

    private Map<NotificationTargetRole, Long> findTargets(PurchaseCanceledPayload payload) {
        return Map.of(
                NotificationTargetRole.BUYER, payload.buyerUserId(),
                NotificationTargetRole.SELLER, payload.sellerUserId()
        );
    }
}
