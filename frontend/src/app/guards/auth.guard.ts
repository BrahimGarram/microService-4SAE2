import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

/**
 * Auth Guard
 * 
 * Protects routes that require authentication.
 * Redirects to login if user is not authenticated.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  
  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}
  
  async canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {
    // Check KeycloakService first
    let isLoggedIn = await this.keycloakService.isLoggedIn();
    
    // If not logged in via KeycloakService, check localStorage (for REST API login)
    if (!isLoggedIn) {
      const token = localStorage.getItem('kc-access-token');
      if (token) {
        isLoggedIn = true;
      }
    }
    
    if (!isLoggedIn) {
      // Redirect to login page instead of Keycloak login
      this.router.navigate(['/login']);
      return false;
    }
    
    return true;
  }
}
