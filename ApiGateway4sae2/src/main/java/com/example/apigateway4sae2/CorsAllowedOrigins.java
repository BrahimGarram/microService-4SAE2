package com.example.apigateway4sae2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parse {@code gateway.cors.allowed-origins} (CSV). Si vide ou mal résolu (env Docker),
 * retombe sur les origines dev courantes pour éviter zéro en-tête CORS.
 */
public final class CorsAllowedOrigins {

    private static final Set<String> FALLBACK = Set.of(
            "http://localhost:4200",
            "http://127.0.0.1:4200",
            "http://[::1]:4200");

    private CorsAllowedOrigins() {
    }

    public static Set<String> asSet(String csv) {
        if (csv == null || csv.isBlank()) {
            return new LinkedHashSet<>(FALLBACK);
        }
        Set<String> s = Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(v -> v.replaceAll("^\"|\"$", ""))
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (s.isEmpty()) {
            return new LinkedHashSet<>(FALLBACK);
        }
        return s;
    }

    public static List<String> asList(String csv) {
        return new ArrayList<>(asSet(csv));
    }
}
