package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.response.CategoryListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1CategoryController implements CategoryApiController {
    private final ProductFacade productFacade;

    @Override
    @GetMapping
    public CommonResponse<CategoryListResponseDto> getCategories() {
        CategoryListResponseDto response = CategoryListResponseDto.builder()
                .categories(productFacade.getCategories())
                .build();
        return CommonResponse.success(SuccessCode.OK, response);
    }
}
