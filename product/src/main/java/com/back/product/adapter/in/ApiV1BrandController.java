package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
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
    @GetMapping
    public CommonResponse<BrandListResponseDto> getBrands() {
        BrandListResponseDto response = BrandListResponseDto.builder()
                .brands(productFacade.getBrands())
                .build();
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<BrandListResponseDto> createBrands(@RequestBody @Valid BrandCreateRequestDto request) {
        BrandListResponseDto response = productFacade.createBrands(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }
}
