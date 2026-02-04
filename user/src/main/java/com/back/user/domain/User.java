package com.back.user.domain;

import com.back.common.entity.BaseTimeEntity;
import com.back.user.domain.enums.Provider;
import com.back.user.domain.enums.Role;
import com.back.user.dto.request.UserProfileRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String name;

    private String email;

    private String address;
    private String phone;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Builder.Default
    private int blindCount = 0;
    private LocalDateTime chatRestrictedUntil;

    private String fcmToken;

    public static User createSocialUser(String email, String nickname,
                                        Provider provider, String providerId) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .nickname(nickname)
                .email(email)
                .role(Role.USER)
                .build();
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void incrementBlindCount() {
        this.blindCount++;
    }

    public void restrictChatForDays(int days) {
        if (this.chatRestrictedUntil == null
                || this.chatRestrictedUntil.isBefore(LocalDateTime.now())) {
            this.chatRestrictedUntil = LocalDateTime.now().plusDays(days);
        } else {
            this.chatRestrictedUntil = this.chatRestrictedUntil.plusDays(days);
        }
    }

    public void resetBlindCount(){
        this.blindCount=0;
    }

    public void updateProfile(UserProfileRequestDto req) {
        if (req.nickname() != null) this.nickname = req.nickname();
        if (req.name() != null) this.name = req.name();
        if (req.phone() != null) this.phone = req.phone();
        if (req.address() != null) this.address = req.address();
    }
}
