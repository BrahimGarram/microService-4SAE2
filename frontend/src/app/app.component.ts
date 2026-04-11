import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';

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
  isAuthPage = false; // For login/signup pages
  
  constructor(
    private keycloakService: KeycloakService,
    private router: Router
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
    }
    
    // Check route to determine layout
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.updateLayout(event.url);
    });
    this.updateLayout(this.router.url);
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
            
            // Clean URL and redirect
            window.history.replaceState({}, document.title, '/');
            if (isAdmin) {
              window.location.href = '/admin/dashboard';
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
              
              // Clean URL (remove code parameter)
              window.history.replaceState({}, document.title, window.location.pathname);
              
              // Redirect
              if (isAdmin) {
                window.location.href = '/admin/dashboard';
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
      const response = await fetch('http://localhost:8081/api/users/sync', {
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
  
  updateLayout(url: string) {
    // Auth pages (login/signup) - no navbar, no footer
    this.isAuthPage = url === '/login' || url === '/signup';
    
    // Admin routes use sidebar/header layout
    // Only /admin/* routes are admin routes, not regular user routes like /projects or /complaints
    this.isAdminRoute = url.startsWith('/admin') || 
                        url.startsWith('/users');
    
    // Don't show footer on admin routes or auth pages
    this.showFooter = !this.isAdminRoute && !this.isAuthPage;
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
