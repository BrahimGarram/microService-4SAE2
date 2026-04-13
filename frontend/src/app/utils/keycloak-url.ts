import { environment } from '../../environments/environment';

/** Clé localStorage pour forcer l’URL Keycloak sans rebuild (ex. basculer 8080 ↔ 8180). */
export const KEYCLOAK_URL_STORAGE_KEY = 'freelance_keycloak_url';

/**
 * URL du serveur Keycloak pour ce build :
 * - `environment.ts` (ng serve par défaut) : http://localhost:8080
 * - `environment.docker.ts` (image Docker ou `ng serve --configuration docker`) : http://localhost:8180
 *
 * Surcharge : `localStorage.setItem('freelance_keycloak_url', 'http://localhost:8180')` puis recharger la page.
 */
export function getKeycloakUrl(): string {
  if (typeof window !== 'undefined' && window.localStorage) {
    const override = window.localStorage.getItem(KEYCLOAK_URL_STORAGE_KEY)?.trim();
    if (override) {
      return override.replace(/\/$/, '');
    }
  }
  return (environment.keycloakUrl || '').trim().replace(/\/$/, '');
}
