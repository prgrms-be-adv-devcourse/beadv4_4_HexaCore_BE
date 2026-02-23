package com.back.product.adapter.in.web.controller;

import com.back.common.annotation.Loggable;
import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.adapter.in.web.api.OptionApiController;
import com.back.product.app.facade.OptionFacade;
import com.back.product.dto.request.OptionAppendRequestDto;
import com.back.product.dto.request.OptionGroupModifyRequestDto;
import com.back.product.dto.request.OptionListCreateRequestDto;
import com.back.product.dto.request.OptionValueModifyRequestDto;
import com.back.product.dto.response.OptionGroupModifyResponseDto;
import com.back.product.dto.response.OptionListResponseDto;
import com.back.product.dto.response.OptionResponseDto;
import com.back.product.dto.response.OptionValueModifyResponseDto;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products/options", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1OptionController implements OptionApiController {
    private final OptionFacade optionFacade;

    @Override
    @Loggable
    @GetMapping
    public CommonResponse<OptionListResponseDto> getOptions() {
        OptionListResponseDto response = optionFacade.getOptions();
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<OptionListResponseDto> createOptions(@Valid @RequestBody OptionListCreateRequestDto request) {
        OptionListResponseDto response = optionFacade.createOptions(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @Loggable
    @PostMapping(path = "/{optionGroupId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<OptionResponseDto> appendOptions(
            @PathVariable Long optionGroupId,
            @Valid @RequestBody OptionAppendRequestDto request) {
        OptionResponseDto response = optionFacade.appendOptions(optionGroupId, request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    @Loggable
    @PutMapping("/groups/{optionGroupId}")
    public CommonResponse<OptionGroupModifyResponseDto> modifyOptionGroup(
            @PathVariable Long optionGroupId,
            @Valid @RequestBody OptionGroupModifyRequestDto request) {
        OptionGroupModifyResponseDto response = optionFacade.modifyOptionGroup(optionGroupId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @PutMapping("/values/{optionValueId}")
    public CommonResponse<OptionValueModifyResponseDto> modifyOptionValue(
            @PathVariable Long optionValueId,
            @Valid @RequestBody OptionValueModifyRequestDto request) {
        OptionValueModifyResponseDto response = optionFacade.modifyOptionValue(optionValueId, request);
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    @Loggable
    @DeleteMapping("/groups/{optionGroupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteOptionGroup(@PathVariable Long optionGroupId) {
        optionFacade.deleteOptionGroup(optionGroupId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }

    @Override
    @Loggable
    @DeleteMapping("/values/{optionValueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public CommonResponse<?> deleteOptionValue(@PathVariable Long optionValueId) {
        optionFacade.deleteOptionValue(optionValueId);
        return CommonResponse.success(SuccessCode.NO_CONTENT, null);
    }
}
