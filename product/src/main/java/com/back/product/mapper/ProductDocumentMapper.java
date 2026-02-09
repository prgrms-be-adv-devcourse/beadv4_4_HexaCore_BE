package com.back.product.mapper;

import com.back.product.document.ProductDocument;
import com.back.product.dto.model.CategoryDto;
import com.back.product.dto.model.OptionDto;
import com.back.product.dto.model.ProductInfoDto;
import com.back.product.dto.model.ProductSearchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductDocumentMapper {
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;

    public ProductSearchDto toDto(ProductDocument document) {
        return ProductSearchDto.builder()
                .productInfoId(document.getProductInfo().getProductInfoId())
                .productName(document.getProductInfo().getProductName())
                .thumbnailUrl(document.getThumbnailUrl())
                .brandName(document.getProductInfo().getBrand().getBrandName())
                .categoryName(document.getProductInfo().getCategory().getCategoryName())
                .releasePrice(document.getProductInfo().getReleasePrice())
                .build();
    }
    
    public ProductDocument toDocument(ProductInfoDto productInfoDto, List<OptionDto>optionDtos, String thumbnailUrl) {
        ProductDocument.ProductInfo productInfoDocument = toProductInfoDocument(productInfoDto);

        List<ProductDocument.Option> optionDocuments = optionDtos.stream()
                .flatMap(optionDto -> toOptionDocumentList(optionDto).stream())
                .toList();

        return ProductDocument.builder()
                .productInfo(productInfoDocument)
                .totalOptions(optionDocuments)
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    private ProductDocument.ProductInfo toProductInfoDocument(ProductInfoDto productInfoDto) {
        ProductDocument.Brand brandDocument = brandMapper.toDocument(productInfoDto.brand());
        ProductDocument.Category categoryDocument = categoryMapper.toDocument(productInfoDto.category());

        return ProductDocument.ProductInfo.builder()
                .productInfoId(productInfoDto.productInfoId())
                .brand(brandDocument)
                .category(categoryDocument)
                .productName(productInfoDto.name())
                .productCode(productInfoDto.code())
                .releasePrice(productInfoDto.releasePrice())
                .releasedDate(productInfoDto.releaseDate())
                .build();
    }

    private ProductDocument.Category toCategoryDocument(CategoryDto categoryDto) {
        return ProductDocument.Category.builder()
                .categoryId(categoryDto.categoryId())
                .categoryName(categoryDto.name())
                .build();
    }

    private List<ProductDocument.Option> toOptionDocumentList(OptionDto optionDto) {
        return optionDto.values().stream()
                .map(valueDto -> toOptionDocument(optionDto.group(), valueDto))
                .toList();
    }

    private ProductDocument.Option toOptionDocument(OptionDto.GroupDto groupDto, OptionDto.ValueDto valueDto) {
        ProductDocument.Option.OptionGroup optionGroupDocument = toOptionGroupDocument(groupDto);
        ProductDocument.Option.OptionValue optionValueDocument = toOptionValueDocument(valueDto);

        return ProductDocument.Option.builder()
                .group(optionGroupDocument)
                .value(optionValueDocument)
                .build();
    }

    private ProductDocument.Option.OptionGroup toOptionGroupDocument(OptionDto.GroupDto groupDto) {
        return ProductDocument.Option.OptionGroup.builder()
                .groupId(groupDto.id())
                .groupName(groupDto.name())
                .build();
    }

    private ProductDocument.Option.OptionValue toOptionValueDocument(OptionDto.ValueDto valueDto) {
        return ProductDocument.Option.OptionValue.builder()
                .valueId(valueDto.id())
                .valueName(valueDto.name())
                .build();
    }
}
