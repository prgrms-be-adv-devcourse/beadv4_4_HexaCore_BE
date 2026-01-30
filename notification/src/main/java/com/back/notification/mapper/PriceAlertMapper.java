package com.back.notification.mapper;

import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.PriceAlert;
import com.back.notification.dto.request.PriceAlertSaveRequestDto;
import com.back.notification.dto.response.PriceAlertIdDto;
import com.back.notification.dto.response.PriceAlertResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PriceAlertMapper {
    public PriceAlert toPriceAlert(PriceAlertSaveRequestDto dto, NotificationUser user, Long productId) {
        return PriceAlert.builder()
                .user(user)
                .productId(productId)
                .targetPrice(dto.targetPrice())
                .build();
    }

    public PriceAlertIdDto toPriceAlertIdDto(Long id) {
        return PriceAlertIdDto.builder()
                .id(id)
                .build();
    }

    public PriceAlertResponseDto toPriceAlertResponseDto(PriceAlert priceAlert) {
        return PriceAlertResponseDto.builder()
                .id(priceAlert.getId())
                .productId(priceAlert.getProductId())
                .targetPrice(priceAlert.getTargetPrice())
                .triggeredAt(priceAlert.getTriggeredAt())
                .createdAt(priceAlert.getCreatedAt())
                .build();
    }

    public List<PriceAlertResponseDto> toPriceAlertResponseDtoList(List<PriceAlert> priceAlerts) {
        return priceAlerts.stream()
                .map(this::toPriceAlertResponseDto)
                .toList();
    }
}
