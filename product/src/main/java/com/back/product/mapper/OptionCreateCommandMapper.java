package com.back.product.mapper;

import com.back.product.dto.command.OptionCreateCommand;
import com.back.product.dto.request.OptionCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class OptionCreateCommandMapper {
    public OptionCreateCommand toCommand(OptionCreateRequestDto request) {
        return OptionCreateCommand.builder()
                .group(request.group())
                .values(request.values())
                .build();
    }
}
