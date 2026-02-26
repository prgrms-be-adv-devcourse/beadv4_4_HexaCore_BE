package com.back.notification.mapper;

import com.back.notification.adapter.out.feign.product.dto.ProductDetailDto;
import com.back.notification.domain.NotificationUser;
import com.back.notification.domain.PriceAlert;
import com.back.notification.dto.request.PriceAlertSaveRequestDto;
import com.back.notification.dto.response.PriceAlertIdDto;
import com.back.notification.dto.response.PriceAlertResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    public PriceAlertResponseDto toPriceAlertResponseDto(PriceAlert priceAlert, ProductDetailDto productDetail) {
        ProductDetailDto filteredDetail = filterProductDetail(priceAlert.getProductId(), productDetail);
        return PriceAlertResponseDto.builder()
                .id(priceAlert.getId())
                .productId(priceAlert.getProductId())
                .targetPrice(priceAlert.getTargetPrice())
                .triggeredAt(priceAlert.getTriggeredAt())
                .createdAt(priceAlert.getCreatedAt())
                .productDetail(filteredDetail)
                .build();
    }

    private ProductDetailDto filterProductDetail(Long productId, ProductDetailDto productDetail) {
        if (productDetail == null || productDetail.getProducts() == null) {
            return productDetail;
        }
        return productDetail.withFilteredProduct(productId);
    }

    public List<PriceAlertResponseDto> toPriceAlertResponseDtoList(List<PriceAlert> priceAlerts,
                                                                    Map<Long, ProductDetailDto> productDetailMap) {
        return priceAlerts.stream()
                .map(priceAlert -> toPriceAlertResponseDto(
                        priceAlert,
                        productDetailMap.get(priceAlert.getProductId())
                ))
                .toList();
    }
}
