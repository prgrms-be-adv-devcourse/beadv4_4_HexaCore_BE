package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.response.BrandListResponseDto;
import com.back.product.dto.response.BrandResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/products", produces = "application/json")
@RequiredArgsConstructor
public class ApiV1BrandController implements BrandApiController {
    private final ProductFacade ProductFacade;

    @Override
    @GetMapping("/brands")
    public CommonResponse<BrandListResponseDto> getBrands() {
        BrandListResponseDto response = BrandListResponseDto.builder()
                .brands(ProductFacade.getBrands())
                .build();
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<BrandResponseDto> createBrand(@RequestBody @Valid BrandCreateRequestDto request) {
        BrandResponseDto response = BrandResponseDto.builder()
                .brand(ProductFacade.createBrand(request))
                .build();
        return CommonResponse.success(SuccessCode.CREATED, response);
    }
}
