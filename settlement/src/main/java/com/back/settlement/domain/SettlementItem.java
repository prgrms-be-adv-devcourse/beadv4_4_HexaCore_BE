package com.back.settlement.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "settlement_item")
public class SettlementItem extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    @NotNull
    private Settlement settlement;

    @NotNull
    @Column(name = "order_id")
    private Long orderId;

    @NotNull
    @Column(name = "product_id")
    private Long productId;

    @NotNull
    @Column(name = "buyer_id")
    private Long buyerId;

    @NotNull
    @Column(name = "seller_id")
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SettlementItemStatus status;

    @NotNull
    @Column(name = "sales_amount", precision = 15, scale = 2)
    private BigDecimal salesAmount;

    @NotNull
    @Column(name = "fee_amount", precision = 15, scale = 2)
    private BigDecimal feeAmount;

    @NotNull
    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;

    @NotNull
    @Column(name = "transaction_at")
    private LocalDateTime transactionAt; // 결제 시간
}
