package com.back.settlement.domain;

import static com.back.common.code.FailureCode.*;

import com.back.common.entity.BaseTimeEntity;
import com.back.common.exception.BadRequestException;
import com.back.settlement.app.dto.request.SettlementRequest;
import com.back.settlement.domain.event.SettlementCreatedEvent;
import com.back.settlement.domain.event.SettlementEvent;
import com.back.settlement.domain.event.SettlementFailedEvent;
import com.back.settlement.domain.event.SettlementHoldEvent;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import com.back.settlement.domain.event.SettlementStartedEvent;
import com.back.settlement.domain.exception.InvalidSettlementStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
@Table(name = "settlement")
public class Settlement extends BaseTimeEntity {

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

    @Transient
    private final List<SettlementEvent> domainEvents = new ArrayList<>();

    public static Settlement createSettlement(SettlementRequest request) {
        Settlement settlement = Settlement.builder()
                .sellerId(request.sellerId())
                .sellerName(request.sellerName())
                .status(SettlementStatus.PENDING)
                .startAt(request.startAt())
                .endAt(request.endAt())
                .expectedAt(calculateExpectedDate(request.endAt()))
                .completedAt(null)
                .totalSalesAmount(request.totalSalesAmount())
                .totalFeeAmount(request.totalFeeAmount())
                .totalNetAmount(request.totalNetAmount())
                .build();

        settlement.registerEvent(new SettlementCreatedEvent(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getTotalNetAmount()
        ));
        return settlement;
    }

    private static LocalDateTime calculateExpectedDate(LocalDateTime endAt) {
        return endAt.plusMonths(1)
                .withDayOfMonth(10)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    // 정산 시작 처리
    public void start() {
        validateStatusTransition(SettlementStatus.IN_PROGRESS);

        SettlementStatus previousStatus = this.status;
        this.status = SettlementStatus.IN_PROGRESS;

        registerEvent(new SettlementStartedEvent(this.id, previousStatus));
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
                this.sellerId
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

    // 도메인 이벤트 등록
    private void registerEvent(SettlementEvent event) {
        this.domainEvents.add(event);
    }

    // 등록된 도메인 이벤트 목록 조회
    public List<SettlementEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    // 등록된 도메인 이벤트 제거
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
