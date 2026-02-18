package com.back.product.mapper;

import com.back.product.dto.model.BrandDto;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.event.spring.BrandDeletionCompletedEvent;
import com.back.product.event.spring.BrandUpdateCompletedEvent;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BrandSpringEventMapper {
    public BrandCreationCompletedEvent toBrandCreatedEvent(List<BrandDto> brandDtos) {
        String eventId = UUID.randomUUID().toString();

        return BrandCreationCompletedEvent.builder()
                .eventId(eventId)
                .brands(brandDtos)
                .build();
    }

    public BrandUpdateCompletedEvent toBrandUpdatedEvent(BrandDto brandDto) {
        String eventId = UUID.randomUUID().toString();

        return BrandUpdateCompletedEvent.builder()
                .eventId(eventId)
                .brand(brandDto)
                .build();
    }

    public BrandDeletionCompletedEvent toBrandDeletedEvent(Long brandId) {
        String eventId = UUID.randomUUID().toString();

        return BrandDeletionCompletedEvent.builder()
                .eventId(eventId)
                .brandId(brandId)
                .build();
    }
}
