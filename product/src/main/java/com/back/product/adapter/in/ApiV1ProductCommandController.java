package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.ProductCreateRequestDto;
import com.back.product.dto.request.ProductUpdateRequestDto;
import com.back.product.dto.response.ProductResponseDto;
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<ProductResponseDto> createProduct(@Valid @RequestBody ProductCreateRequestDto request) {
        ProductResponseDto response = productFacade.createProduct(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @PutMapping("/{productInfoId}")
    public CommonResponse<ProductResponseDto> updateProduct(
            @PathVariable Long productInfoId,
            @Valid @RequestBody ProductUpdateRequestDto request
    ) {
        ProductResponseDto response = productFacade.updateProduct(productInfoId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @DeleteMapping("/{productInfoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteProduct(@PathVariable Long productInfoId) {
        productFacade.deleteProduct(productInfoId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
