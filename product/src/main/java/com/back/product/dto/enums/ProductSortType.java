package com.back.product.dto.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProductSortType {
    LATEST("releasedDate", Sort.Direction.DESC),     // 최신순 (기본값)
    PRICE_LOW("releasePrice", Sort.Direction.ASC),   // 낮은 가격순
    PRICE_HIGH("releasePrice", Sort.Direction.DESC), // 높은 가격순
    POPULAR("totalInventory", Sort.Direction.ASC);   // 인기순 (예시)

    private final String fieldName;
    private final Sort.Direction direction;
}
