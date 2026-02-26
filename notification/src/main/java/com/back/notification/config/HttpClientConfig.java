package com.back.notification.config;

import com.back.notification.adapter.out.feign.product.ProductFeignClient;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
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
        log.info("[HttpClientConfig] CAN_OVERRIDE_ACCESS_MODIFIERS: {}",
                new ObjectMapper().getDeserializationConfig().isEnabled(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS));

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
