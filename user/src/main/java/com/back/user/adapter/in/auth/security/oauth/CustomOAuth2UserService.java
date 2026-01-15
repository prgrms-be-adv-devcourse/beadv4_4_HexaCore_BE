package com.back.user.adapter.in.auth.security.oauth;

import com.back.user.adapter.in.auth.security.oauth.principal.CustomOAuth2User;
import com.back.user.adapter.in.auth.security.oauth.userinfo.GoogleResponse;
import com.back.user.adapter.in.auth.security.oauth.userinfo.KakaoResponse;
import com.back.user.adapter.in.auth.security.oauth.userinfo.NaverResponse;
import com.back.user.adapter.in.auth.security.oauth.userinfo.OAuth2Response;
import com.back.user.adapter.out.UserRepository;
import com.back.user.app.auth.GenerateNicknameUseCase;
import com.back.user.domain.User;
import com.back.user.domain.enums.Provider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GenerateNicknameUseCase generateNicknameUseCase;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("소셜 로그인한 사용자 정보 : {}", oAuth2User.getAttributes());

        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response response = switch (provider) {
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            case "naver" -> new NaverResponse(oAuth2User.getAttributes());
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + provider);
        };

        Provider authProvider = response.getProvider();
        String providerId = response.getProviderId();

        User user = userRepository.findByProviderAndProviderId(authProvider, providerId)
                .orElseGet(() -> {
                    String nickname = generateNicknameUseCase.generateUnique();
                    return userRepository.save(
                            User.createSocialUser(
                                    response.getEmail(),
                                    nickname,
                                    authProvider,
                                    providerId
                            )
                    );
                });

        return new CustomOAuth2User(user.getRole(), user.getId(), oAuth2User.getAttributes());
    }
}

