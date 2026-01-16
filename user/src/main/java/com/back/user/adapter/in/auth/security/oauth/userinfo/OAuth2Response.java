package com.back.user.adapter.in.auth.security.oauth.userinfo;

import com.back.user.domain.enums.Provider;

public interface OAuth2Response {

    Provider getProvider();
    String getProviderId();
    String getEmail();
}
