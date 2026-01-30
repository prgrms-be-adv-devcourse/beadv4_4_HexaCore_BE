package com.back.notification.app.pricealert;

import com.back.notification.adapter.out.PriceAlertRepository;
import com.back.notification.domain.PriceAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceAlertDeleteUsecase {
    private final PriceAlertRepository priceAlertRepository;
    private final PriceAlertSupport priceAlertSupport;

    public void delete(Long priceAlertId, Long userId) {
        PriceAlert priceAlert = priceAlertSupport.findById(priceAlertId);
        priceAlertSupport.validateOwner(priceAlert, userId);
        priceAlertRepository.delete(priceAlert);
    }
}
