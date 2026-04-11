/**
 * Ports publiés par docker-compose (hôte) :
 * - Frontend (nginx) : 4200
 * - API Gateway : 18090
 * - Keycloak : 8180
 * - Eureka : 18761
 * - user-service (accès direct) : 18081
 * - service-test (accès direct) : 18089
 * - MySQL : 3307
 *
 * Le navigateur appelle la gateway sur 18090 (CORS : origine http://localhost:4200).
 */
export const environment = {
  production: true,
  apiGatewayUrl: 'http://localhost:18090',
  backendUrl: 'http://localhost:18090/user',
  complaintsServiceUrl: 'http://localhost:18090/complaints',
  complaintsApiBaseUrl: 'http://localhost:18090/complaints',

  imgbbApiKey: '9c862141b3c163e8a8e11109ce021ff3',
  imgbbApiUrl: 'https://api.imgbb.com/1/upload',

  keycloakUrl: 'http://localhost:8180',
  keycloakRealm: 'projetpidev',
  keycloakClientId: 'freelance-client'
};
