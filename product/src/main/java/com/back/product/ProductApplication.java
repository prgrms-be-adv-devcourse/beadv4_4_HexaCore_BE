package com.back.product;

import com.back.security.config.BaseSecurityConfig;
import com.back.security.jwt.JWTFilter;
import com.back.security.jwt.JWTUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@Import({ // Importing security configurations and utilities for use in the product module
    BaseSecurityConfig.class,
    JWTFilter.class,
    JWTUtil.class
})
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
