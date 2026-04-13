/**
 * Production: tous les appels passent par l'API Gateway (même base que le frontend ou URL configurée).
 */
export const environment = {
  production: true,
  apiGatewayUrl: '',
  backendUrl: '/user',
  complaintsServiceUrl: '/complaints',
  complaintsApiBaseUrl: '/complaints',

  imgbbApiKey: '',
  imgbbApiUrl: 'https://api.imgbb.com/1/upload',
  keycloakUrl: '',
  keycloakRealm: 'projetpidev',
  keycloakClientId: 'freelance-client'
};
