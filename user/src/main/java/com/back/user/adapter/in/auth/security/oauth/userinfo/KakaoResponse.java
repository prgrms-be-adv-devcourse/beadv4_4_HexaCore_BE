package com.back.user.adapter.in.auth.security.oauth.userinfo;

import com.back.user.domain.enums.Provider;

import java.util.Map;

public class KakaoResponse implements OAuth2Response{

    private final Map<String, Object> attribute;
    private final Map<String, Object> kakaoAccount;

    public KakaoResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
        this.kakaoAccount = (Map<String,Object>) attribute.get("kakao_account");
    }

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public String getProviderId() {
        return attribute.get("id").toString();
    }

    @Override
    public String getEmail() {
        return kakaoAccount.get("email").toString();
    }
}
