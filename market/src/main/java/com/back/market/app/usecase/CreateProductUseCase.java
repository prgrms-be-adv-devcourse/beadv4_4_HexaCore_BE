package com.back.market.app.usecase;

import com.back.market.adapter.out.MarketProductRepository;
import com.back.market.domain.MarketProduct;
import com.back.market.event.resultpayload.ProductCreatedResultPayload;
import com.back.market.mapper.MarketProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
    private final MarketProductRepository marketProductRepository;
    private final MarketProductMapper marketProductMapper;

    public int createMarketProduct(List<ProductCreatedResultPayload> payloads) {
        int savedCount = 0;
        for(ProductCreatedResultPayload payload: payloads) {
            if(!marketProductRepository.existsById(payload.productOptionId())) {
                MarketProduct product = marketProductMapper.toEntity(payload);
                marketProductRepository.save(product);
                savedCount++;
                log.info("  >> [신규 등록] 옵션ID: {}, 옵션값: {}", payload.productOptionId(), payload.productOption());
            } else {
                log.info("[CreateProductUseCase] 이미 존재하는 옵션 건너뜀. optionId: {}", payload.productOptionId());
            }
        }
        return savedCount;
    }
}
