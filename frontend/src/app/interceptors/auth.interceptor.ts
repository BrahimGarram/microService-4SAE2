import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, from, throwError } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { rewriteLegacyGatewayUrl } from '../utils/gateway-url';
import { getKeycloakUrl } from '../utils/keycloak-url';

/**
 * Auth Interceptor
 * 
 * Automatically attaches JWT token to all HTTP requests.
 * Gets the token from Keycloak and adds it to the Authorization header.
 * Checks token expiration and refreshes if needed.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  
  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}
  
  /**
   * Check if token is expired or will expire soon (within 60 seconds)
   */
  private isTokenExpired(token: string): boolean {
    try {
      const tokenPayload = JSON.parse(atob(token.split('.')[1]));
      const exp = tokenPayload.exp * 1000; // Convert to milliseconds
      const now = Date.now();
      const timeUntilExpiry = exp - now;
      
      // Token is expired or will expire within 60 seconds
      return timeUntilExpiry < 60000;
    } catch (e) {
      console.error('Error checking token expiration:', e);
      return true; // If we can't parse, assume expired
    }
  }
  
  /**
   * Refresh token using refresh_token from localStorage
   */
  private async refreshToken(): Promise<string | null> {
    const refreshToken = localStorage.getItem('kc-refresh-token');
    if (!refreshToken) {
      console.warn('No refresh token available');
      return null;
    }
    
    try {
      const keycloakUrl = getKeycloakUrl();
      const realm = environment.keycloakRealm;
      const clientId = environment.keycloakClientId;

      const response = await fetch(
        `${keycloakUrl}/realms/${realm}/protocol/openid-connect/token`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: new URLSearchParams({
            grant_type: 'refresh_token',
            refresh_token: refreshToken,
            client_id: clientId
          })
        }
      );
      
      if (!response.ok) {
        console.error('Token refresh failed:', response.statusText);
        // Clear tokens and redirect to login
        localStorage.removeItem('kc-access-token');
        localStorage.removeItem('kc-refresh-token');
        localStorage.removeItem('kc-id-token');
        this.router.navigate(['/login']);
        return null;
      }
      
      const data = await response.json();
      
      // Store new tokens
      if (data.access_token) {
        localStorage.setItem('kc-access-token', data.access_token);
      }
      if (data.refresh_token) {
        localStorage.setItem('kc-refresh-token', data.refresh_token);
      }
      if (data.id_token) {
        localStorage.setItem('kc-id-token', data.id_token);
      }
      
      return data.access_token || null;
    } catch (error) {
      console.error('Error refreshing token:', error);
      // Clear tokens and redirect to login
      localStorage.removeItem('kc-access-token');
      localStorage.removeItem('kc-refresh-token');
      localStorage.removeItem('kc-id-token');
      this.router.navigate(['/login']);
      return null;
    }
  }
  
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const fixedUrl = rewriteLegacyGatewayUrl(request.url);
    if (fixedUrl !== request.url) {
      request = request.clone({ url: fixedUrl });
    }
    // Try to get token from Keycloak first
    return from(this.keycloakService.getToken().catch(() => null)).pipe(
      switchMap((token) => {
        // If Keycloak doesn't have a token, check localStorage (for REST API login)
        if (!token) {
          const storedToken = localStorage.getItem('kc-access-token');
          if (storedToken) {
            token = storedToken;
          }
        }
        
        // Check if token is expired or about to expire
        if (token && this.isTokenExpired(token)) {
          console.log('Token expired or expiring soon, attempting refresh...');
          return from(this.refreshToken()).pipe(
            switchMap((newToken) => {
              if (newToken) {
                const clonedRequest = request.clone({
                  setHeaders: {
                    Authorization: `Bearer ${newToken}`
                  }
                });
                return next.handle(clonedRequest);
              } else {
                // Refresh failed, proceed without token (will likely fail, but let it fail naturally)
                console.warn('Token refresh failed, proceeding without token');
                return next.handle(request);
              }
            })
          );
        }
        
        // Clone the request and add the Authorization header
        if (token) {
          const clonedRequest = request.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`
            }
          });
          return next.handle(clonedRequest);
        }
        // If no token, proceed without Authorization header
        return next.handle(request);
      }),
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized - token might be invalid
        if (error.status === 401) {
          console.warn('Received 401, token may be invalid. Attempting refresh...');
          return from(this.refreshToken()).pipe(
            switchMap((newToken) => {
              if (newToken) {
                // Retry the request with new token
                const clonedRequest = request.clone({
                  setHeaders: {
                    Authorization: `Bearer ${newToken}`
                  }
                });
                return next.handle(clonedRequest);
              } else {
                // Refresh failed, redirect to login
                this.router.navigate(['/login']);
                return throwError(() => error);
              }
            })
          );
        }
        return throwError(() => error);
      })
    );
  }
}
