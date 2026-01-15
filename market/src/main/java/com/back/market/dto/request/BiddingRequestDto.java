package com.back.market.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Controller에서 Facade로, Facade에서 UseCase로 데이터를 넘길 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BiddingRequestDto {
    private Long productId;
    private BigDecimal price;
    private String size;
}
