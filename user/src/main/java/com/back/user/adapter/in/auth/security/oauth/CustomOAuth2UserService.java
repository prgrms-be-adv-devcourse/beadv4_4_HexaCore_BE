package com.back.user.adapter.in.auth.security.oauth;

import com.back.common.user.event.UserCreatedEvent;
import com.back.common.user.event.WalletCreateRequestedEvent;
import com.back.common.util.IpAddressExtractor;
import com.back.user.adapter.in.auth.security.oauth.principal.CustomOAuth2User;
import com.back.user.adapter.in.auth.security.oauth.userinfo.GoogleResponse;
import com.back.user.adapter.in.auth.security.oauth.userinfo.KakaoResponse;
import com.back.user.adapter.in.auth.security.oauth.userinfo.NaverResponse;
import com.back.user.adapter.in.auth.security.oauth.userinfo.OAuth2Response;
import com.back.user.adapter.out.UserRepository;
import com.back.user.adapter.out.UserSettingRepository;
import com.back.user.app.auth.GenerateNicknameUseCase;
import com.back.user.domain.User;
import com.back.user.domain.UserSetting;
import com.back.user.domain.enums.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GenerateNicknameUseCase generateNicknameUseCase;
    private final UserSettingRepository userSettingRepository;
    private final ApplicationEventPublisher eventPublisher;

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

        User user;
        boolean isNewUser;

        Optional<User> optionalUser = userRepository.findByProviderAndProviderId(authProvider, providerId);

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            isNewUser = false;
        } else {
            String nickname = generateNicknameUseCase.generateUnique();
            user = userRepository.save(
                    User.createSocialUser(
                            response.getEmail(),
                            nickname,
                            authProvider,
                            providerId
                    ));
            isNewUser = true;
        }


        UserSetting userSetting = userSettingRepository.findByUser(user)
                .orElseGet(() -> userSettingRepository.save(UserSetting.of(user)));

        if (isNewUser) {
            // 회원가입 IP 등록
            String clientIp = getClientIp();
            if (clientIp != null) {
                log.info("[OAuth2 회원가입 IP 등록] userId: {}, IP: {}", user.getId(), clientIp);
            } else {
                log.warn("[OAuth2 회원가입 IP 추출 실패] userId: {}", user.getId());
            }

            eventPublisher.publishEvent(new WalletCreateRequestedEvent(user.getId()));
            eventPublisher.publishEvent(new UserCreatedEvent(
                    user.getId(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getAddress(),
                    user.getPhone(),
                    user.getProfileImageUrl(),
                    clientIp
                    ));
        }

        return new CustomOAuth2User(user.getRole(), user.getId(), oAuth2User.getAttributes());
    }

    /**
     * 클라이언트 IP 추출
     * RequestContextHolder를 사용하여 현재 HTTP 요청에서 IP 가져오기
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            return IpAddressExtractor.extractClientIp(request);
        } catch (Exception e) {
            log.error("IP 추출 중 오류 발생", e);
            return null;
        }
    }
}

