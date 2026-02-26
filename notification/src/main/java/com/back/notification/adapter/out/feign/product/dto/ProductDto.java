package com.back.notification.adapter.out.feign.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProductDto {
    private Long productId;
    private Long inventory;
    private List<OptionDto> options;
    private List<String> imageUrls;
}
