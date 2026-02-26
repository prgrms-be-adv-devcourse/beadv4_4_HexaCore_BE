package com.back.notification.adapter.out.feign.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryDto {
    private Long categoryId;
    private String name;
    private String imageUrl;
}
