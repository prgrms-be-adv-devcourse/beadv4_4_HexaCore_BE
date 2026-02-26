package com.back.notification.app.pricealert;

import com.back.notification.adapter.out.feign.product.ProductFeignClient;
import com.back.notification.adapter.out.feign.product.dto.ProductDetailDto;
import com.back.notification.adapter.out.feign.product.dto.ProductDetailListResponse;
import com.back.notification.domain.PriceAlert;
import com.back.notification.dto.request.PriceAlertSaveRequestDto;
import com.back.notification.dto.response.PriceAlertIdDto;
import com.back.notification.dto.response.PriceAlertResponseDto;
import com.back.notification.mapper.PriceAlertMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertFacade {
    private final PriceAlertSaveUsecase priceAlertSaveUsecase;
    private final PriceAlertFindUsecase priceAlertFindUsecase;
    private final PriceAlertDeleteUsecase priceAlertDeleteUsecase;
    private final PriceAlertMapper priceAlertMapper;
    private final ProductFeignClient productFeignClient;

    public PriceAlertIdDto save(PriceAlertSaveRequestDto dto, Long userId) {
        Long id = priceAlertSaveUsecase.save(dto, userId);

        return priceAlertMapper.toPriceAlertIdDto(id);
    }

    public List<PriceAlertResponseDto> findByUserId(Long userId) {
        List<PriceAlert> priceAlerts = priceAlertFindUsecase.findByUserId(userId);

        if (priceAlerts.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = priceAlerts.stream()
                .map(PriceAlert::getProductId)
                .distinct()
                .toList();

        Map<Long, ProductDetailDto> productDetailMap = fetchProductDetailMap(productIds);

        return priceAlertMapper.toPriceAlertResponseDtoList(priceAlerts, productDetailMap);
    }

    public void delete(Long priceAlertId, Long userId) {
        priceAlertDeleteUsecase.delete(priceAlertId, userId);
    }

    private Map<Long, ProductDetailDto> fetchProductDetailMap(List<Long> productIds) {
        try {
            log.info("[PriceAlert] Feign 호출 시작 - productIds: {}", productIds);
            var feignResponse = productFeignClient.getProducts(productIds);
            log.info("[PriceAlert] Feign 응답 - feignResponse: {}, data: {}", feignResponse, feignResponse != null ? feignResponse.getData() : null);
            ProductDetailListResponse response = feignResponse.getData();
            if (response == null || response.getProducts() == null) {
                log.warn("[PriceAlert] response 또는 products가 null - response: {}", response);
                return Collections.emptyMap();
            }
            log.info("[PriceAlert] products 개수: {}", response.getProducts().size());
            // ProductDetailDto 하나에 여러 variant(ProductDto)가 묶여 있으므로
            // 각 variant의 productId를 key로 해당 ProductDetailDto를 매핑
            return response.getProducts().stream()
                    .filter(detail -> detail.getProducts() != null)
                    .flatMap(detail -> detail.getProducts().stream()
                            .map(variant -> Map.entry(variant.getProductId(), detail)))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (existing, replacement) -> existing
                    ));
        } catch (Exception e) {
            log.error("[PriceAlert] product-service Feign 호출 실패: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
