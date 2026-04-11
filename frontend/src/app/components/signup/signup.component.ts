import { Component, OnInit } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-signup',
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnInit {
  
  isLoading = false;
  signUpError = '';
  signUpSuccess = '';

  signUpForm = {
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'USER',
    acceptTerms: false
  };

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
        console.log('SignupComponent ngOnInit: User is admin, redirecting to /admin/dashboard');
        window.location.href = '/admin/dashboard';
      } else {
        console.log('SignupComponent ngOnInit: User is not admin, redirecting to home');
        window.location.href = '/';
      }
    }
  }

  /**
   * Handle Sign Up form submission
   * Uses backend API to register user in Keycloak
   */
  async onSignUp() {
    this.isLoading = true;
    this.signUpError = '';
    this.signUpSuccess = '';

    // Validate form
    if (!this.signUpForm.username || !this.signUpForm.email || !this.signUpForm.password) {
      this.signUpError = 'Please fill in all required fields.';
      this.isLoading = false;
      return;
    }

    if (this.signUpForm.password !== this.signUpForm.confirmPassword) {
      this.signUpError = 'Passwords do not match.';
      this.isLoading = false;
      return;
    }

    if (this.signUpForm.password.length < 8) {
      this.signUpError = 'Password must be at least 8 characters long.';
      this.isLoading = false;
      return;
    }

    if (!this.signUpForm.acceptTerms) {
      this.signUpError = 'Please accept the Terms of Service and Privacy Policy.';
      this.isLoading = false;
      return;
    }

    try {
      // Register user via backend API
      const response: any = await this.http.post(
        'http://localhost:8081/api/users/register',
        {
          username: this.signUpForm.username,
          email: this.signUpForm.email,
          password: this.signUpForm.password,
          role: this.signUpForm.role
        },
        {
          headers: new HttpHeaders({
            'Content-Type': 'application/json'
          })
        }
      ).toPromise();

      this.toastService.success('Account created successfully! Redirecting to login...');
      this.signUpSuccess = 'Account created successfully! Redirecting to login...';
      
      // Clear form
      this.signUpForm = {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'USER',
        acceptTerms: false
      };
      
      // Redirect to login page after 2 seconds
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 2000);
      
      this.isLoading = false;
    } catch (error: any) {
      console.error('Sign up error:', error);
      
      let errorMessage = 'Failed to create account. Please try again.';
      if (error.error) {
        if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.error) {
          errorMessage = error.error.error;
        } else if (typeof error.error === 'string') {
          errorMessage = error.error;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      // Check for specific error cases
      if (error.status === 409 || errorMessage.toLowerCase().includes('already exists')) {
        errorMessage = 'Username or email already exists. Please use different credentials.';
      } else if (error.status === 400) {
        errorMessage = 'Invalid registration data. Please check your input.';
      } else if (error.status === 0) {
        errorMessage = 'Cannot connect to server. Please check if the backend is running.';
      }
      
      this.signUpError = errorMessage;
      this.toastService.error(errorMessage);
      this.isLoading = false;
    }
  }
}
