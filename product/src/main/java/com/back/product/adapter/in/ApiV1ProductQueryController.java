package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.ProductSearchRequestDto;
import com.back.product.dto.response.ProductResponseDto;
import com.back.product.dto.response.ProductSearchListResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1ProductQueryController implements ProductQueryApiController {
    private final ProductFacade productFacade;

    @Override
    @GetMapping("/{productInfoId}")
    public CommonResponse<ProductResponseDto> getProductDetail(@PathVariable Long productInfoId) {
        ProductResponseDto response = productFacade.getProductDetail(productInfoId);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @GetMapping(params = "keyword")
    public CommonResponse<ProductSearchListResponseDto> searchProducts(
       @ModelAttribute @Valid ProductSearchRequestDto request,
       @RequestParam(defaultValue = "0") Long page,
       @RequestParam(defaultValue = "10") Long size
     ) {
        ProductSearchListResponseDto response = productFacade.findProductPage(request, page, size);
        return CommonResponse.success(SuccessCode.OK, response);
    }
}
