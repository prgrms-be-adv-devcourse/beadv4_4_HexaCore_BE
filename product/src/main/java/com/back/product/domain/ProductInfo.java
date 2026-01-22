package com.back.product.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE PRODUCT_INFO SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "product_info",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_info_brand_code", columnNames = {"brand_id", "code"})
    }
)
public class ProductInfo extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String productCode;

    @Column(nullable = false, precision = 10, scale = 0) // 10자리 정수
    @Min(0)
    private BigDecimal releasePrice;

    @Column(nullable = false)
    private LocalDateTime releasedDate;

    public void update(Brand brand, Category category, String name, String code, BigDecimal releasePrice, LocalDateTime releasedDate) {
        this.brand = brand;
        this.category = category;
        this.name = name;
        this.productCode = code;
        this.releasePrice = releasePrice;
        this.releasedDate = releasedDate;
    }
}
