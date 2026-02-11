package com.back.product.app.usecase.command;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.ProductInfoRepository;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.Brand;
import com.back.product.domain.ProductInfo;
import com.back.product.dto.command.ProductInfoDataCommand;
import com.back.product.mapper.ProductInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductInfoUseCase {
    private final ProductInfoMapper productInfoMapper;
    private final ProductInfoRepository productInfoRepository;
    private final ProductSupport productSupport;

    @Loggable
    @Transactional
    public ProductInfo createProductInfo(ProductInfoDataCommand productInfo) {
        isDuplicateProductInfo(productInfo.brand(), productInfo.code());

        ProductInfo newProductInfo = productInfoMapper.toEntity(productInfo);

        return productInfoRepository.save(newProductInfo);
    }

    private void isDuplicateProductInfo(Brand brand, String code) {
        if (productSupport.existsProductInfoByBrandAndCode(brand, code)) {
            throw new CustomException(FailureCode.DUPLICATE_PRODUCT_INFO);
        }
    }

    @Loggable
    @Transactional
    public ProductInfo updateProductInfo(Long productInfoId, ProductInfoDataCommand productInfo) {
        ProductInfo productInfoToUpdate = productSupport.findProductInfoById(productInfoId)
                .orElseThrow(() -> new CustomException(FailureCode.PRODUCT_NOT_FOUND));

        productInfoToUpdate.update(productInfo);

        return productInfoToUpdate;
    }

    @Loggable
    @Transactional
    public void deleteProductInfo(Long productInfoId) {
        ProductInfo productInfo = getProductInfoExists(productInfoId);

        productInfoRepository.delete(productInfo);
    }

    @Loggable
    @Transactional(readOnly = true)
    public ProductInfo findProductInfo(Long productInfoId) {
        return getProductInfoExists(productInfoId);
    }

    private ProductInfo getProductInfoExists(Long productInfoId) {
        return productSupport.findProductInfoById(productInfoId)
                .orElseThrow(() -> new CustomException(FailureCode.PRODUCT_INFO_NOT_FOUND));
    }

    @Loggable
    @Transactional
    public Boolean isBrandInUse(Long brandId) {
        return productSupport.existsProductInfoByBrand(brandId);
    }

    @Loggable
    @Transactional
    public Boolean isCategoryInUse(Long categoryId) {
        return productSupport.existsProductInfoByCategory(categoryId);
    }
}
