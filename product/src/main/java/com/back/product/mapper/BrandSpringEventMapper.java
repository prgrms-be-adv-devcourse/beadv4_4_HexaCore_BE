package com.back.product.mapper;

import com.back.product.dto.model.BrandDto;
import com.back.product.event.spring.BrandCreationCompletedEvent;
import com.back.product.event.spring.BrandDeletionCompletedEvent;
import com.back.product.event.spring.BrandUpdateCompletedEvent;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BrandSpringEventMapper {
    public BrandCreationCompletedEvent toBrandCreatedEvent(List<BrandDto> brandDtos) {
        return BrandCreationCompletedEvent.builder()
                .brands(brandDtos)
                .build();
    }

    public BrandUpdateCompletedEvent toBrandUpdatedEvent(BrandDto brandDto) {
        return BrandUpdateCompletedEvent.builder()
                .brand(brandDto)
                .build();
    }

    public BrandDeletionCompletedEvent toBrandDeletedEvent(Long brandId) {
        return BrandDeletionCompletedEvent.builder()
                .brandId(brandId)
                .build();
    }
}
