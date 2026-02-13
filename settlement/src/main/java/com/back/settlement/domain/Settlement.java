package com.back.settlement.domain;

import static com.back.common.code.FailureCode.*;
import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT;

import com.back.common.exception.BadRequestException;
import com.back.settlement.domain.event.SettlementFailedEvent;
import com.back.settlement.domain.event.SettlementHoldEvent;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import com.back.settlement.domain.exception.InvalidSettlementStateException;
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
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "settlement")
public class Settlement extends BaseAggregateEntity<Settlement> {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "seller_id")
    private Long sellerId;

    @NotNull
    @Column(name = "seller_name")
    private String sellerName;

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

    private static final String SYSTEM_NAME = "SYSTEM";

    public static Settlement create(Long sellerId, List<SettlementItem> items, LocalDateTime startAt, LocalDateTime endAt) {
        SettlementAmounts amounts = SettlementAmounts.fromItems(items);
        return Settlement.builder()
                .sellerId(sellerId)
                .sellerName(extractSellerName(items))
                .status(SettlementStatus.PENDING)
                .startAt(startAt)
                .endAt(endAt)
                .expectedAt(calculateExpectedDate(endAt))
                .totalSalesAmount(amounts.totalSalesAmount())
                .totalFeeAmount(amounts.totalFeeAmount())
                .totalNetAmount(amounts.totalNetAmount())
                .build();
    }

    private static String extractSellerName(List<SettlementItem> items) {
        return items.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_AMOUNT)
                .map(SettlementItem::getSellerName)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(SYSTEM_NAME);
    }

    private static LocalDateTime calculateExpectedDate(LocalDateTime endAt) {
        return endAt.plusMonths(1)
                .withDayOfMonth(10)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    // 정산 완료 처리
    public void complete() {
        validateStatusTransition(SettlementStatus.COMPLETED);

        SettlementStatus previousStatus = this.status;
        this.status = SettlementStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();

        registerEvent(new SettlementInternalCompletedEvent(
                this.id,
                previousStatus,
                this.totalNetAmount,
                this.sellerId,
                this.sellerName,
                this.completedAt
        ));
    }

    // 정산 보류 처리
    public void hold(String reason) {
        // TODO 보류 관련 처리시 reason 수정 예정
        String actualReason = (reason == null || reason.isBlank()) ? "판매자 계좌 정보 없음" : reason;

        validateStatusTransition(SettlementStatus.HOLD);

        SettlementStatus previousStatus = this.status;
        this.status = SettlementStatus.HOLD;

        registerEvent(new SettlementHoldEvent(this.id, previousStatus, actualReason));
    }

    // 정산 실패 처리
    public void fail(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException(REQUIRED_FAILURE_REASON);
        }

        validateStatusTransition(SettlementStatus.FAILED);

        SettlementStatus previousStatus = this.status;
        this.status = SettlementStatus.FAILED;

        registerEvent(new SettlementFailedEvent(this.id, previousStatus, reason));
    }

    // 상태 전이 유효성을 검증
    private void validateStatusTransition(SettlementStatus targetStatus) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidSettlementStateException(this.status, targetStatus);
        }
    }

}
