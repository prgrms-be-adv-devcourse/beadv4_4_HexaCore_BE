package com.back.product.mapper;

import com.back.product.dto.command.BrandDataCommand;
import com.back.product.dto.request.BrandDataRequestDto;
import org.springframework.stereotype.Component;

@Component
public class BrandDataCommandMapper {
    public BrandDataCommand toCommand(BrandDataRequestDto request) {
        return BrandDataCommand.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .build();
    }
}
