package com.back.cash.domain;


import com.back.cash.domain.enums.RelType;
import com.back.cash.domain.enums.Type;
import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    private RelType relType;

    private Long relId;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount; // 입출금액

    @Column(precision = 19, scale = 4)
    private BigDecimal balance; // 잔액

    @Enumerated(EnumType.STRING)
    private Type type;

}
