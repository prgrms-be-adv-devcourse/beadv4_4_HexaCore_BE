package com.back.market.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.market.domain.enums.BiddingPosition;
import com.back.market.domain.enums.BiddingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 입찰 테이블
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE biddings SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "biddings")
public class Bidding extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // PK

    @ManyToOne(fetch = FetchType.LAZY) // LAZY LOADING
    @JoinColumn(name = "user_id", nullable = false)
    private MarketUser marketUser;         // 누가 입찰했는지 (MarketUsers 참조)

    @ManyToOne(fetch = FetchType.LAZY) // LAZY LOADING
    @JoinColumn(name = "product_id", nullable = false)
    private MarketProduct marketProduct;    // 어떤 상품(사이즈 포함)에 입찰했는지 (MarketProduct 참조)

    @Column(name = "price", nullable = false)
    private Long price;                     // 입찰 희망 가격

    @Column(name = "position", nullable = false)
    @Enumerated(EnumType.STRING)
    private BiddingPosition position;       // 구매입찰, 판매입찰 구분

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BiddingStatus status;           // 입찰 상태

}
