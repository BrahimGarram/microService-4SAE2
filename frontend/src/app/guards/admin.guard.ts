import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

/**
 * Admin Guard
 * 
 * Protects routes that require ADMIN role.
 * Redirects to home if user is not an admin.
 */
@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  
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
    
    // Check if user has ADMIN role
    try {
      let userRoles: string[] = [];
      
      // First, try to decode token directly from localStorage (most reliable)
      const token = localStorage.getItem('kc-access-token');
      if (token) {
        try {
          // Decode JWT token to get roles
          const tokenPayload = JSON.parse(atob(token.split('.')[1]));
          console.log('AdminGuard: Token payload:', tokenPayload);
          
          if (tokenPayload.realm_access && tokenPayload.realm_access.roles) {
            userRoles = tokenPayload.realm_access.roles;
            console.log('AdminGuard: Roles from token:', userRoles);
            
            // Also update localStorage with fresh roles from token
            localStorage.setItem('kc-roles', JSON.stringify(userRoles));
          }
        } catch (tokenError) {
          console.error('AdminGuard: Error decoding token:', tokenError);
        }
      }
      
      // If no roles from token, try KeycloakService
      if (userRoles.length === 0) {
        try {
          userRoles = await this.keycloakService.getUserRoles();
          console.log('AdminGuard: Roles from KeycloakService:', userRoles);
        } catch (error) {
          console.error('AdminGuard: Error getting roles from KeycloakService:', error);
        }
      }
      
      // If still no roles, try localStorage
      if (userRoles.length === 0) {
        const rolesStr = localStorage.getItem('kc-roles');
        if (rolesStr) {
          try {
            userRoles = JSON.parse(rolesStr);
            console.log('AdminGuard: Roles from localStorage:', userRoles);
          } catch (parseError) {
            console.error('AdminGuard: Error parsing roles from localStorage:', parseError);
          }
        }
      }
      
      console.log('AdminGuard: Final user roles:', userRoles);
      console.log('AdminGuard: Roles type:', typeof userRoles, 'Is array:', Array.isArray(userRoles));
      
      // Check for ADMIN role (case-insensitive)
      const isAdmin = Array.isArray(userRoles) && userRoles.some((role: string) => {
        const roleUpper = String(role).toUpperCase();
        console.log('AdminGuard: Checking role:', role, '->', roleUpper);
        return roleUpper === 'ADMIN';
      });
      
      console.log('AdminGuard: Is admin:', isAdmin);
      
      if (!isAdmin) {
        console.log('AdminGuard: User is not admin, redirecting to home');
        // Redirect to home if not admin
        this.router.navigate(['/']);
        return false;
      }
      
      console.log('AdminGuard: User is admin, allowing access');
      return true;
    } catch (error) {
      console.error('AdminGuard: Error checking admin role:', error);
      this.router.navigate(['/']);
      return false;
    }
  }
}
