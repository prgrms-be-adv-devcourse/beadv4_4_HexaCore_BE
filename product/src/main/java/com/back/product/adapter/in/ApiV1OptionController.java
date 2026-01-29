package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.BrandCreateRequestDto;
import com.back.product.dto.response.OptionResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products/options", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ApiV1OptionController implements OptionApiController {
    private final ProductFacade productFacade;

    @Override
    @GetMapping
    public CommonResponse<OptionResponseDto> getOptions() {
        OptionResponseDto response = productFacade.getOptions();
        return CommonResponse.success(SuccessCode.OK, response);
    }

    @Override
    public CommonResponse<?> createOptionGroups(BrandCreateRequestDto request) {
        return null;
    }

    @Override
    public CommonResponse<?> createOptionValues(BrandCreateRequestDto request) {
        return null;
    }
}
