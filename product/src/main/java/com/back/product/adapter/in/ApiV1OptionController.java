package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.request.OptionCreateRequestDto;
import com.back.product.dto.response.OptionResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<OptionResponseDto> createOptions(@Valid @RequestBody OptionCreateRequestDto request) {
        OptionResponseDto response = productFacade.createOptions(request);
        return CommonResponse.success(SuccessCode.CREATED, response);
    }

    @Override
    public CommonResponse<?> appendOptions(OptionCreateRequestDto request) {
        return null;
    }
}
