package com.back.product.app.usecase.command;

import com.back.common.annotation.Loggable;
import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.product.adapter.out.persistence.ProductImageRepository;
import com.back.product.adapter.out.persistence.ProductOptionValuesRepository;
import com.back.product.adapter.out.persistence.ProductRepository;
import com.back.product.app.usecase.query.ProductSupport;
import com.back.product.domain.*;
import com.back.product.dto.command.ProductVariantCreateCommand;
import com.back.product.dto.command.ProductVariantUpdateCommand;
import com.back.product.dto.model.ProductDetailDto;
import com.back.product.dto.model.ProductDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.mapper.*;
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
    private final ProductInfoMapper productInfoMapper;
    private final ProductOptionValuesMapper productOptionValuesMapper;
    private final ProductRepository productRepository;
    private final ProductOptionValuesRepository productOptionValuesRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSupport productSupport;

    @Loggable
    @Transactional
    public List<ProductDto> createMultipleProduct(ProductInfo productInfo, List<ProductVariantCreateCommand> variants) {
        List<Long> allOptionValueIds = variants.stream()
                .flatMap(variant -> variant.optionValueIds().stream())
                .distinct()
                .toList();
        Map<Long, OptionValue> optionValueMap = findOptionValuesAsMap(allOptionValueIds);

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

        return buildProductDto(newProducts, newProductOptionValues, newProductImages);
    }

    @Loggable
    @Transactional
    public List<ProductDto> updateMultipleProduct(ProductInfo productInfo, List<ProductVariantUpdateCommand> variants) {
        List<Product> existProducts = productSupport.getAllProductsByProductInfo(productInfo);

        List<Long> allOptionValueIds = variants.stream()
                .flatMap(variant -> variant.optionValueIds().stream())
                .distinct()
                .toList();
        Map<Long, OptionValue> optionValueMap = findOptionValuesAsMap(allOptionValueIds);

        Map<Boolean, List<ProductVariantUpdateCommand>> categorizedVariants = variants.stream()
                .collect(Collectors.partitioningBy(variant -> variant.productId() != null));
        List<ProductVariantUpdateCommand> variantsToUpdate = categorizedVariants.get(true);
        List<ProductVariantUpdateCommand> variantsToCreate = categorizedVariants.get(false);

        handleDeletes(existProducts, variants);
        handleUpdates(productInfo, variantsToUpdate, optionValueMap, existProducts);
        handleCreations(productInfo, variantsToCreate, optionValueMap);

        return findAllProduct(productInfo);
    }

    @Loggable
    @Transactional
    public void deleteMultipleProduct(Long productInfoId) {
        List<Product> deletedProducts = productSupport.getAllProductsByProductInfoId(productInfoId);
        deleteProducts(deletedProducts);
    }

    @Loggable
    @Transactional(readOnly = true)
    public List<ProductDto> findAllProduct(ProductInfo productInfo) {
        List<Product> products = productSupport.getAllProductsByProductInfo(productInfo);

        if (products.isEmpty()) {
            throw new CustomException(FailureCode.ENTITY_NOT_FOUND);
        }

        List<ProductOptionValues> productOptionValues = productSupport.getAllProductOptionValuesByProductsIn(products);
        List<ProductImage> productImages = productSupport.getAllProductImagesByProductsIn(products);

        return buildProductDto(products, productOptionValues, productImages);
    }

    @Loggable
    private void handleCreations(ProductInfo productInfo, List<ProductVariantUpdateCommand> variantsToCreate, Map<Long, OptionValue> optionValueMap) {
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

    @Loggable
    private void handleUpdates(ProductInfo productInfo, List<ProductVariantUpdateCommand> variantsToUpdate, Map<Long, OptionValue> optionValueMap, List<Product> existProducts) {
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

    @Loggable
    private void handleDeletes(List<Product> existProducts, List<ProductVariantUpdateCommand> variants) {
        Set<Long> requestIds = variants.stream()
                .map(ProductVariantUpdateCommand::productId)
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
    
    private Map<Long, OptionValue> findOptionValuesAsMap(List<Long> optionValueIds) {
        List<OptionValue> optionValues = productSupport.getAllOptionValues(optionValueIds);
        
        if (optionValues.size() != optionValueIds.size()) {
            throw new CustomException(FailureCode.ENTITY_NOT_FOUND);
        }
        
        return optionValues.stream().collect(Collectors.toMap(
                OptionValue::getId, 
                value -> value
        ));
    }

    private List<ProductDto> buildProductDto(
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

    @Loggable
    @Transactional
    public Boolean isOptionGroupInUse(Long optionGroupId) {
        return productSupport.existsProductByOptionGroupId(optionGroupId);
    }

    @Loggable
    @Transactional
    public Boolean isOptionValueInUse(Long optionValueId) {
        return productSupport.existsProductByOptionValueId(optionValueId);
    }

    @Loggable
    @Transactional(readOnly = true)
    public List<ProductDetailDto> findMultipleProduct(List<Long> productIds) {
        List<Product> products = productSupport.findMultipleProductByIds(productIds);

        if (!isSameSize(products, productIds)) {
            throw new CustomException(FailureCode.ENTITY_NOT_FOUND);
        }

        List<ProductImage> productImages = productSupport.getAllProductImagesByProductsIn(products);
        List<ProductOptionValues> productOptions = productSupport.getAllProductOptionValuesByProductsIn(products);

        return buildProductResponseDto(products, productOptions, productImages);
    }

    private List<ProductDetailDto> buildProductResponseDto(List<Product> products, List<ProductOptionValues> options, List<ProductImage> images) {
        Map<Product, List<ProductImage>> imagesByProduct = images.stream()
                .collect(Collectors.groupingBy(ProductImage::getProduct));

        Map<Product, List<ProductOptionValues>> optionsByProduct = options.stream()
                .collect(Collectors.groupingBy(ProductOptionValues::getProduct));;

        Map<ProductInfo, List<Product>> productsByInfo = products.stream()
                .collect(Collectors.groupingBy(Product::getProductInfo));

        return productsByInfo.entrySet().stream().map(entry -> {
            ProductInfoDto productInfoDto = productInfoMapper.toDto(entry.getKey());

            List<ProductDto> productDtos = entry.getValue().stream().map(product -> productMapper.toDto(
                    product,
                    optionsByProduct.getOrDefault(product, Collections.emptyList()),
                    imagesByProduct.getOrDefault(product, Collections.emptyList())
            )).toList();

            return productMapper.toDetailDto(productInfoDto, productDtos);
        }).toList();
    }

    private Boolean isSameSize(List<?> list1, List<?> list2) {
        return list1.size() == list2.size();
    }
}
