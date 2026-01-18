package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.CategoryCreateRequestDto;
import com.back.product.dto.response.CategoryListResponseDto;
import com.back.product.dto.response.CategoryResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<CategoryResponseDto> createCategory(@RequestBody @Valid CategoryCreateRequestDto request) {
        CategoryResponseDto response = CategoryResponseDto.builder()
                .category(productFacade.createCategory(request))
                .build();
        return CommonResponse.success(SuccessCode.CREATED, response);
    }
}
