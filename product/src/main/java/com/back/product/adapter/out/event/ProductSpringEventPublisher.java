package com.back.product.adapter.out.event;

import com.back.common.annotation.Loggable;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import com.back.product.mapper.ProductSpringEventMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ProductSpringEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    private final ProductSpringEventMapper productSpringEventMapper;

    @Loggable
    @Transactional
    public void sendCreatedEvent(@Valid ProductInfoDto productInfoDto, @Valid List<ProductDto> productDtos, @Valid String thumbnailUrl) {
        List<OptionDto> optionDtos = productDtos.stream()
                .flatMap(productDto -> productDto.options().stream())
                .toList();

        ProductUpdateCompletedEvent event = productSpringEventMapper.toProductUpdatedEvent(productInfoDto, optionDtos, thumbnailUrl);

        applicationEventPublisher.publishEvent(event);
    }

    @Loggable
    @Transactional
    public void sendModifiedEvent(@Valid ProductInfoDto productInfoDto, @Valid List<ProductDto> productDtos, @Valid String thumbnailUrl) {
        List<OptionDto> optionDtos = productDtos.stream()
                .flatMap(productDto -> productDto.options().stream())
                .toList();

        ProductCreationCompletedEvent event = productSpringEventMapper.toProductCreatedEvent(productInfoDto, optionDtos, thumbnailUrl);

        applicationEventPublisher.publishEvent(event);
    }

    @Loggable
    @Transactional
    public void sendDeletedEvent(@Valid Long productInfoId) {
        ProductDeletionCompletedEvent event = productSpringEventMapper.toProductDeletedEvent(productInfoId);

        applicationEventPublisher.publishEvent(event);
    }
}
