package com.back.product.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.product.dto.command.ProductInfoDataCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
        @UniqueConstraint(name = "uk_product_info_brand_code", columnNames = {"brand_id", "product_code"})
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

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(nullable = false, precision = 10, scale = 0) // 10자리 정수
    @Min(0)
    private BigDecimal releasePrice;

    @Column(nullable = false)
    private LocalDateTime releasedDate;

    public void update(ProductInfoDataCommand updated) {
        this.brand = updated.brand();
        this.category = updated.category();
        this.name = updated.name();
        this.productCode = updated.code();
        this.releasePrice = updated.releasePrice();
        this.releasedDate = updated.releasedDate();
    }
}
