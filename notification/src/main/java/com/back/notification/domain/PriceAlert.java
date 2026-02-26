package com.back.notification.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "price_alerts")
public class PriceAlert extends BaseTimeEntity {
    @Id
    @Column(name = "price_alert_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private NotificationUser user;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private BigDecimal targetPrice;

    private LocalDateTime triggeredAt;

}
