package com.back.notification.domain;

import com.back.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * User 모듈의 Users 테이블 복제
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "notification_users")
public class NotificationUser extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long id;    // 기존 userId 사용

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(nullable = false)
    private String email;

    private String fcmToken;
}
