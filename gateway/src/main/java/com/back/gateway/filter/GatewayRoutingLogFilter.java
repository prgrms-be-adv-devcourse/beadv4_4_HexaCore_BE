package com.back.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GatewayRoutingLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String query = request.getURI().getRawQuery();

        log.info("[Gateway 라우팅 시작] {} {}{}", method, path, query != null ? "?" + query : "");

        return chain.filter(exchange).doFinally(signalType -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeInfo = route != null
                    ? String.format("routeId=%s, target=%s", route.getId(), route.getUri())
                    : "no-matched-route";

            Object status = exchange.getResponse().getStatusCode();
            log.info("[Gateway 라우팅 완료] path={} signal={} status={} | {}", path, signalType, status, routeInfo);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
