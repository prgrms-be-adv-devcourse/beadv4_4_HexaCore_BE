package com.back.market.mapper;

import com.back.market.domain.MarketUser;
import com.back.market.domain.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class MarketUserMapper {
    public MarketUser toEntity(Long id, Role role, String nickname, String email, String address, String phone, String profileImageUrl){
        return MarketUser.builder()
                .id(id)
                .role(role)
                .nickname(nickname)
                .email(email)
                .address(address)
                .phone(phone)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
