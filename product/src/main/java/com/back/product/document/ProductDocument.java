package com.back.product.document;

import com.back.product.global.document.BaseDocument;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

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

    // 👇 [수정 핵심] MultiField 적용 (productName)
    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = {
            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "my_ngram_analyzer")
        }
    )
    private String productName;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Long)
    private Long brandId;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = {
            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer"),
            @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "my_ngram_analyzer")
        }
    )
    private String brandName;

    @Field(type = FieldType.Long)
    private Long categoryId;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = {
            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer")
        }
    )
    private String categoryName;

    @Field(type = FieldType.Double)
    private BigDecimal releasePrice;

    @Field(type = FieldType.Long)
    private Long totalInventory;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = {
            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer")
        }
    )
    private List<String> totalOptions;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime releasedDate;
}