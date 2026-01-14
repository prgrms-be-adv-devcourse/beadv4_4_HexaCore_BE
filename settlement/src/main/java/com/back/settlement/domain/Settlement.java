package com.back.settlement.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "settlement")
public class Settlement extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "seller_id")
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false, length = 20)
    private SettlementStatus status;

    @Column(name = "settlement_expected_at")
    private LocalDateTime expectedAt; // 정산 예정일

    @NotNull
    @Column(name = "settlement_start_at")
    private LocalDateTime startAt; // 정산 시작일

    @NotNull
    @Column(name = "settlement_end_at")
    private LocalDateTime endAt; // 정산 종료일

    @Column(name = "settlement_completed_at")
    private LocalDateTime completedAt; // 정산 완료일

    @NotNull
    @Column(name = "total_sales_amount", precision = 15, scale = 2)
    private BigDecimal totalSalesAmount;

    @NotNull
    @Column(name = "total_fee_amount", precision = 15, scale = 2)
    private BigDecimal totalFeeAmount;

    @NotNull
    @Column(name = "total_net_amount", precision = 15, scale = 2)
    private BigDecimal totalNetAmount;
}
