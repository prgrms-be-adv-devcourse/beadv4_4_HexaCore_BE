package com.back.user.adapter.in.auth.security.oauth.principal;

import com.back.user.domain.enums.Role;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private final Role role;
    private final Long userId;
    private final Map<String, Object> attribute;

    public CustomOAuth2User(Long userId, Role role) {
        this.role = role;
        this.userId = userId;
        this.attribute = Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attribute;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }

    public Long getUserId() {
        return userId;
    }

}
