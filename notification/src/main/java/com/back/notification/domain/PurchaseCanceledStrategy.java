package com.back.notification.domain;

import com.back.common.market.event.PurchaseCanceledEvent;
import com.back.notification.domain.enums.NotificationTargetRole;
import com.back.notification.domain.enums.Type;
import com.back.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PurchaseCanceledStrategy implements NotificationStrategy<PurchaseCanceledEvent> {
    private final NotificationMapper mapper;

    @Override
    public Type type() {
        return Type.PURCHASE_CANCELED;
    }

    public List<Notification> create(PurchaseCanceledEvent event) {
        return findTargets(event).entrySet()
                .stream()
                .map(entry ->
                        mapper.toPurchaseCanceledNotification(type(),
                                event,
                                entry.getValue(),
                                entry.getKey())
                ).toList();
    }

    private Map<NotificationTargetRole, Long> findTargets(PurchaseCanceledEvent event) {
        return Map.of(
                NotificationTargetRole.BUYER, event.buyerUserId(),
                NotificationTargetRole.SELLER, event.sellerUserId()
        );
    }
}
