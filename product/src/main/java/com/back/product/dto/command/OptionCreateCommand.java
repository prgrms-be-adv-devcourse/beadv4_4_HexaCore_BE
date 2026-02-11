package com.back.product.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record OptionCreateCommand(
        String group,
        List<String> values
) {
}
