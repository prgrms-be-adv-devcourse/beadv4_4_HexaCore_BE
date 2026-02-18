package com.back.product.adapter.in.event;

import com.back.common.event.Envelope;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.mapper.OptionMapper;
import com.back.product.mapper.ProductInfoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ProductKafkaEventListener {
    private final JsonMapper jsonMapper;
    private final ProductDocumentUseCase productDocumentUseCase;
    private final ProductInfoMapper productInfoMapper;
    private final OptionMapper optionMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductCreate(String message) {
        try {
            Envelope<ProductCreatedPayload> event = jsonMapper.readValue(message, new TypeReference<>() {});

            ProductCreatedPayload payload = event.payload();

            processSyncCreatedProduct(payload);
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] ProductCreatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductUpdate(String message) {
        try {
            Envelope<ProductUpdatedPayload> event = jsonMapper.readValue(message, new TypeReference<>() {});

            ProductUpdatedPayload payload = event.payload();

            processSyncUpdatedProduct(payload);
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] ProductUpdatedPayload 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void handleProductDelete(String message) {
        try {
            Envelope<ProductDeletedPayload> event = jsonMapper.readValue(message, new TypeReference<>() {});

            ProductDeletedPayload payload = event.payload();

            processSyncDeletedProduct(payload);
        } catch (JacksonException e) {
            log.error("[KafkaListenerFailed] ProductDeletedEvent 역직렬화 중 에러 발생 : {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void processSyncCreatedProduct(@Valid ProductCreatedPayload payload) {
        log.info("[KafkaListenerSuccess] ProductCreatedEvent 수신 : productInfo = {}", payload.productInfo());

        ProductInfoDto productInfoDto = productInfoMapper.toDto(payload.productInfo());
        List<OptionDto> optionDtos = payload.options().stream().map(optionMapper::toDto).toList();
        String thumbnailUrl = payload.thumbnailUrl();

        productDocumentUseCase.syncProduct(productInfoDto, optionDtos, thumbnailUrl);
    }

    @Transactional
    public void processSyncUpdatedProduct(@Valid ProductUpdatedPayload payload) {
        log.info("[KafkaListenerSuccess] ProductUpdatedEvent 수신 : productInfo = {}", payload.productInfo());

        ProductInfoDto productInfoDto = productInfoMapper.toDto(payload.productInfo());
        List<OptionDto> optionDtos = payload.options().stream().map(optionMapper::toDto).toList();
        String thumbnailUrl = payload.thumbnailUrl();

        productDocumentUseCase.syncProduct(productInfoDto, optionDtos, thumbnailUrl);
    }

    @Transactional
    public void processSyncDeletedProduct(@Valid ProductDeletedPayload payload) {
        log.info("[KafkaListenerSuccess] ProductDeletedEvent 수신 : productInfoId = {}", payload.productInfoId());

        productDocumentUseCase.deleteProduct(payload.productInfoId());
    }
}
