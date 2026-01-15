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
public class UserNotificationSetting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    private boolean bdStatusEnabled = true;

    @Builder.Default
    private boolean productStatusEnabled = true;

    @Builder.Default
    private boolean priceEnabled = true;

    @Builder.Default
    private boolean settlementEnabled = true;

}
