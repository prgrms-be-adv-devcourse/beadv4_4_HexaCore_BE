package com.back.product.adapter.in.web.controller;

import com.back.common.annotation.Loggable;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.BrandApiController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.dto.response.BrandResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/products/brands", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1BrandController implements BrandApiController {
    private final ProductFacade productFacade;

    @Override
    @Loggable
    @GetMapping
    public CommonResponse<BrandListResponseDto> getBrands() {
        BrandListResponseDto response = BrandListResponseDto.builder()
                .brands(productFacade.getBrands())
                .build();
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<BrandListResponseDto> createBrands(@RequestBody @Valid BrandListCreateRequestDto request) {
        BrandListResponseDto response = productFacade.createBrands(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @Loggable
    @PutMapping("/{brandId}")
    public CommonResponse<BrandResponseDto> modifyBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandDataRequestDto request) {
        BrandResponseDto response = productFacade.modifyBrand(brandId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteBrand(@PathVariable Long brandId) {
        productFacade.deleteBrand(brandId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
