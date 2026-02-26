package com.back.market.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.market.dto.request.MarketProductDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

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

    @Id
    @Column(name = "id")
    private Long id;        // 원본 상품의 Option Value ID (비즈니스 키)

    @Column(name = "product_option", nullable = false, length = 100)
    private String productOption;        // 상품 사이즈(옵션)

    @Column(name = "product_info_id", nullable = false)
    private Long productInfoId;         // 원본 상품의 Info ID (삭제 시 활용, 상품이 삭제되면 옵션들까지 전부 삭제해야하므로, 삭제 시 기준이 됨)

    @Column(name = "name", nullable = false)
    private String name;                // 상품명

    @Column(name = "product_number", nullable = false)
    private String productNumber;       // 제품번호

    @Column(name = "image")
    private String thumbnailImage;      // 제품 이미지

    @Column(name = "release_price")
    private BigDecimal releasePrice;    // 발매가

    @Column(name = "brand_name", nullable = false)
    private String brandName;           // 브랜드명

    @Column(name = "category_name", nullable = false)
    private String categoryName;        // 카테고리명

    public void updateInfo(MarketProductDto dto) {
        this.name = dto.name();
        this.productNumber = dto.productNumber();
        this.thumbnailImage = dto.thumbnailImage();
        this.releasePrice = dto.releasePrice();
        this.brandName = dto.brandName();
        this.categoryName = dto.categoryName();
    }
}
