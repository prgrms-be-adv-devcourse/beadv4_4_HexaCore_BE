package com.back.notification.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "notification_user_settings")
public class NotificationUserSetting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private NotificationUser user;

    @Builder.Default
    private boolean bidStatusEnabled = true;

    @Builder.Default
    private boolean productStatusEnabled = true;

    @Builder.Default
    private boolean priceEnabled = true;

    @Builder.Default
    private boolean settlementEnabled = true;

    public void update(boolean bidStatusEnabled, boolean productStatusEnabled,
                       boolean priceEnabled, boolean settlementEnabled) {
        this.bidStatusEnabled = bidStatusEnabled;
        this.productStatusEnabled = productStatusEnabled;
        this.priceEnabled = priceEnabled;
        this.settlementEnabled = settlementEnabled;
    }
}
