package com.back.notification.app.pricealert;

import com.back.notification.domain.PriceAlert;
import com.back.notification.dto.request.PriceAlertSaveRequestDto;
import com.back.notification.dto.response.PriceAlertIdDto;
import com.back.notification.dto.response.PriceAlertResponseDto;
import com.back.notification.mapper.PriceAlertMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAlertFacade {
    private final PriceAlertSaveUsecase priceAlertSaveUsecase;
    private final PriceAlertFindUsecase priceAlertFindUsecase;
    private final PriceAlertMapper priceAlertMapper;

    public PriceAlertIdDto save(PriceAlertSaveRequestDto dto, Long userId) {
        Long id = priceAlertSaveUsecase.save(dto, userId);

        return priceAlertMapper.toPriceAlertIdDto(id);
    }

    public List<PriceAlertResponseDto> findByUserId(Long userId) {
        List<PriceAlert> priceAlerts = priceAlertFindUsecase.findByUserId(userId);

        return priceAlertMapper.toPriceAlertResponseDtoList(priceAlerts);
    }
}
