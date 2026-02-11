package com.back.product.adapter.in.web.controller;

import com.back.common.annotation.Loggable;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.ProductCommandApiController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import com.back.product.dto.response.ProductDetailResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1ProductCommandController implements ProductCommandApiController {
    private final ProductFacade productFacade;

    @Override
    @Loggable
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<ProductDetailResponseDto> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
        ProductDetailResponseDto response = productFacade.createProduct(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @Loggable
    @PutMapping("/{productInfoId}")
    public CommonResponse<ProductDetailResponseDto> updateProduct(
            @PathVariable Long productInfoId,
            @Valid @RequestBody ProductUpdateRequestDto request
    ) {
        ProductDetailResponseDto response = productFacade.updateProduct(productInfoId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @DeleteMapping("/{productInfoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteProduct(@PathVariable Long productInfoId) {
        productFacade.deleteProduct(productInfoId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
