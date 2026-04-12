package com.freelance.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.Arrays;
import java.util.List;

/**
 * En Docker, les tokens Keycloak ont souvent {@code iss=http://localhost:8180/realms/...}
 * (URL utilisée par le navigateur) alors que le service charge les clés via {@code http://keycloak:8080}.
 * Un {@code issuer-uri} unique fait échouer la validation. Ce décodeur charge les JWK en interne
 * et accepte une liste d'émetteurs de confiance.
 */
@Configuration
@Profile("docker")
public class DockerResourceServerJwtConfig {

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${app.security.jwt.trusted-issuers:}") String trustedIssuersRaw) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        List<String> trusted = Arrays.stream(trustedIssuersRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        List<String> effectiveTrusted = trusted.isEmpty() ? defaultTrustedIssuers() : trusted;

        OAuth2TokenValidator<Jwt> issValidator = jwt -> {
            String iss = jwt.getClaimAsString(JwtClaimNames.ISS);
            if (iss != null && effectiveTrusted.contains(iss)) {
                return OAuth2TokenValidatorResult.success();
            }
            OAuth2Error err = new OAuth2Error("invalid_token", "Unexpected iss claim", null);
            return OAuth2TokenValidatorResult.failure(err);
        };

        OAuth2TokenValidator<Jwt> validators = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issValidator
        );
        decoder.setJwtValidator(validators);
        return decoder;
    }

    private static List<String> defaultTrustedIssuers() {
        return List.of(
                "http://localhost:8180/realms/projetpidev",
                "http://127.0.0.1:8180/realms/projetpidev",
                "http://keycloak:8080/realms/projetpidev"
        );
    }
}
