import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { UserService, User } from '../../services/user.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  
  isLoggedIn = false;
  currentUser: User | null = null;
  error: string | null = null;
  
  constructor(
    private keycloakService: KeycloakService,
    private userService: UserService,
    private router: Router
  ) {}
  
  async ngOnInit() {
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
      // Check if user is admin and redirect to dashboard
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
        // Redirect admin to dashboard immediately
        console.log('HomeComponent: User is admin, redirecting to /admin/dashboard');
        window.location.href = '/admin/dashboard';
        return;
      }
      // Don't load user profile on home page - keep it simple
      // User can access profile via navbar or /profile route
    }
  }
}
