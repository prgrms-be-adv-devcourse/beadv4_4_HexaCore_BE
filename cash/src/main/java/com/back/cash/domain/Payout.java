package com.back.cash.domain;

import com.back.cash.domain.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(
        name = "payout",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payout_settlement",
                        columnNames = {"settlement_id"}
                )
        }
)
public class Payout {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long settlementId;

    private Long payeeId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalGrossAmount;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalNetAmount;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalFeeAmount;

    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status;

    String failReason;

    public void markDone() {
        this.status = PayoutStatus.DONE;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PayoutStatus.FAILED;
        this.failReason = reason;
    }
}

