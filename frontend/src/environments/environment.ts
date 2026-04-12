/**
 * Tous les appels backend passent par l'API Gateway.
 * - ng serve + gateway lancée en local (Maven) : http://localhost:8090
 * - Tout en Docker : voir environment.docker.ts → gateway http://localhost:18090
 * Eureka (ce repo Docker) : http://localhost:18761 — le service collaboration (Symfony) n’y apparaît pas (pas Java / pas client Eureka).
 */
export const environment = {
  production: false,
  /** Base URL de l'API Gateway - tous les services sont accessibles via ce port */
  apiGatewayUrl: 'http://localhost:8090',

  /* URLs dérivées du gateway (ne pas utiliser les ports directs des services) */
  backendUrl: 'http://localhost:8090/user',
  complaintsServiceUrl: 'http://localhost:8090/complaints',
  complaintsApiBaseUrl: 'http://localhost:8090/complaints',

  imgbbApiKey: '9c862141b3c163e8a8e11109ce021ff3',
  imgbbApiUrl: 'https://api.imgbb.com/1/upload',

  /** Keycloak en local (ton cas : 8080). Build Docker : environment.docker.ts utilise 8180. */
  keycloakUrl: 'http://localhost:8080',
  keycloakRealm: 'projetpidev',
  keycloakClientId: 'freelance-client'
};
