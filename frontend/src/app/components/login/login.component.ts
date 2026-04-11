import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  
  isLoading = false;
  signInError = '';

  signInForm = {
    username: '',
    password: '',
    rememberMe: false
  };

  private keycloakUrl = 'http://localhost:8180';
  private realm = 'projetpidev';
  private clientId = 'freelance-client';

  constructor(
    private keycloakService: KeycloakService,
    private router: Router,
    private http: HttpClient,
    private toastService: ToastService
  ) {}
  
  async ngOnInit() {
    // If already logged in, check role and redirect accordingly
    let isLoggedIn = await this.keycloakService.isLoggedIn();
    
    // Also check localStorage for token (REST API login)
    if (!isLoggedIn) {
      const token = localStorage.getItem('kc-access-token');
      if (token) {
        isLoggedIn = true;
      }
    }
    
    if (isLoggedIn) {
      // Check roles from KeycloakService or localStorage
      let userRoles: string[] = [];
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
      const isAdmin = userRoles.some((role: string) => role.toUpperCase() === 'ADMIN');
      
      if (isAdmin) {
        console.log('LoginComponent ngOnInit: User is admin, redirecting to /admin/dashboard');
        window.location.href = '/admin/dashboard';
      } else {
        console.log('LoginComponent ngOnInit: User is not admin, redirecting to home');
        window.location.href = '/';
      }
    }
  }

  /**
   * Handle Sign In form submission
   * Uses Keycloak REST API (Direct Access Grants) for authentication
   * Authenticates in the background without showing Keycloak login page
   */
  async onSignIn() {
    this.isLoading = true;
    this.signInError = '';

    if (!this.signInForm.username || !this.signInForm.password) {
      this.signInError = 'Please fill in all fields.';
      this.isLoading = false;
      return;
    }

    try {
      // Use Keycloak REST API to authenticate (Direct Access Grants)
      const formData = new URLSearchParams();
      formData.append('grant_type', 'password');
      formData.append('client_id', this.clientId);
      formData.append('username', this.signInForm.username);
      formData.append('password', this.signInForm.password);

      const response: any = await this.http.post(
        `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/token`,
        formData.toString(),
        {
          headers: new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
          })
        }
      ).toPromise();

      if (response.access_token) {
        // Store tokens in localStorage for persistence
        localStorage.setItem('kc-access-token', response.access_token);
        if (response.refresh_token) {
          localStorage.setItem('kc-refresh-token', response.refresh_token);
        }
        if (response.id_token) {
          localStorage.setItem('kc-id-token', response.id_token);
        }

        // Decode token to get user info
        const tokenPayload = JSON.parse(atob(response.access_token.split('.')[1]));
        localStorage.setItem('kc-user-id', tokenPayload.sub);
        localStorage.setItem('kc-username', tokenPayload.preferred_username || tokenPayload.username || '');
        
        // Log token to console for Postman testing
        console.log('=== JWT TOKEN FOR POSTMAN ===');
        console.log('Full Token:', response.access_token);
        console.log('Token Payload:', tokenPayload);
        console.log('Username:', tokenPayload.preferred_username || tokenPayload.username);
        console.log('Roles:', tokenPayload.realm_access?.roles || []);
        console.log('Expires:', new Date(tokenPayload.exp * 1000).toLocaleString());
        console.log('=============================');
        console.log('');
        console.log('Copy this token to use in Postman:');
        console.log(response.access_token);
        console.log('');
        console.log('Use it in Postman as:');
        console.log('Authorization: Bearer ' + response.access_token);
        console.log('');
        
        // Store realm roles if available
        if (tokenPayload.realm_access && tokenPayload.realm_access.roles) {
          const roles = tokenPayload.realm_access.roles;
          console.log('LoginComponent: Storing roles in localStorage:', roles);
          localStorage.setItem('kc-roles', JSON.stringify(roles));
        } else {
          console.warn('LoginComponent: No realm_access.roles found in token!');
          console.log('LoginComponent: Token payload keys:', Object.keys(tokenPayload));
          console.log('LoginComponent: Full token payload:', tokenPayload);
          // Try to get roles from other possible locations
          if (tokenPayload.roles) {
            console.log('LoginComponent: Found roles in token.roles:', tokenPayload.roles);
            localStorage.setItem('kc-roles', JSON.stringify(tokenPayload.roles));
          } else if (tokenPayload.realm_access) {
            console.log('LoginComponent: realm_access exists but no roles:', tokenPayload.realm_access);
          }
        }

        // Sync user to backend database (don't wait for it to complete)
        this.syncUserToBackend().catch(err => console.error('Sync error:', err));

        // Determine redirect based on roles from token
        const roles = tokenPayload.realm_access?.roles || [];
        // Check for ADMIN role (case-insensitive to be safe)
        const isAdmin = roles.some((role: string) => role.toUpperCase() === 'ADMIN');
        
        console.log('=== LOGIN DEBUG ===');
        console.log('Full token payload:', tokenPayload);
        console.log('Realm access:', tokenPayload.realm_access);
        console.log('User roles from token:', roles);
        console.log('Is admin:', isAdmin);
        console.log('===================');
        
        // Show success message
        this.toastService.success('Login successful! Redirecting...');
        
        // Dispatch custom event to notify other components (like navbar) of login
        window.dispatchEvent(new Event('user-logged-in'));
        
        // Small delay to ensure localStorage is set and event is dispatched
        setTimeout(() => {
          // Redirect immediately based on role
          if (isAdmin) {
            console.log('LoginComponent: User is ADMIN, redirecting to /admin/dashboard');
            // Force immediate redirect to admin dashboard
            window.location.replace('/admin/dashboard');
          } else {
            console.log('LoginComponent: User is not admin, redirecting to home');
            window.location.replace('/');
          }
        }, 100);
      }
    } catch (error: any) {
      console.error('Sign in error:', error);
      if (error.status === 401 || error.status === 400) {
        this.signInError = 'Invalid username or password. Please try again.';
        this.toastService.error('Invalid credentials. Please check your username and password.');
      } else if (error.status === 0) {
        this.signInError = 'Cannot connect to authentication server. Please check if Keycloak is running.';
        this.toastService.error('Cannot connect to authentication server.');
      } else {
        this.signInError = error.error?.error_description || error.message || 'Failed to sign in. Please try again.';
        this.toastService.error(this.signInError);
      }
      this.isLoading = false;
    }
  }


  /**
   * Sync user to backend database after successful authentication
   */
  private async syncUserToBackend(): Promise<void> {
    try {
      // Get token from localStorage
      const token = localStorage.getItem('kc-access-token');
      if (!token) return;

      // Call backend sync endpoint
      await this.http.post(
        'http://localhost:8081/api/users/sync',
        {},
        {
          headers: new HttpHeaders({
            'Authorization': `Bearer ${token}`
          })
        }
      ).toPromise();
    } catch (error) {
      console.error('Error syncing user to backend:', error);
      // Non-critical error, continue
    }
  }

  /**
   * Login with Google via Keycloak
   */
  loginWithGoogle() {
    const redirectUri = encodeURIComponent(window.location.origin);
    const googleLoginUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/auth?` +
      `client_id=${this.clientId}&` +
      `redirect_uri=${redirectUri}&` +
      `response_type=code&` +
      `scope=openid profile email&` +
      `kc_idp_hint=google`;
    
    window.location.href = googleLoginUrl;
  }

  /**
   * Login with GitHub via Keycloak
   */
  loginWithGitHub() {
    const redirectUri = encodeURIComponent(window.location.origin);
    const githubLoginUrl = `${this.keycloakUrl}/realms/${this.realm}/protocol/openid-connect/auth?` +
      `client_id=${this.clientId}&` +
      `redirect_uri=${redirectUri}&` +
      `response_type=code&` +
      `scope=openid profile email&` +
      `kc_idp_hint=github`;
    
    window.location.href = githubLoginUrl;
  }

  /**
   * Login with Keycloak (SSO)
   */
  async loginWithKeycloak() {
    try {
      await this.keycloakService.login({
        redirectUri: window.location.origin
      });
    } catch (error: any) {
      console.error('Keycloak login error:', error);
      this.signInError = 'Failed to authenticate. Please try again.';
      this.toastService.error(this.signInError);
    }
  }
}
