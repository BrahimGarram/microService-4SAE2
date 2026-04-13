<?php

namespace App\Security;

use Firebase\JWT\JWT;
use Firebase\JWT\JWK;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Security\Core\Authentication\Token\TokenInterface;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Component\Security\Http\Authenticator\AbstractAuthenticator;
use Symfony\Component\Security\Http\Authenticator\Passport\Badge\UserBadge;
use Symfony\Component\Security\Http\Authenticator\Passport\Passport;
use Symfony\Component\DependencyInjection\Attribute\Autowire;
use Symfony\Component\Security\Http\Authenticator\Passport\SelfValidatingPassport;
use Symfony\Contracts\HttpClient\Exception\HttpExceptionInterface;
use Symfony\Contracts\HttpClient\Exception\TransportExceptionInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class KeycloakJwtAuthenticator extends AbstractAuthenticator
{
    private static ?array $jwksKeys = null;

    private static float $jwksFetchedAt = 0;

    private const JWKS_TTL = 300;

    public function __construct(
        private readonly HttpClientInterface $httpClient,
        #[Autowire(env: 'KEYCLOAK_BASE_URL')] private readonly string $keycloakBaseUrl,
        #[Autowire(env: 'KEYCLOAK_REALM')] private readonly string $keycloakRealm,
    ) {
    }

    public function supports(Request $request): ?bool
    {
        if (!str_starts_with($request->getPathInfo(), '/api')) {
            return false;
        }

        return $request->headers->has('Authorization');
    }

    public function authenticate(Request $request): Passport
    {
        $header = $request->headers->get('Authorization', '');
        if (!str_starts_with($header, 'Bearer ')) {
            throw new AuthenticationException('Missing Bearer token');
        }

        $jwt = trim(substr($header, 7));
        if ('' === $jwt) {
            throw new AuthenticationException('Empty JWT');
        }

        $jwksUrl = rtrim($this->keycloakBaseUrl, '/').'/realms/'.$this->keycloakRealm.'/protocol/openid-connect/certs';
        $keys = $this->getJwksKeys($jwksUrl);

        try {
            $decoded = JWT::decode($jwt, $keys);
        } catch (\Throwable $e) {
            throw new AuthenticationException('Invalid JWT: '.$e->getMessage(), 0, $e);
        }

        $payload = (array) $decoded;

        $sub = (string) ($payload['sub'] ?? '');
        if ('' === $sub) {
            throw new AuthenticationException('Token without sub');
        }

        $username = (string) ($payload['preferred_username'] ?? $payload['username'] ?? $sub);
        $realmAccess = $payload['realm_access'] ?? null;
        $roleNames = [];
        if (\is_object($realmAccess) && isset($realmAccess->roles) && \is_array($realmAccess->roles)) {
            $roleNames = $realmAccess->roles;
        } elseif (\is_array($realmAccess) && isset($realmAccess['roles']) && \is_array($realmAccess['roles'])) {
            $roleNames = $realmAccess['roles'];
        }

        $symfonyRoles = ['ROLE_USER'];
        foreach ($roleNames as $r) {
            $symfonyRoles[] = 'ROLE_'.strtoupper((string) $r);
        }
        $symfonyRoles = array_values(array_unique($symfonyRoles));

        return new SelfValidatingPassport(
            new UserBadge($sub, function () use ($sub, $username, $symfonyRoles) {
                return new KeycloakUser($sub, $username, $symfonyRoles);
            })
        );
    }

    /**
     * @return array<string, mixed>
     */
    private function getJwksKeys(string $jwksUrl): array
    {
        $now = microtime(true);
        if (null !== self::$jwksKeys && ($now - self::$jwksFetchedAt) < self::JWKS_TTL) {
            return self::$jwksKeys;
        }

        try {
            $response = $this->httpClient->request('GET', $jwksUrl, ['timeout' => 10]);
            $statusCode = $response->getStatusCode();
            if (200 !== $statusCode) {
                throw new AuthenticationException(\sprintf(
                    'JWKS Keycloak HTTP %d (%s). Vérifiez KEYCLOAK_BASE_URL / KEYCLOAK_REALM.',
                    $statusCode,
                    $jwksUrl
                ));
            }
            $data = $response->toArray(false);
        } catch (TransportExceptionInterface|HttpExceptionInterface $e) {
            throw new AuthenticationException(
                'Impossible de joindre Keycloak pour valider le JWT (JWKS). '
                .'KEYCLOAK_BASE_URL doit être accessible depuis PHP (ex. http://localhost:8080 ou :8180 selon votre stack). '
                .$e->getMessage(),
                0,
                $e
            );
        }

        try {
            self::$jwksKeys = JWK::parseKeySet($data);
        } catch (\Throwable $e) {
            throw new AuthenticationException('Réponse JWKS invalide: '.$e->getMessage(), 0, $e);
        }
        self::$jwksFetchedAt = $now;

        return self::$jwksKeys;
    }

    public function onAuthenticationSuccess(Request $request, TokenInterface $token, string $firewallName): ?Response
    {
        return null;
    }

    public function onAuthenticationFailure(Request $request, AuthenticationException $exception): ?Response
    {
        return new JsonResponse(['error' => $exception->getMessage()], Response::HTTP_UNAUTHORIZED);
    }
}
