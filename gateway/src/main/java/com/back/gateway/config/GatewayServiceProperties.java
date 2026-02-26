package com.back.gateway.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "custom.auth")
public class GatewayServiceProperties {
    /** 메서드 무관하게 인증 없이 허용 */
    private List<String> publicPaths = new ArrayList<>();

    /** GET 요청만 인증 없이 허용 */
    private List<String> publicGetPaths = new ArrayList<>();

    private String adminApiPathPrefix;
    private String adminRequiredRole = "ROLE_ADMIN";
}
