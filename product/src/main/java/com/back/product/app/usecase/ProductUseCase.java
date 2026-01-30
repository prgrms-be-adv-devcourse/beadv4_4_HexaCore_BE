package com.back.product.app.usecase;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.ProductImageRepository;
import com.back.product.adapter.out.ProductOptionValuesRepository;
import com.back.product.adapter.out.ProductRepository;
import com.back.product.domain.*;
import com.back.product.dto.ProductDto;
import com.back.product.dto.request.ProductVariantCreateRequestDto;
import com.back.product.dto.request.ProductVariantUpdateRequestDto;
import com.back.product.mapper.ProductImageMapper;
import com.back.product.mapper.ProductMapper;
import com.back.product.mapper.ProductOptionValuesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductUseCase {
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductOptionValuesMapper productOptionValuesMapper;
    private final ProductRepository productRepository;
    private final ProductOptionValuesRepository productOptionValuesRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSupport productSupport;

    @Transactional
    public List<ProductDto> createMultipleProduct(
            ProductInfo productInfo,
            List<ProductVariantCreateRequestDto> variants,
            Map<Long, OptionValue> optionValueMap
    ) {
        List<Product> createdProducts = new ArrayList<>();
        List<ProductOptionValues> createdProductOptionValues = new ArrayList<>();
        List<ProductImage> createdImages = new ArrayList<>();

        variants.forEach(variant -> {
            Product newProduct = createProduct(productInfo, variant.inventory());
            createdProducts.add(newProduct);

            List<ProductOptionValues> newProductOptionValues = createProductOptionValues(newProduct, variant.optionValueIds(), optionValueMap);
            createdProductOptionValues.addAll(newProductOptionValues);

            List<ProductImage> newProductImages = createProductImages(newProduct, variant.imageUrls());
            createdImages.addAll(newProductImages);
        });

        List<Product> newProducts = productRepository.saveAll(createdProducts);
        List<ProductOptionValues> newProductOptionValues = productOptionValuesRepository.saveAll(createdProductOptionValues);
        List<ProductImage> newProductImages = productImageRepository.saveAll(createdImages);

        return convertToDto(newProducts, newProductOptionValues, newProductImages);
    }

    @Transactional
    public List<ProductDto> updateMultipleProduct(ProductInfo productInfo, List<ProductVariantUpdateRequestDto> variants, Map<Long, OptionValue> optionValueMap) {
        List<Product> existProducts = productSupport.getAllProductsByProductInfo(productInfo);

        Map<Boolean, List<ProductVariantUpdateRequestDto>> categorizedVariants = variants.stream()
                .collect(Collectors.partitioningBy(variant -> variant.productId() != null));
        List<ProductVariantUpdateRequestDto> variantsToUpdate = categorizedVariants.get(true);
        List<ProductVariantUpdateRequestDto> variantsToCreate = categorizedVariants.get(false);

        handleDeletes(existProducts, variants);
        handleUpdates(productInfo, variantsToUpdate, optionValueMap, existProducts);
        handleCreations(productInfo, variantsToCreate, optionValueMap);

        return findAllProduct(productInfo);
    }

    @Transactional
    public void deleteMultipleProduct(Long productInfoId) {
        List<Product> deletedProducts = productSupport.getAllProductsByProductInfoId(productInfoId);
        deleteProducts(deletedProducts);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> findAllProduct(ProductInfo productInfo) {
        List<Product> products = productSupport.getAllProductsByProductInfo(productInfo);

        if (products.isEmpty()) {
            throw new CustomException(FailureCode.ENTITY_NOT_FOUND);
        }

        List<ProductOptionValues> productOptionValues = productSupport.getAllProductOptionValuesByProductsIn(products);
        List<ProductImage> productImages = productSupport.getAllProductImagesByProductsIn(products);

        return convertToDto(products, productOptionValues, productImages);
    }

    private void handleCreations(ProductInfo productInfo, List<ProductVariantUpdateRequestDto> variantsToCreate, Map<Long, OptionValue> optionValueMap) {
        List<Product> createdProducts = new ArrayList<>();
        List<ProductOptionValues> createdProductOptionValues = new ArrayList<>();
        List<ProductImage> createdImages = new ArrayList<>();

        variantsToCreate.forEach(variant -> {
            if (variant.productId() != null) {
                throw new CustomException(FailureCode.INVALID_INPUT_VALUE);
            }

            Product newProduct = createProduct(productInfo, variant.inventory());
            createdProducts.add(newProduct);

            List<ProductOptionValues> newProductOptionValues = createProductOptionValues(newProduct, variant.optionValueIds(), optionValueMap);
            createdProductOptionValues.addAll(newProductOptionValues);

            List<ProductImage> newProductImages = createProductImages(newProduct, variant.imageUrls());
            createdImages.addAll(newProductImages);
        });

        productRepository.saveAll(createdProducts);
        productOptionValuesRepository.saveAll(createdProductOptionValues);
        productImageRepository.saveAll(createdImages);
    }

    private void handleUpdates(ProductInfo productInfo, List<ProductVariantUpdateRequestDto> variantsToUpdate, Map<Long, OptionValue> optionValueMap, List<Product> existProducts) {
        Map<Long, Product> existProductsMap = existProducts.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Product> updatedProducts = new ArrayList<>();
        List<ProductOptionValues> updatedProductOptionValues = new ArrayList<>();
        List<ProductImage> updatedProductImages = new ArrayList<>();

        variantsToUpdate.forEach(variant -> {
            if (variant.productId() == null) {
                throw new CustomException(FailureCode.INVALID_INPUT_VALUE);
            }

            Product updatedProduct = existProductsMap.get(variant.productId());
            updatedProduct.update(productInfo, variant.inventory());
            updatedProducts.add(updatedProduct);

            List<ProductOptionValues> newProductOptionValues = createProductOptionValues(updatedProduct, variant.optionValueIds(), optionValueMap);
            updatedProductOptionValues.addAll(newProductOptionValues);

            List<ProductImage> newProductImages = createProductImages(updatedProduct, variant.imageUrls());
            updatedProductImages.addAll(newProductImages);
        });

        deletedProductVariants(updatedProducts);
        productOptionValuesRepository.saveAll(updatedProductOptionValues);
        productImageRepository.saveAll(updatedProductImages);
    }

    private void handleDeletes(List<Product> existProducts, List<ProductVariantUpdateRequestDto> variants) {
        Set<Long> requestIds = variants.stream()
                .map(ProductVariantUpdateRequestDto::productId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Product> deletedProducts = existProducts.stream()
                .filter(p -> !requestIds.contains(p.getId()))
                .toList();

        if (!deletedProducts.isEmpty()) {
            deleteProducts(deletedProducts);
        }
    }

    private void deleteProducts(List<Product> deletedProducts) {
        if (!deletedProducts.isEmpty()) {
            deletedProductVariants(deletedProducts);
            productRepository.deleteAll(deletedProducts);
        }
    }

    private void deletedProductVariants(List<Product> deletedProducts) {
        if (!deletedProducts.isEmpty()) {
            productOptionValuesRepository.deleteAllByProductIn(deletedProducts);
            productImageRepository.deleteAllByProductIn(deletedProducts);
        }
    }
    
    private Product createProduct(ProductInfo productInfo, Long inventory) {
        return productMapper.toEntity(productInfo, inventory);
    }

    private List<ProductOptionValues> createProductOptionValues(Product product, List<Long> optionValueIds, Map<Long, OptionValue> optionValueMap) {
        return optionValueIds.stream().map(optionValueId -> {
            OptionValue optionValue = optionValueMap.get(optionValueId);
            return productOptionValuesMapper.toEntity(product, optionValue);
        }).toList();
    }

    private List<ProductImage> createProductImages(Product product, List<String> imageUrls) {
        return imageUrls.stream().map(imageUrl ->
                productImageMapper.toEntity(product, imageUrl)
        ).toList();
    }

    private List<ProductDto> convertToDto(
            List<Product> products,
            List<ProductOptionValues> productOptionValues,
            List<ProductImage> productImages
    ) {
        Map<Product, List<ProductOptionValues>> productOptionValuesAsMap = productOptionValues
                .stream().collect(Collectors.groupingBy(ProductOptionValues::getProduct));
        Map<Product, List<ProductImage>> productImagesAsMap = productImages.stream()
                .collect(Collectors.groupingBy(ProductImage::getProduct));

        return products.stream().map(product ->
            productMapper.toDto(
                product,
                productOptionValuesAsMap.get(product),
                productImagesAsMap.get(product)
            )
        ).toList();
    }

    @Transactional
    public Boolean isOptionGroupInUse(Long optionGroupId) {
        return productSupport.existsProductByOptionGroupId(optionGroupId);
    }

    @Transactional
    public Boolean isOptionValueInUse(Long optionValueId) {
        return productSupport.existsProductByOptionValueId(optionValueId);
    }
}
