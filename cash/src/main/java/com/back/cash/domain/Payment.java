package com.back.cash.domain;

import com.back.cash.domain.enums.PaymentStatus;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_rel",
                        columnNames = {"rel_type", "rel_id"}
                )
        }
)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private RelType relType;
    private Long relId;

    private String tossOrderId; // 토스에서 사용할 주문 번호

    private String paymentKey; // PG사 고유 키

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal walletUsedAmount; // 지갑(예치금) 사용 금액

    @Column(precision = 19, scale = 2)
    private BigDecimal pgAmount; // PG 결제 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime releasedAt;

    public void markAsDone() {
        this.status = PaymentStatus.DONE;
    }

    public void updateAmounts(BigDecimal walletUse, BigDecimal pgNeed) {
        this.walletUsedAmount = walletUse;
        this.pgAmount = pgNeed;
    }

    public void issueTossOrderIdIfAbsent(String tossOrderId) {
        if (this.tossOrderId == null) {
            this.tossOrderId = tossOrderId;
        }
    }

    public void setPaymentKeyIfAbsent(String paymentKey) {
        if (this.paymentKey == null) {
            this.paymentKey = paymentKey;
        }
    }

    public void markAsFail() {
        this.status = PaymentStatus.FAIL;
    }

    public boolean isReleased() {
        return releasedAt != null;
    }

    public void markReleased() {
        this.releasedAt = LocalDateTime.now();
    }

    public void markAsCanceled() {
        this.status = PaymentStatus.CANCELED;
    }
}
