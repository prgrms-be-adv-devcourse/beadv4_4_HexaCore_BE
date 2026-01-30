package com.back.notification.app.pricealert;

import com.back.notification.adapter.out.PriceAlertRepository;
import com.back.notification.domain.PriceAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceAlertFindUsecase {
    private final PriceAlertRepository priceAlertRepository;

    public List<PriceAlert> findByUserId(Long userId) {
        return priceAlertRepository.findByUserId(userId);
    }
}
