/**
 * Développement local (`ng serve`) : même schéma que Docker — URLs relatives `/gateway`
 * pour rester sur l’origine `http://localhost:4200` (pas de CORS). Le proxy Angular
 * (`proxy.conf.json`) envoie `/gateway` → gateway Maven sur http://localhost:8090.
 * Pour tester contre la stack Docker depuis le host : `npm run start:docker-backend`
 * (proxy vers http://localhost:18090).
 */
export const environment = {
  production: false,
  apiGatewayUrl: '/gateway',

  backendUrl: '/gateway/user',
  complaintsServiceUrl: '/gateway/complaints',
  complaintsApiBaseUrl: '/gateway/complaints',

  imgbbApiKey: '9c862141b3c163e8a8e11109ce021ff3',
  imgbbApiUrl: 'https://api.imgbb.com/1/upload',

  /** Keycloak en local (ton cas : 8080). Build Docker : environment.docker.ts utilise 8180. */
  keycloakUrl: 'http://localhost:8080',
  keycloakRealm: 'projetpidev',
  keycloakClientId: 'freelance-client'
};
