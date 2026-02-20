package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.product.app.usecase.EventLogUseCase;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
import com.back.product.mapper.OptionMapper;
import com.back.product.mapper.ProductInfoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class EventConsumptionFacade {
    private final ProductDocumentUseCase productDocumentUseCase;
    private final EventLogUseCase eventLogUseCase;
    private final ProductInfoMapper productInfoMapper;
    private final OptionMapper optionMapper;

    @Loggable
    @Transactional
    public void syncCreatedProduct(String eventId, String eventType, String message, @Valid ProductCreatedPayload payload) {
        if (eventLogUseCase.isAlreadyProcessed(eventId)) {
            log.info("[EventConsumptionFacade] {}-{} is already processed.", eventType, eventId);
            return;
        }

        try {
            eventLogUseCase.startProcessing(eventId, eventType, message);

            ProductInfoDto productInfoDto = productInfoMapper.toDto(payload.productInfo());
            List<OptionDto> optionDtos = payload.options().stream().map(optionMapper::toDto).toList();
            String thumbnailUrl = payload.thumbnailUrl();
            productDocumentUseCase.syncProduct(productInfoDto, optionDtos, thumbnailUrl);

            eventLogUseCase.markAsSuccess(eventId);
        } catch (DataIntegrityViolationException | IllegalStateException e) {
            log.info("[EventConsumptionFacade] Event {} is already processing or completed.", eventId);
        } catch (Exception e) {
            eventLogUseCase.markAsFailed(eventId, e.getMessage());
            throw e;
        }
    }

    @Loggable
    @Transactional
    public void syncUpdatedProduct(String eventId, String eventType, String message, @Valid ProductUpdatedPayload payload) {
        if (eventLogUseCase.isAlreadyProcessed(eventId)) {
            log.info("[EventConsumptionFacade] {}-{} is already processed.", eventType, eventId);
            return;
        }

        try {
            eventLogUseCase.startProcessing(eventId, eventType, message);

            ProductInfoDto productInfoDto = productInfoMapper.toDto(payload.productInfo());
            List<OptionDto> optionDtos = payload.options().stream().map(optionMapper::toDto).toList();
            String thumbnailUrl = payload.thumbnailUrl();
            productDocumentUseCase.syncProduct(productInfoDto, optionDtos, thumbnailUrl);

            eventLogUseCase.markAsSuccess(eventId);
        } catch (DataIntegrityViolationException | IllegalStateException e) {
            log.info("[EventConsumptionFacade] Event {} is already processing or completed.", eventId);
        } catch (Exception e) {
            eventLogUseCase.markAsFailed(eventId, e.getMessage());
            throw e;
        }
    }

    @Loggable
    @Transactional
    public void syncDeletedProduct(String eventId, String eventType, String message, @Valid ProductDeletedPayload payload) {
        if (eventLogUseCase.isAlreadyProcessed(eventId)) {
            log.info("[EventConsumptionFacade] {}-{} is already processed.", eventType, eventId);
            return;
        }

        try {
            eventLogUseCase.startProcessing(eventId, eventType, message);

            productDocumentUseCase.deleteProduct(payload.productInfoId());

            eventLogUseCase.markAsSuccess(eventId);
        } catch (DataIntegrityViolationException | IllegalStateException e) {
            log.info("[EventConsumptionFacade] Event {} is already processing or completed.", eventId);
        } catch (Exception e) {
            eventLogUseCase.markAsFailed(eventId, e.getMessage());
            throw e;
        }
    }
}
