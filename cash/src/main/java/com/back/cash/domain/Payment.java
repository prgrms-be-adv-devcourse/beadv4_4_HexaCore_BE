package com.back.cash.domain;

import com.back.cash.domain.enums.PaymentStatus;
import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String tossOrderId; // 토스에서 사용할 주문 번호

    private String paymentKey; // PG사 고유 키

    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private String method; // 결제 수단

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
