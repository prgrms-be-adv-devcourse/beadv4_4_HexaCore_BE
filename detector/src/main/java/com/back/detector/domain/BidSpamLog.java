package com.back.detector.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "bid_spam_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class BidSpamLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private int requestCount;

    @Column(nullable = false)
    private int timeWindowMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidSpamBanLevel banLevel;
}
