package com.back.market.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.market.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE orders SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "orders")
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;                             // PK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_bidding_id", nullable = false)
    private Bidding buyBidding;                 // 구매 입찰 내역(1:1)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_bidding_id", nullable = false)
    private Bidding sellBidding;                // 판매 입찰 내역(1:1)

    @Column(name = "price", nullable = false)
    private Long price;                          // 체결 가격(입찰가 변동과 무관)

    @Column(name = "address", nullable = false, length = 500)
    private String address;                      // 배송지 주소(사용자 주소 변경과 무관)

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;             // HOLD, PAID, CANCELLED

    @Column(name = "request_payment_date", nullable = false)
    private LocalDateTime requestPaymentDate;    // 결제 요청일 (주문 생성 시점)

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;           // 실제 결제일 (결제 완료 시 업데이트)
}
