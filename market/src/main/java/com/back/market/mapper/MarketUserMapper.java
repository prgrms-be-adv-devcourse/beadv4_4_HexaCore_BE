package com.back.market.mapper;

import com.back.market.domain.MarketUser;
import org.springframework.stereotype.Component;

@Component
public class MarketUserMapper {
    public MarketUser toEntity(Long id, String name, String email, String address, String phone, String profileImageUrl){
        return MarketUser.builder()
                .id(id)
                .name(name)
                .email(email)
                .address(address)
                .phone(phone)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
