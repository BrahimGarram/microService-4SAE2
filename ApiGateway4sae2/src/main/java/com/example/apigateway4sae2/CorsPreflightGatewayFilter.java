package com.example.apigateway4sae2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Répond au prévol OPTIONS pour <strong>tous</strong> les chemins (pas seulement /collaboration).
 * Sinon Spring/Gateway peut renvoyer un 200/204 sans {@code Access-Control-Allow-Origin} → échec navigateur.
 * Doit s’exécuter en premier dans la chaîne Gateway.
 */
@Component
public class CorsPreflightGatewayFilter implements GlobalFilter, Ordered {

    private final Set<String> allowedOrigins;

    public CorsPreflightGatewayFilter(
            @Value("${gateway.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://[::1]:4200}") String allowedOriginsCsv) {
        this.allowedOrigins = CorsAllowedOrigins.asSet(allowedOriginsCsv);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getMethod() != HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        String origin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (origin == null || !allowedOrigins.contains(origin)) {
            return chain.filter(exchange);
        }
        HttpHeaders h = exchange.getResponse().getHeaders();
        h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        String reqHeaders = exchange.getRequest().getHeaders()
                .getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        String allowHeaders = (reqHeaders != null && !reqHeaders.isBlank())
                ? reqHeaders
                : "Authorization, Content-Type, Accept, Accept-Language, Origin, X-Requested-With";
        h.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
        h.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
        exchange.getResponse().setStatusCode(HttpStatus.NO_CONTENT);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
