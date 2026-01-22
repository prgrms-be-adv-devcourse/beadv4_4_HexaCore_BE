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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "settlement_log")
public class SettlementLog extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private SettlementStatus previousStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private SettlementStatus newStatus;

    @Column(name = "reason", length = 500)
    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", length = 20)
    private ActorType actorType;

    @Column(name = "actor_id")
    private Long actorId;

    public static SettlementLog create(Settlement settlement, SettlementStatus previousStatus, SettlementStatus newStatus, String reason, ActorType actorType) {
        return SettlementLog.builder()
                .settlement(settlement)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .actorType(actorType)
                .actorId(null)
                .build();
    }

    public static SettlementLog createByAdmin(Settlement settlement, SettlementStatus previousStatus, SettlementStatus newStatus, String reason, Long adminId) {
        return SettlementLog.builder()
                .settlement(settlement)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .reason(reason)
                .actorType(ActorType.ADMIN)
                .actorId(adminId)
                .build();
    }

    public enum ActorType {
        SYSTEM,
        ADMIN,
        BATCH
    }
}
