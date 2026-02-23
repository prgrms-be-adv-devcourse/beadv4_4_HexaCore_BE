package com.back.market.app.usecase;

import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.domain.MarketProduct;
import com.back.market.dto.request.MarketProductDto;
import com.back.market.event.resultpayload.ProductUpdatedResultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {
    private final MarketProductRepository marketProductRepository;

    public int updateMarketProduct(List<ProductUpdatedResultPayload> payloads) {
        int updatedCount = 0;
        for(ProductUpdatedResultPayload payload: payloads) {
            MarketProduct marketProduct = marketProductRepository.findById(payload.productOptionId()).orElse(null);
            if(marketProduct != null) {
                MarketProductDto updateDto = new MarketProductDto(
                        payload.name(),
                        payload.productNumber(),
                        payload.thumbnailImage(),
                        payload.releasePrice(),
                        payload.brandName(),
                        payload.categoryName()
                );
                marketProduct.updateInfo(updateDto); //setter 역할
                updatedCount++;
            } else {
                log.warn("[UpdateProductUseCase] 수정 대상 없음 - id : {} ", payload.productOptionId());
            }
        }
        return updatedCount;
    }
}
