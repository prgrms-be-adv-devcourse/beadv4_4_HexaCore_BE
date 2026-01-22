package com.back.settlement.domain;

import static com.back.settlement.domain.SettlementPolicy.PLATFORM_FEE_RATE;

import com.back.common.entity.BaseTimeEntity;
import com.back.common.dto.settlement.SettlementTargetOrder;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
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
    private Settlement settlement; // nullable: 일일 수집 시 null, 월별 정산 시 연결

    @NotNull
    @Column(name = "order_id")
    private Long orderId;

    @NotNull
    @Column(name = "product_id")
    private Long productId;

    @NotNull
    @Column(name = "payer_id")
    private Long payerId;  // 지불자

    @Column(name = "payee_id")
    private Long payeeId;  // 수취인

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "amount")
    @NotNull
    private BigDecimal amount;  // 금액

    @Enumerated(EnumType.STRING)
    @NotNull
    private SettlementEventType eventType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SettlementItemStatus status;

    @NotNull
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt; // 구매 확정 시간

    public static List<SettlementItem> createSettlementItem(SettlementTargetOrder request, Long systemPayeeId) {
        BigDecimal price = request.price();
        BigDecimal feeAmount = price.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal netAmount = price.subtract(feeAmount);

        SettlementItem salesItem = SettlementItem.builder()
                .settlement(null)
                .orderId(request.orderId())
                .productId(request.productId())
                .payerId(request.buyerId())
                .payeeId(request.sellerId())
                .sellerName(request.sellerName())
                .amount(netAmount)
                .eventType(SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT)
                .status(SettlementItemStatus.INCLUDED)
                .confirmedAt(request.confirmedAt())
                .build();

        SettlementItem feeItem = SettlementItem.builder()
                .settlement(null)
                .orderId(request.orderId())
                .productId(request.productId())
                .payerId(request.buyerId())
                .payeeId(systemPayeeId)
                .sellerName(request.sellerName())
                .amount(feeAmount)
                .eventType(SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE)
                .status(SettlementItemStatus.INCLUDED)
                .confirmedAt(request.confirmedAt())
                .build();

        return List.of(salesItem, feeItem);
    }

    public void addSettlement(Settlement settlement) {
        this.settlement = settlement;
    }
}
