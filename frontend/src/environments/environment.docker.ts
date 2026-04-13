/**
 * Docker : nginx du frontend proxifie /gateway/ → conteneur api-gateway:8090 (voir docker/nginx.conf).
 * Toutes les requêtes API passent par la MÊME origine que l’app (ex. http://localhost:4200) → pas de CORS.
 * Keycloak / OAuth reste sur un autre port (8180), c’est normal.
 */
export const environment = {
  production: true,
  apiGatewayUrl: '/gateway',
  backendUrl: '/gateway/user',
  complaintsServiceUrl: '/gateway/complaints',
  complaintsApiBaseUrl: '/gateway/complaints',

  imgbbApiKey: '9c862141b3c163e8a8e11109ce021ff3',
  imgbbApiUrl: 'https://api.imgbb.com/1/upload',

  keycloakUrl: 'http://localhost:8180',
  keycloakRealm: 'projetpidev',
  keycloakClientId: 'freelance-client'
};
