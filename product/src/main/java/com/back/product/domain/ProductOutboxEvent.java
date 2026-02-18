package com.back.product.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.product.dto.enums.OutboxEventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE product_outbox_event SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "product_outbox_event")
public class ProductOutboxEvent extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OutboxEventStatus status = OutboxEventStatus.INIT;

    @Column(nullable = false)
    @Builder.Default
    private Long retryCount = 0L;

    public void markAsSucceeded() {
        this.status = OutboxEventStatus.SUCCEEDED;
    }

    public void markAsFailed() {
        this.status = OutboxEventStatus.FAILED;
        this.retryCount += 1L;
    }
}
