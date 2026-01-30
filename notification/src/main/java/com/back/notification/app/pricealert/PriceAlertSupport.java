package com.back.notification.app.pricealert;

import com.back.common.market.event.SellBiddingCreatedEvent;
import com.back.notification.adapter.out.PriceAlertRepository;
import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.PriceAlert;
import com.back.notification.exception.PriceAlertNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceAlertSupport {
    private final PriceAlertRepository priceAlertRepository;

    @Transactional
    public List<NotificationUser> findUsersForPriceDropAlert(SellBiddingCreatedEvent event) {
        LocalDateTime now = LocalDateTime.now();

        List<PriceAlert> priceAlerts = priceAlertRepository.findEligibleAlerts(event.productId(),
                event.currentPrice(), now.minusDays(1));

        priceAlerts.forEach(alert -> alert.trigger(now));

        return priceAlerts.stream()
                .map(PriceAlert::getUser)
                .distinct()
                .toList();
    }

    public PriceAlert findById(Long priceAlertId) {
        return priceAlertRepository.findById(priceAlertId)
                .orElseThrow(PriceAlertNotFoundException::new);
    }
}
