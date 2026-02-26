package com.back.product.adapter.in.web.controller;

import com.back.common.annotation.Loggable;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.ProductQueryApiController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.request.PageRequestDto;
import com.back.product.dto.request.ProductQueryRequestDto;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductDetailListResponseDto;
import com.back.product.dto.response.ProductDetailResponseDto;
import com.back.product.dto.response.ProductSearchResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1ProductQueryController implements ProductQueryApiController {
    private final ProductFacade productFacade;

    @Override
    @Loggable
    @GetMapping("/{productInfoId}")
    public CommonResponse<ProductDetailResponseDto> getProductDetail(@PathVariable Long productInfoId) {
        ProductDetailResponseDto response = productFacade.getProductDetail(productInfoId);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @GetMapping
    public CommonResponse<ProductSearchResponseDto> searchProducts(@ModelAttribute @Valid ProductSearchRequestDto request) {
        ProductSearchResponseDto response = productFacade.findProductPage(request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @GetMapping("/{productInfoId}/similar")
    public CommonResponse<ProductSearchResponseDto> findSimilarProducts(
            @PathVariable Long productInfoId,
            @Valid @ModelAttribute PageRequestDto request
    ) {
        ProductSearchResponseDto response = productFacade.findSimilarProducts(productInfoId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @GetMapping("/variants")
    public CommonResponse<ProductDetailListResponseDto> getProducts(@Valid @ModelAttribute ProductQueryRequestDto request) {
        ProductDetailListResponseDto response = productFacade.getProducts(request);
        return CommonResponse.success(SuccessCode.OK, response);
    }
}
