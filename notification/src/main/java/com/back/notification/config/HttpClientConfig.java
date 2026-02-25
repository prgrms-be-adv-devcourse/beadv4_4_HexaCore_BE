package com.back.notification.config;

import com.back.notification.adapter.out.feign.product.ProductFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${feign.product.url}")
    private String productServiceUrl;

    @Value("${feign.timeout.connect:3000}")
    private int connectTimeout;

    @Value("${feign.timeout.read:5000}")
    private int readTimeout;

    @Bean
    public ProductFeignClient productFeignClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestClient restClient = RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestFactory(factory)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(ProductFeignClient.class);
    }
}
