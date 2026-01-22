package com.back.cash.config;

import com.back.cash.adapter.out.market.MarketPaymentsClient;
import com.back.cash.adapter.out.market.MarketPaymentsHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Profile("!local")
public class HttpClientConfig {

    @Bean
    public MarketPaymentsClient marketPaymentsClient(
            @Value("${market.base-url}") String marketBaseUrl
    ) {
        RestClient restClient = RestClient.builder()
                .baseUrl(marketBaseUrl)
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(MarketPaymentsHttpClient.class);
    }
}
