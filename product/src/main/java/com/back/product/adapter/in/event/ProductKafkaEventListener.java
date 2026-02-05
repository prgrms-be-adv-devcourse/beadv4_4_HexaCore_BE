package com.back.product.adapter.in.event;

import com.back.common.product.event.ProductCreatedEvent;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.dto.OptionDto;
import com.back.product.dto.ProductInfoDto;
import com.back.product.mapper.OptionMapper;
import com.back.product.mapper.ProductInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductKafkaEventListener {
    private final ProductDocumentUseCase productDocumentUseCase;
    private final ProductInfoMapper productInfoMapper;
    private final OptionMapper optionMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.product-created}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductCreate(ProductCreatedEvent event) {
        ProductInfoDto productInfoDto = productInfoMapper.toDto(event.productInfo());

        List<OptionDto> optionDtos = event.options().stream()
                .map(optionMapper::toDto).toList();

        String thumbnailUrl = event.thumbnailUrl();

        productDocumentUseCase.syncProduct(productInfoDto, optionDtos, thumbnailUrl);
    }
}
