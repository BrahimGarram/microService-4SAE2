/**
 * Tous les appels backend passent par l'API Gateway (port 8090).
 * Microservices exposés : user, project, complaints, service-test.
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

  keycloakUrl: 'http://localhost:8080',
  keycloakRealm: 'projetpidev',
  keycloakClientId: 'freelance-client'
};
