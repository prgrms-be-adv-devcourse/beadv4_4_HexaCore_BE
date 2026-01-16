package com.back.product.adapter.in;

import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.response.BrandResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/products", produces = "application/json")
@RequiredArgsConstructor
public class ApiV1ProductController implements ProductApi {
    private final ProductFacade ProductFacade;

    @Override
    @GetMapping("/brands")
    public CommonResponse<?> getBrands() {
        List<BrandResponseDto> response = ProductFacade.getBrands();
        return CommonResponse.successWithData(HttpStatus.OK, response);
    }
}
