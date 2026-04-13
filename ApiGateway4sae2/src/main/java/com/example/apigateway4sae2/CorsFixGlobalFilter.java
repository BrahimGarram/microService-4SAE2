package com.example.apigateway4sae2;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.beans.factory.annotation.Value;
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

    private final Set<String> allowedOrigins;

    public CorsFixGlobalFilter(
            @Value("${gateway.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://[::1]:4200}") String allowedOriginsCsv) {
        this.allowedOrigins = CorsAllowedOrigins.asSet(allowedOriginsCsv);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestOrigin = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ORIGIN);
        final String allowOrigin = (requestOrigin != null && allowedOrigins.contains(requestOrigin))
                ? requestOrigin
                : null;

        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public HttpHeaders getHeaders() {
                // Copie mutable : super.getHeaders() peut être en lecture seule (ReadOnlyHttpHeaders) → remove() = 500 Whitelabel
                HttpHeaders headers = new HttpHeaders();
                headers.addAll(super.getHeaders());
                removeCorsHeaders(headers);
                if (allowOrigin != null) {
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowOrigin);
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                    // Avec credentials=true, les navigateurs refusent Allow-Headers: * (preflight échoue).
                    // Répercuter la demande du client ou une liste explicite (ex. Authorization via Keycloak).
                    String reqHeaders = exchange.getRequest().getHeaders()
                            .getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
                    String allowHeaders = (reqHeaders != null && !reqHeaders.isBlank())
                            ? reqHeaders
                            : "Authorization, Content-Type, Accept, Accept-Language, Origin, X-Requested-With";
                    headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
                    headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
                }
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
