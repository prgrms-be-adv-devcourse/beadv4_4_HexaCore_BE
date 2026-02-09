package com.back.image.dto.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageCategory {
    PROFILE("user/profile"),
    PRODUCT("product/main"),
    BRAND("product/brand"),
    CATEGORY("product/category");

    private final String path;
}
