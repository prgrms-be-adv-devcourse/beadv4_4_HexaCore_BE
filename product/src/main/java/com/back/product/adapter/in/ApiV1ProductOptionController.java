package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.BrandCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/products/options")
@RequiredArgsConstructor
public class ApiV1ProductOptionController implements ProductOptionApiController {
    private final ProductFacade productFacade;

    @Override
    public CommonResponse<?> getOptions() {
        return null;
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
