package com.back.product.adapter.in;

import com.back.common.code.SuccessCode;
import com.back.common.response.CommonResponse;
import com.back.product.app.ProductFacade;
import com.back.product.dto.response.ProductResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     *    1. Main Product Search API: GET /api/v1/products/search
     *        * Purpose: Flexible search with full-text (query), filters (brandId, categoryId,
     *          minPrice, maxPrice, startDate, endDate, option), pagination (page, size), and sorting
     *          (sort).
     *        * Response: Paginated ProductDocument list.
     *
     *    2. Get Product Details by ID API: GET /api/v1/products/{productId}
     *        * Purpose: Retrieve single product details by Elasticsearch ID.
     *        * Parameters: productId (path variable).
     *        * Response: Single ProductDocument.
     *
     *    3. Get Products by Brand API (Optional): GET /api/v1/products/brands/{brandId}
     *        * Purpose: Retrieve products by brandId, with pagination and sorting.
     *        * Response: Paginated ProductDocument list.
     *
     *    4. Get Products by Category API (Optional): GET /api/v1/products/categories/{categoryId}
     *        * Purpose: Retrieve products by categoryId, with pagination and sorting.
     *        * Response: Paginated ProductDocument list.
     */
}
