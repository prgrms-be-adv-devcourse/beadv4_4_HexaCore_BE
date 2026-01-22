package com.back.product.document;

import com.back.product.global.document.BaseDocument;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(indexName = "products")
public class ProductDocument extends BaseDocument<String> {
    @Field(type = FieldType.Long)
    private Long productInfoId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String productName;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Long)
    private Long brandId;

    @Field(type =  FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Double)
    private BigDecimal releasePrice;

    @Field(type = FieldType.Long)
    private Long totalInventory;

    @Field(type = FieldType.Keyword)
    private List<String> totalOptions;

    @Field(type = FieldType.Date)
    private LocalDateTime releasedDate;
}
