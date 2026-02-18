package com.back.product.mapper;

import com.back.product.dto.model.BrandDto;
import com.back.product.event.kafka.BrandCreatedPayload;
import com.back.product.event.kafka.BrandDeletedPayload;
import com.back.product.event.kafka.BrandPayload;
import com.back.product.event.kafka.BrandUpdatedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BrandPayloadMapper {
    private final BrandMapper brandMapper;

    public BrandCreatedPayload toCreatedPayload(List<BrandDto> brandDtos) {
        List<BrandPayload> brandPayloads = brandDtos.stream().map(brandMapper::toPayload).toList();

        return BrandCreatedPayload.builder()
                .brands(brandPayloads)
                .build();
    }

    public BrandUpdatedPayload toUpdatePayload(BrandDto brand) {
        BrandPayload brandPayload = brandMapper.toPayload(brand);

        return BrandUpdatedPayload.builder()
                .brand(brandPayload)
                .build();
    }

    public BrandDeletedPayload toDeletedPayload(Long brandId) {
        return BrandDeletedPayload.builder()
                .brandId(brandId)
                .build();
    }
}
