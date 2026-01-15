package com.back.market.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 입찰의 대상이 되는 테이블
 *
 * Brand, Category, Product_info, Product 총 4개의 테이블에서 필요한 컬럼만 선별, Market 모듈에서 사용할 Product를 만든다. Join 없이 조회 가능하게끔
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE market_product SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "market_product")
public class MarketProduct extends BaseTimeEntity {
    // Product 테이블
    @Id
    @Column(name = "id")
    private Long id;                    // 원본 Product의 PK를 그대로 사용

    @Column(name = "size", nullable = false, length = 50)
    private String size;                // 상품 사이즈(옵션)

    // Product_info 테이블
    @Column(name = "name", nullable = false)
    private String name;                // 상품명

    @Column(name = "product_number", nullable = false)
    private String productNumber;       // 제품번호

    @Column(name = "image")
    private String thumbnailImage;      // 제품 이미지

    @Column(name = "release_price")
    private Long originalPrice;         // 발매가

    // Brand 테이블
    @Column(name = "brand_name", nullable = false)
    private String brandName;           // 브랜드명

    //Category 테이블
    @Column(name = "category_name", nullable = false)
    private String categoryName;        // 카테고리명
}
