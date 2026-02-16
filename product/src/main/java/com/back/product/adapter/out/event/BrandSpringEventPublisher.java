package com.back.product.adapter.out.event;


import com.back.common.annotation.Loggable;
import com.back.product.dto.model.BrandDto;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.event.spring.BrandDeletionCompletedEvent;
import com.back.product.event.spring.BrandUpdateCompletedEvent;
import com.back.product.mapper.BrandSpringEventMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BrandSpringEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    private final BrandSpringEventMapper brandSpringEventMapper;

    @Loggable
    public void sendCreatedEvent(@Valid List<BrandDto> brandDtos) {
        BrandCreationCompletedEvent event = brandSpringEventMapper.toBrandCreatedEvent(brandDtos);

        applicationEventPublisher.publishEvent(event);
    }

    @Loggable
    public void sendUpdatedEvent(@Valid BrandDto brandDto) {
        BrandUpdateCompletedEvent event = brandSpringEventMapper.toBrandUpdatedEvent(brandDto);

        applicationEventPublisher.publishEvent(event);
    }

    @Loggable
    public void sendDeletedEvent(@Valid Long brandId) {
        BrandDeletionCompletedEvent event = brandSpringEventMapper.toBrandDeletedEvent(brandId);

        applicationEventPublisher.publishEvent(event);
    }
}
