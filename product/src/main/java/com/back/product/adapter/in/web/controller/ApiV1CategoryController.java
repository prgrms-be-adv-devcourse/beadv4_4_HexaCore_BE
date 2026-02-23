package com.back.product.adapter.in.web.controller;

import com.back.common.annotation.Loggable;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.CategoryApiController;
import com.back.product.app.facade.CategoryFacade;
import com.back.product.dto.request.CategoryDataRequestDto;
import com.back.product.dto.request.CategoryListCreateRequestDto;
import com.back.product.dto.response.CategoryListResponseDto;
import com.back.product.dto.response.CategoryPageResponseDto;
import com.back.product.dto.response.CategoryResponseDto;
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
@RequestMapping(path = "/api/v1/products/categories", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1CategoryController implements CategoryApiController {
    private final CategoryFacade categoryFacade;

    @Override
    @Loggable
    @GetMapping
    public CommonResponse<CategoryPageResponseDto> getCategories(
            @RequestParam(defaultValue = "0") Long page,
            @RequestParam(defaultValue = "10") Long size
    ) {
        CategoryPageResponseDto response = categoryFacade.getCategories(page, size);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<CategoryListResponseDto> createCategories(@RequestBody @Valid CategoryListCreateRequestDto request) {
        CategoryListResponseDto response = categoryFacade.createCategories(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @Loggable
    @PutMapping("/{categoryId}")
    public CommonResponse<CategoryResponseDto> modifyCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryDataRequestDto request) {
        CategoryResponseDto response = categoryFacade.modifyCategory(categoryId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteCategory(@PathVariable Long categoryId) {
        categoryFacade.deleteCategory(categoryId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
