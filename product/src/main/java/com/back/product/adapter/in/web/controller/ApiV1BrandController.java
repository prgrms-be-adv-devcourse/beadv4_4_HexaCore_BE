package com.back.product.adapter.in.web.controller;

import com.back.common.annotation.Loggable;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.BrandApiController;
import com.back.product.app.facade.BrandFacade;
import com.back.product.dto.request.BrandDataRequestDto;
import com.back.product.dto.request.BrandListCreateRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.dto.response.BrandPageResponseDto;
import com.back.product.dto.response.BrandResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products/brands", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1BrandController implements BrandApiController {
    private final BrandFacade brandFacade;

    @Override
    @Loggable
    @GetMapping
    public CommonResponse<BrandPageResponseDto> getBrands(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        BrandPageResponseDto response = brandFacade.getBrands(page, size);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<BrandListResponseDto> createBrands(@RequestBody @Valid BrandListCreateRequestDto request) {
        BrandListResponseDto response = brandFacade.createBrands(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @Loggable
    @PutMapping("/{brandId}")
    public CommonResponse<BrandResponseDto> modifyBrand(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandDataRequestDto request) {
        BrandResponseDto response = brandFacade.modifyBrand(brandId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @DeleteMapping("/{brandId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteBrand(@PathVariable Long brandId) {
        brandFacade.deleteBrand(brandId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
