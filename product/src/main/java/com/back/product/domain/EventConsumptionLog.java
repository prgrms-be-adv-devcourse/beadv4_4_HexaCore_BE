package com.back.product.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.product.dto.enums.EventConsumptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE event_consumption_log SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "event_consumption_log")
public class EventConsumptionLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String eventId;

    @Column(nullable = false)
    String eventType;

    @Column(nullable = false)
    String topic;

    @Column(nullable = false)
    Integer partition;

    @Column(nullable = false)
    Long offset;

    @Column(nullable = false)
    @Builder.Default
    EventConsumptionStatus status = EventConsumptionStatus.PROCESSING;

    @Column(nullable = false)
    @Builder.Default
    Long retryCount = 0L;

    @Column(nullable = false)
    String message;

    @Column
    String errorMessage;

    public void updateStatus(EventConsumptionStatus status) {
        this.status = status;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void updateErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
