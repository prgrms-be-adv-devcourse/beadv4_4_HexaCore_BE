package com.back.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Resello 프로젝트 API")
                .version("1.0.0")
                .description("Resello 멀티모듈 프로젝트의 API 명세서입니다.");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}
