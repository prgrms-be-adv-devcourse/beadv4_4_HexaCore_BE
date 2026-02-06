package com.back.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableFeignClients //FeignClient 사용 위해 추가
@EnableJpaAuditing //BaseTimeEntity 작동 위해 추가
@SpringBootApplication(scanBasePackages = {
        "com.back.common",
        "com.back.security",
        "com.back.market"
}) // common 모듈 스캔을 위해 추가
public class MarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketApplication.class, args);
    }

}
