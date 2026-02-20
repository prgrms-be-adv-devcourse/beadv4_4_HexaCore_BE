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

    public void register(List<ProductCreatedResultPayload> payloads) {
        for(ProductCreatedResultPayload payload: payloads) {
            if(!marketProductRepository.existsByProductOptionId(payload.productOptionId())) {
                MarketProduct product = marketProductMapper.toEntity(payload);
                marketProductRepository.save(product);
            } else {
                log.info("[RegisterProductUseCase] 이미 존재하는 옵션 건너뜀. optionId: {}", payload.productOptionId());
            }
        }
    }
}
