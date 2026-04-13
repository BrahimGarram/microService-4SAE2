import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd, NavigationStart } from '@angular/router';
import { filter } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';
import { AccessibilityService } from './services/accessibility.service';
import { environment } from '../environments/environment';
import { CollaborationApiService, CollaborationPublicDto } from './services/collaboration-api.service';
import { isCollaborateurOnlyAccount } from './utils/role-utils';
import { rewriteLegacyGatewayUrl } from './utils/gateway-url';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Freelance Platform';
  isLoggedIn = false;
  username = '';
  isAdmin = false;
  isAdminRoute = false;
  showFooter = true;
  isAuthPage = false; // Pages sans layout (login, inscription, mot de passe)
  /** Publicités collaboration : uniquement sur le site « interne », pas login ni admin */
  showCollaborationAds = false;

  /** Publicités (priorité haute / moyenne) pour invités et utilisateurs. */
  highPopupAd: CollaborationPublicDto | null = null;
  showHighAdModal = false;
  mediumSideAds: CollaborationPublicDto[] = [];
  dismissedMediumIds = new Set<number>();

  constructor(
    private keycloakService: KeycloakService,
    private router: Router,
    private accessibility: AccessibilityService,
    private collaborationApi: CollaborationApiService
  ) {}
  
  async ngOnInit() {
    // Handle OAuth callback from social providers (Google/GitHub)
    this.handleOAuthCallback();
    
    // Check KeycloakService first
    this.isLoggedIn = await this.keycloakService.isLoggedIn();
    
    // Also check localStorage for token (REST API login)
    if (!this.isLoggedIn) {
      const token = localStorage.getItem('kc-access-token');
      if (token) {
        this.isLoggedIn = true;
      }
    }
    
    if (this.isLoggedIn) {
      await this.loadUsername();
      await this.checkAdminRole();
      this.logTokenToConsole();
    }
    
    // Check route to determine layout
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.updateLayout(event.url);
    });
    this.updateLayout(this.router.url);

    this.router.events.pipe(filter((e) => e instanceof NavigationStart)).subscribe((e) => {
      if (!(e instanceof NavigationStart) || !isCollaborateurOnlyAccount()) {
        return;
      }
      const url = e.url.split('?')[0];
      if (this.isCollaborateurAllowedPath(url)) {
        return;
      }
      this.router.navigate(['/collaboration'], { queryParams: { restricted: '1' }, replaceUrl: true });
    });

    this.loadCollaborationOverlays();
  }

  private isCollaborateurAllowedPath(url: string): boolean {
    if (url === '/' || url === '/home' || url === '/login' || url === '/signup' || url === '/forgot-password') {
      return true;
    }
    if (url.startsWith('/reset-password')) {
      return true;
    }
    return url === '/collaboration' || url.startsWith('/collaboration');
  }

  private loadCollaborationOverlays(): void {
    this.collaborationApi.getPublic().subscribe({
      next: (g) => {
        const highs = g.high || [];
        if (highs.length > 0) {
          const first = highs[0];
          const key = 'collab_high_dismissed_' + first.id;
          if (!sessionStorage.getItem(key)) {
            this.highPopupAd = first;
            this.showHighAdModal = true;
          }
        }
        this.mediumSideAds = g.medium || [];
      },
      error: () => {
        /* service optionnel si gateway / Symfony non démarrés */
      }
    });
  }

  dismissHighAd(): void {
    if (this.highPopupAd) {
      sessionStorage.setItem('collab_high_dismissed_' + this.highPopupAd.id, '1');
    }
    this.showHighAdModal = false;
    this.highPopupAd = null;
  }

  dismissMediumAd(id: number): void {
    this.dismissedMediumIds.add(id);
  }

  mediumAdsVisible(): CollaborationPublicDto[] {
    return this.mediumSideAds.filter((a) => !this.dismissedMediumIds.has(a.id));
  }

  collabMediaFull(relativePath: string): string {
    return this.collaborationApi.absoluteMediaUrl(relativePath);
  }

  isCollaborateurOnly(): boolean {
    return isCollaborateurOnlyAccount();
  }
  
  /**
   * Handle OAuth callback from social providers
   * When user returns from Google/GitHub authentication, Keycloak redirects here
   * Also handles first-broker-login redirects when account already exists
   */
  private async handleOAuthCallback() {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const error = urlParams.get('error');
    const errorDescription = urlParams.get('error_description');
    
    // Check if this is a first-broker-login redirect (account already exists scenario)
    const isFirstBrokerLogin = window.location.pathname.includes('first-broker-login') || 
                               window.location.search.includes('first-broker-login');
    
    // If user is already logged in and we're on first-broker-login page, redirect to home
    if (isFirstBrokerLogin) {
      try {
        const isLoggedIn = await this.keycloakService.isLoggedIn();
        if (isLoggedIn) {
          console.log('User already logged in on first-broker-login page, redirecting to home');
          const token = await this.keycloakService.getToken();
          if (token) {
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));
            const roles = tokenPayload.realm_access?.roles || [];
            const isAdmin = roles.some((role: string) => role.toUpperCase() === 'ADMIN');
            const isCollabOnly =
              !isAdmin &&
              roles.some((role: string) => role.toUpperCase() === 'COLLABORATEUR');

            // Clean URL and redirect
            window.history.replaceState({}, document.title, '/');
            if (isAdmin) {
              window.location.href = '/admin/dashboard';
            } else if (isCollabOnly) {
              window.location.href = '/collaboration';
            } else {
              window.location.href = '/';
            }
            return;
          }
        }
      } catch (error) {
        console.error('Error checking login status on first-broker-login:', error);
      }
    }
    
    if (code) {
      // User is returning from OAuth provider
      try {
        // KeycloakService should handle this automatically, but we ensure tokens are stored
        const isLoggedIn = await this.keycloakService.isLoggedIn();
        if (isLoggedIn) {
          // Get tokens and store them
          const token = await this.keycloakService.getToken();
          if (token) {
            localStorage.setItem('kc-access-token', token);
            
            // Decode token to get user info
            try {
              const tokenPayload = JSON.parse(atob(token.split('.')[1]));
              localStorage.setItem('kc-user-id', tokenPayload.sub);
              localStorage.setItem('kc-username', tokenPayload.preferred_username || tokenPayload.username || '');
              
              if (tokenPayload.realm_access && tokenPayload.realm_access.roles) {
                localStorage.setItem('kc-roles', JSON.stringify(tokenPayload.realm_access.roles));
              }
              
              // Sync user to backend
              this.syncUserToBackend(token);
              
              // Redirect based on role
              const roles = tokenPayload.realm_access?.roles || [];
              const isAdmin = roles.some((role: string) => role.toUpperCase() === 'ADMIN');
              const isCollabOnly =
                !isAdmin &&
                roles.some((role: string) => role.toUpperCase() === 'COLLABORATEUR');

              // Clean URL (remove code parameter)
              window.history.replaceState({}, document.title, window.location.pathname);

              // Redirect
              if (isAdmin) {
                window.location.href = '/admin/dashboard';
              } else if (isCollabOnly) {
                window.location.href = '/collaboration';
              } else {
                window.location.href = '/';
              }
            } catch (e) {
              console.error('Error processing token:', e);
            }
          }
        }
      } catch (error) {
        console.error('Error handling OAuth callback:', error);
      }
    } else if (error) {
      // Handle OAuth errors
      console.error('OAuth error:', error, errorDescription);
      // If account already exists error, try to redirect to home if user is logged in
      if (error === 'account_already_exists' || errorDescription?.includes('already exists')) {
        try {
          const isLoggedIn = await this.keycloakService.isLoggedIn();
          if (isLoggedIn) {
            console.log('Account already exists but user is logged in, redirecting to home');
            window.location.href = '/';
            return;
          }
        } catch (e) {
          console.error('Error checking login status:', e);
        }
      }
    }
  }
  
  /**
   * Sync user to backend after social login
   */
  private async syncUserToBackend(token: string) {
    try {
      const response = await fetch(rewriteLegacyGatewayUrl(`${environment.apiGatewayUrl}/user/sync`), {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      if (response.ok) {
        console.log('User synced to backend after social login');
      }
    } catch (error) {
      console.error('Error syncing user to backend:', error);
    }
  }
  
  /**
   * Log JWT token to browser console (for Postman / debugging).
   * Open DevTools (F12) → Console to see it.
   */
  private async logTokenToConsole(): Promise<void> {
    try {
      const token = await this.keycloakService.getToken().catch(() => null) || localStorage.getItem('kc-access-token');
      if (token) {
        console.log('%c[JWT Token for Postman]', 'color: #FF6B35; font-weight: bold; font-size: 12px;');
        console.log(token);
        console.log('%cCopy the line above and use it as Bearer token in Postman.', 'color: #718096;');
      }
    } catch {
      const token = localStorage.getItem('kc-access-token');
      if (token) {
        console.log('%c[JWT Token for Postman]', 'color: #FF6B35; font-weight: bold; font-size: 12px;');
        console.log(token);
        console.log('%cCopy the line above and use it as Bearer token in Postman.', 'color: #718096;');
      }
    }
  }

  updateLayout(url: string) {
    // Pages auth — pas de navbar / pas de publicités collaboration
    this.isAuthPage =
      url === '/login' ||
      url === '/signup' ||
      url === '/forgot-password' ||
      url.startsWith('/reset-password');
    
    // Admin routes use sidebar/header layout
    // Only /admin/projects is admin route, not /projects. Add question is part of admin test.
    this.isAdminRoute = url.startsWith('/admin') || 
                        url.startsWith('/users') || 
                        url.startsWith('/addquestion') ||
                        (url.includes('/dashboard') && url.startsWith('/admin')) || 
                        (url.includes('/settings') && url.startsWith('/admin')) ||
                        url.startsWith('/admin/collaborations');
    
    // Don't show footer on admin routes or auth pages
    this.showFooter = !this.isAdminRoute && !this.isAuthPage;

    this.showCollaborationAds = !this.isAuthPage && !this.isAdminRoute;
  }
  
  /**
   * Check if user has ADMIN role
   */
  async checkAdminRole() {
    try {
      let userRoles: string[] = [];
      
      // Try to get roles from KeycloakService
      try {
        userRoles = await this.keycloakService.getUserRoles();
      } catch (error) {
        // If KeycloakService fails, get roles from localStorage
        const rolesStr = localStorage.getItem('kc-roles');
        if (rolesStr) {
          userRoles = JSON.parse(rolesStr);
        }
      }
      
      // Check for ADMIN role (case-insensitive)
      this.isAdmin = userRoles.some((role: string) => role.toUpperCase() === 'ADMIN');
    } catch (error) {
      console.error('Error checking admin role:', error);
      this.isAdmin = false;
    }
  }
  
  /**
   * Check if user is logged in
   */
  async checkLoginStatus() {
    this.isLoggedIn = await this.keycloakService.isLoggedIn();
    if (this.isLoggedIn) {
      await this.loadUsername();
    }
  }
  
  /**
   * Load username from Keycloak
   */
  async loadUsername() {
    try {
      const userProfile = await this.keycloakService.loadUserProfile();
      this.username = userProfile.username || '';
    } catch (error) {
      this.username = '';
    }
  }
  
  /**
   * Logout user
   */
  async logout() {
    // Clear localStorage tokens
    localStorage.removeItem('kc-access-token');
    localStorage.removeItem('kc-refresh-token');
    localStorage.removeItem('kc-id-token');
    localStorage.removeItem('kc-user-id');
    localStorage.removeItem('kc-username');
    localStorage.removeItem('kc-roles');
    
    // Logout from Keycloak
    try {
      await this.keycloakService.logout();
    } catch (error) {
      console.error('Error during Keycloak logout:', error);
    }
    
    // Redirect to login page
    this.router.navigate(['/login']);
  }
}
