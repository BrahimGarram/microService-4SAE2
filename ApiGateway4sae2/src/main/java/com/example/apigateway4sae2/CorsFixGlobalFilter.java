package com.example.apigateway4sae2;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

/**
 * Évite les en-têtes CORS en double : les backends et le gateway en envoient tous les deux.
 * Ce filtre supprime tous les en-têtes Access-Control-* de la réponse puis en ajoute un seul jeu.
 * Ordre HIGHEST_PRECEDENCE + 1 pour s'exécuter après les autres et décorer la réponse avant envoi.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class CorsFixGlobalFilter implements WebFilter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:4200",
            "http://127.0.0.1:4200"
    );
    private static final String DEFAULT_ORIGIN = "http://localhost:4200";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestOrigin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);
        String allowOrigin = (requestOrigin != null && ALLOWED_ORIGINS.contains(requestOrigin))
                ? requestOrigin
                : DEFAULT_ORIGIN;

        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = super.getHeaders();
                removeCorsHeaders(headers);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                return headers;
            }
        };
        return chain.filter(exchange.mutate().response(response).build());
    }

    private void removeCorsHeaders(HttpHeaders headers) {
        List<String> toRemove = List.of(
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                HttpHeaders.ACCESS_CONTROL_MAX_AGE
        );
        toRemove.forEach(headers::remove);
    }
}
