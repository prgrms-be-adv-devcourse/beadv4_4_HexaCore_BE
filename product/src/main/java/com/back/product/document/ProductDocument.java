package com.back.product.document;

import com.back.product.global.document.BaseDocument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch-settings.json")
public class ProductDocument extends BaseDocument<String> {
    @Field(type = FieldType.Object)
    private ProductInfo productInfo;

    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    @Field(type = FieldType.Nested)
    private List<Option> totalOptions;

    @Field(type = FieldType.Dense_Vector, dims = 384)
    private float[] embedding;

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ProductInfo {

        @Field(type = FieldType.Long)
        private Long productInfoId;

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "standard"),
                otherFields = {
                        @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer"),
                        @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "my_ngram_analyzer")
                }
        )
        private String productName;

        @Field(type = FieldType.Keyword)
        private String productCode;

        @Field(type = FieldType.Object)
        private Brand brand;

        @Field(type = FieldType.Object)
        private Category category;

        @Field(type = FieldType.Double)
        private BigDecimal releasePrice;

        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
        private LocalDateTime releasedDate;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Brand {

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
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Category {

        @Field(type = FieldType.Long)
        private Long categoryId;

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "standard"),
                otherFields = {
                        @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer")
                }
        )
        private String categoryName;
    }

    @Getter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Option {
        @Field(type = FieldType.Object)
        private OptionGroup group;

        @Field(type = FieldType.Object)
        private OptionValue value;

        @Getter
        @Builder
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static class OptionGroup {
            @Field(type = FieldType.Long)
            private Long groupId;

            @MultiField(
                    mainField = @Field(type = FieldType.Text, analyzer = "standard"),
                    otherFields = {
                            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer"),
                            @InnerField(suffix = "keyword", type = FieldType.Keyword)
                    }
            )
            private String groupName;
        }

        @Getter
        @Builder
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static class OptionValue {
            @Field(type = FieldType.Long)
            private Long valueId;

            @MultiField(
                    mainField = @Field(type = FieldType.Text, analyzer = "standard"),
                    otherFields = {
                            @InnerField(suffix = "nori", type = FieldType.Text, analyzer = "my_nori_analyzer"),
                            @InnerField(suffix = "keyword", type = FieldType.Keyword)
                    }
            )
            private String valueName;
        }
    }
}