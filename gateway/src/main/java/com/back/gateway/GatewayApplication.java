package com.back.gateway;

import com.back.gateway.config.GatewayServiceProperties;
import com.back.security.jwt.JWTUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
        scanBasePackageClasses = {GatewayApplication.class, JWTUtil.class}
)
@EnableConfigurationProperties(GatewayServiceProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
