package com.back.market.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.market.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * User 모듈의 Users 테이블 데이터를 복제하여 저장하는 테이블
 * 
 * 용도: 판매자/구매자 관련 정보 표시 등 Join 조회 용도
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE market_users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Table(name = "market_users")
public class MarketUser extends BaseTimeEntity {
    
    @Id
    @Column(name = "id")
    private Long id;                // Member 모듈의 PK를 그대로 사용

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;             // 권한(USER, ADMIN)

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;        // 닉네임

    @Column(name = "email", nullable = false)
    private String email;           // 이메일

    @Column(name = "address", length = 500)
    private String address;         // 주소(기본 배송지로 사용예정)

    @Column(name = "phone", length = 20)
    private String phone;           // 연락처

    @Column(name = "profile_image_url")
    private String profileImageUrl; // 프로필 이미지
}
