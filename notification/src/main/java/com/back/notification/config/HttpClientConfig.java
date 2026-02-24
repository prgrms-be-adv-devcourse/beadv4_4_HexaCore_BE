package com.back.notification.config;

import com.back.notification.adapter.out.feign.product.ProductFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${feign.product.url}")
    private String productServiceUrl;

    @Bean
    public ProductFeignClient productFeignClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(ProductFeignClient.class);
    }
}
