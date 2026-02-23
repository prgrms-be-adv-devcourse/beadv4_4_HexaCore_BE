package com.back.product.app.facade;

import com.back.common.annotation.Loggable;
import com.back.product.app.usecase.EventLogUseCase;
import com.back.product.app.usecase.ProductDocumentUseCase;
import com.back.product.dto.command.EventConsumptionCommand;
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
    public void syncCreatedProduct(EventConsumptionCommand command, @Valid ProductCreatedPayload payload) {
        String eventId = command.eventId();

        if (eventLogUseCase.isAlreadyProcessed(eventId)) {
            log.info("[EventConsumptionFacade] {}-{} is already processed.", command.eventType(), eventId);
            return;
        }

        try {
            eventLogUseCase.startProcessing(command);

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
    public void syncUpdatedProduct(EventConsumptionCommand command, @Valid ProductUpdatedPayload payload) {
        String eventId = command.eventId();

        if (eventLogUseCase.isAlreadyProcessed(eventId)) {
            log.info("[EventConsumptionFacade] {}-{} is already processed.", command.eventType(), eventId);
            return;
        }

        try {
            eventLogUseCase.startProcessing(command);

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
    public void syncDeletedProduct(EventConsumptionCommand command, @Valid ProductDeletedPayload payload) {
        String eventId = command.eventId();

        if (eventLogUseCase.isAlreadyProcessed(eventId)) {
            log.info("[EventConsumptionFacade] {}-{} is already processed.", command.eventType(), eventId);
            return;
        }

        try {
            eventLogUseCase.startProcessing(command);

            productDocumentUseCase.deleteProduct(payload.productInfoId());

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
    public void eventFailedLog(String eventId, String errorMessage) {
        eventLogUseCase.markAsDead(eventId, errorMessage);
    }
}
