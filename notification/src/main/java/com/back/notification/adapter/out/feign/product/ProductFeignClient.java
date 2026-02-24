package com.back.notification.adapter.out.feign.product;

import com.back.common.response.CommonResponse;
import com.back.notification.adapter.out.feign.product.dto.ProductDetailListResponse;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange
public interface ProductFeignClient {

    @GetExchange("/api/v1/products/variants")
    CommonResponse<ProductDetailListResponse> getProducts(@RequestParam("productIds") List<Long> productIds);
}
