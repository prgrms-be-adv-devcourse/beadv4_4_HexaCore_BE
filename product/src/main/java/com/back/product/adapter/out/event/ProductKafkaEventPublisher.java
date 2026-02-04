package com.back.product.adapter.out.event;

import com.back.common.event.KafkaEventPublisher;
import com.back.common.product.event.ProductCreatedEvent;
import com.back.common.product.event.payload.BrandPayload;
import com.back.common.product.event.payload.CategoryPayload;
import com.back.common.product.event.payload.OptionPayload;
import com.back.common.product.event.payload.ProductInfoPayload;
import com.back.product.dto.OptionDto;
import com.back.product.dto.ProductInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductKafkaEventPublisher {
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${custom.kafka.topic.product-created}")
    private String productCreatedTopic;

    public void sendCreatedEvent(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        ProductCreatedEvent event = new ProductCreatedEvent(
                toProductInfoPayload(productInfoDto),
                toOptionPayload(optionDtos),
                thumbnailUrl
        );

        kafkaEventPublisher.publish(productCreatedTopic, event);
    }

    private ProductInfoPayload toProductInfoPayload(ProductInfoDto productInfoDto) {
        return new ProductInfoPayload(
                productInfoDto.productInfoId(),
                new BrandPayload(
                        productInfoDto.brand().brandId(),
                        productInfoDto.brand().name()
                ),
                new CategoryPayload(
                        productInfoDto.category().categoryId(),
                        productInfoDto.category().name()
                ),
                productInfoDto.name(),
                productInfoDto.code(),
                productInfoDto.releasePrice(),
                productInfoDto.releaseDate()
        );
    }

    private List<OptionPayload> toOptionPayload(List<OptionDto> optionDtos) {
        return optionDtos.stream().map(optionDto -> {
            OptionPayload.GroupPayload groupPayload = new OptionPayload.GroupPayload(
                    optionDto.group().id(),
                    optionDto.group().name()
            );

            List<OptionPayload.ValuePayload> valuePayloads = optionDto.values().stream()
                    .map(value -> new OptionPayload.ValuePayload(value.id(), value.name()))
                    .toList();

            return new OptionPayload(groupPayload, valuePayloads);
        }).toList();
    }
}
