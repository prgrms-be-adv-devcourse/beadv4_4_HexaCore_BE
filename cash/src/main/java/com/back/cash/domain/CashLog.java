package com.back.cash.domain;

import com.back.cash.domain.enums.Type;
import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CashLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String relTypeCode;
    private Long relId;

    private long amount;
    private long balance;

    @Enumerated(EnumType.STRING)
    private Type type;

    @ManyToOne(fetch = LAZY)
    private Wallet wallet;
}
