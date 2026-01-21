package com.back.user.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "user_notification_settings")
public class UserSetting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private boolean bidStatusEnabled = true;

    @Builder.Default
    private boolean productStatusEnabled = true;

    @Builder.Default
    private boolean priceEnabled = true;

    @Builder.Default
    private boolean settlementEnabled = true;

    public static UserSetting of(User user) {
        return UserSetting.builder()
                .user(user)
                .build();
    }

    public void setBidStatusEnabled(Boolean enabled) {
        this.bidStatusEnabled = enabled;
    }

    public void setProductStatusEnabled(Boolean enabled) {
        this.productStatusEnabled = enabled;
    }

    public void setPriceEnabled(Boolean enabled) {
        this.priceEnabled = enabled;
    }

    public void setSettlementEnabled(Boolean enabled) {
        this.settlementEnabled = enabled;
    }
}
