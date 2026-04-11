import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

/**
 * Freelancer Guard
 *
 * Réserve l'accès aux routes (ex: Test) aux utilisateurs ayant le rôle FREELANCER.
 * Redirige vers la page "Espace réservé aux freelancers" si l'utilisateur est USER ou ADMIN.
 */
@Injectable({
  providedIn: 'root'
})
export class FreelancerGuard implements CanActivate {

  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  async canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {
    let isLoggedIn = await this.keycloakService.isLoggedIn();

    if (!isLoggedIn) {
      const token = localStorage.getItem('kc-access-token');
      if (token) isLoggedIn = true;
    }

    if (!isLoggedIn) {
      this.router.navigate(['/login']);
      return false;
    }

    let userRoles: string[] = [];
    const token = localStorage.getItem('kc-access-token');
    if (token) {
      try {
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        if (tokenPayload.realm_access?.roles) {
          userRoles = tokenPayload.realm_access.roles;
          localStorage.setItem('kc-roles', JSON.stringify(userRoles));
        }
      } catch (_) {}
    }
    if (userRoles.length === 0) {
      try {
        userRoles = await this.keycloakService.getUserRoles();
      } catch (_) {}
    }
    if (userRoles.length === 0) {
      const rolesStr = localStorage.getItem('kc-roles');
      if (rolesStr) {
        try {
          userRoles = JSON.parse(rolesStr);
        } catch (_) {}
      }
    }

    const isFreelancer = Array.isArray(userRoles) &&
      userRoles.some((r: string) => String(r).toUpperCase() === 'FREELANCER');

    if (!isFreelancer) {
      this.router.navigate(['/freelancer-only']);
      return false;
    }
    return true;
  }
}
