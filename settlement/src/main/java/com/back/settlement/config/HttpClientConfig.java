package com.back.settlement.config;

import com.back.settlement.adapter.out.feign.cash.CashFeignClient;
import com.back.settlement.adapter.out.feign.market.OrderFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${feign.order.url}")
    private String orderServiceUrl;

    @Value("${feign.cash.url}")
    private String cashServiceUrl;

    @Bean
    public OrderFeignClient orderFeignClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(orderServiceUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(OrderFeignClient.class);
    }

    @Bean
    public CashFeignClient cashFeignClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(cashServiceUrl)
                .build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(CashFeignClient.class);
    }
}
