package com.back.product.mapper;

import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.event.kafka.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPayloadMapper {
    private final ProductInfoMapper productInfoMapper;
    private final OptionMapper optionMapper;

    public ProductCreatedPayload toCreatedPayload(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        ProductInfoPayload productInfoPayload = productInfoMapper.toPayload(productInfoDto);
        List<OptionPayload> optionPayloads = optionDtos.stream().map(optionMapper::toPayload).toList();

        return ProductCreatedPayload.builder()
                .productInfo(productInfoPayload)
                .options(optionPayloads)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public ProductUpdatedPayload toUpdatedPayload(ProductInfoDto productInfoDto, List<OptionDto> optionDtos, String thumbnailUrl) {
        ProductInfoPayload productInfoPayload = productInfoMapper.toPayload(productInfoDto);
        List<OptionPayload> optionPayloads = optionDtos.stream().map(optionMapper::toPayload).toList();

        return ProductUpdatedPayload.builder()
                .productInfo(productInfoPayload)
                .options(optionPayloads)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public ProductDeletedPayload toDeletedPayload(Long productInfoId) {
        return ProductDeletedPayload.builder()
                .productInfoId(productInfoId)
                .build();
    }
}
