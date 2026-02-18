package com.back.product.mapper;

import com.back.product.event.spring.ProductCreationCompletedEvent;
import com.back.product.event.spring.ProductDeletionCompletedEvent;
import com.back.product.event.spring.ProductUpdateCompletedEvent;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProductSpringEventMapper {
    public ProductUpdateCompletedEvent toProductUpdatedEvent(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        return ProductUpdateCompletedEvent.builder()
                .productInfoDto(productInfoDto)
                .optionDtos(optionDtos)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public ProductCreationCompletedEvent toProductCreatedEvent(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        String eventId = UUID.randomUUID().toString();

        return ProductCreationCompletedEvent.builder()
                .eventId(eventId)
                .productInfoDto(productInfoDto)
                .optionDtos(optionDtos)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public ProductDeletionCompletedEvent toProductDeletedEvent(Long productInfoId) {
        return ProductDeletionCompletedEvent.builder()
                .productInfoId(productInfoId)
                .build();
    }
}
