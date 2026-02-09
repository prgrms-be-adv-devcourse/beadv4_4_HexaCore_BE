package com.back.product.adapter.in.web.controller;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.CategoryApiController;
import com.back.product.app.facade.ProductFacade;
import com.back.product.dto.request.CategoryCreateRequestDto;
import com.back.product.dto.request.CategoryModifyRequestDto;
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
    public CommonResponse<CategoryListResponseDto> createCategories(@RequestBody @Valid CategoryCreateRequestDto request) {
        CategoryListResponseDto response = productFacade.createCategories(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @PutMapping("/{categoryId}")
    public CommonResponse<CategoryResponseDto> modifyCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryModifyRequestDto request) {
        CategoryResponseDto response = productFacade.modifyCategory(categoryId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteCategory(@PathVariable Long categoryId) {
        productFacade.deleteCategory(categoryId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
