import { Component, OnInit } from '@angular/core';
import { UserService, User } from '../../services/user.service';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  
  users: User[] = [];
  loading = false;
  error: string | null = null;
  isAdmin = false;

  /** Recherche texte (username, email, id) */
  searchTerm = '';
  /** Filtre par rôle */
  roleFilter: '' | 'USER' | 'FREELANCER' | 'ADMIN' = '';

  readonly roleOptions: { value: '' | 'USER' | 'FREELANCER' | 'ADMIN'; label: string }[] = [
    { value: '', label: 'Tous les rôles' },
    { value: 'USER', label: 'Utilisateur' },
    { value: 'FREELANCER', label: 'Freelancer' },
    { value: 'ADMIN', label: 'Admin' }
  ];

  constructor(
    private userService: UserService,
    private keycloakService: KeycloakService
  ) {}
  
  async ngOnInit() {
    // Check if user is admin
    this.isAdmin = await this.checkAdminRole();
    
    if (this.isAdmin) {
      this.loadUsers();
    } else {
      this.error = 'Access denied. Admin role required.';
    }
  }
  
  /**
   * Check if current user has ADMIN role
   */
  async checkAdminRole(): Promise<boolean> {
    try {
      let userRoles: string[] = [];
      
      // First, try to decode token directly from localStorage (most reliable)
      const token = localStorage.getItem('kc-access-token');
      if (token) {
        try {
          // Decode JWT token to get roles
          const tokenPayload = JSON.parse(atob(token.split('.')[1]));
          console.log('UsersComponent: Token payload:', tokenPayload);
          
          if (tokenPayload.realm_access && tokenPayload.realm_access.roles) {
            userRoles = tokenPayload.realm_access.roles;
            console.log('UsersComponent: Roles from token:', userRoles);
          }
        } catch (tokenError) {
          console.error('UsersComponent: Error decoding token:', tokenError);
        }
      }
      
      // If no roles from token, try KeycloakService
      if (userRoles.length === 0) {
        try {
          userRoles = await this.keycloakService.getUserRoles();
          console.log('UsersComponent: Roles from KeycloakService:', userRoles);
        } catch (error) {
          console.error('UsersComponent: Error getting roles from KeycloakService:', error);
        }
      }
      
      // If still no roles, try localStorage
      if (userRoles.length === 0) {
        const rolesStr = localStorage.getItem('kc-roles');
        if (rolesStr) {
          try {
            userRoles = JSON.parse(rolesStr);
            console.log('UsersComponent: Roles from localStorage:', userRoles);
          } catch (parseError) {
            console.error('UsersComponent: Error parsing roles from localStorage:', parseError);
          }
        }
      }
      
      console.log('UsersComponent: Final user roles:', userRoles);
      
      // Check for ADMIN role (case-insensitive)
      const isAdmin = Array.isArray(userRoles) && userRoles.some((role: string) => {
        const roleUpper = String(role).toUpperCase();
        return roleUpper === 'ADMIN';
      });
      
      console.log('UsersComponent: Is admin:', isAdmin);
      return isAdmin;
    } catch (error) {
      console.error('UsersComponent: Error checking admin role:', error);
      return false;
    }
  }
  
  /**
   * Load all users from backend
   */
  loadUsers() {
    this.loading = true;
    this.error = null;
    
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.error = 'Failed to load users. Please try again.';
        this.loading = false;
      }
    });
  }
  
  /**
   * Liste des utilisateurs filtrés (recherche + rôle)
   */
  get filteredUsers(): User[] {
    let list = this.users;
    const term = (this.searchTerm || '').trim().toLowerCase();
    if (term) {
      list = list.filter(u => {
        const username = (u.username || '').toLowerCase();
        const email = (u.email || '').toLowerCase();
        const id = String(u.id || '');
        return username.includes(term) || email.includes(term) || id.includes(term);
      });
    }
    if (this.roleFilter) {
      list = list.filter(u => u.role === this.roleFilter);
    }
    return list;
  }

  /**
   * Delete a user
   */
  deleteUser(userId: number, username: string) {
    if (confirm(`Are you sure you want to delete user "${username}"?`)) {
      this.userService.deleteUser(userId).subscribe({
        next: () => {
          // Remove user from list
          this.users = this.users.filter(u => u.id !== userId);
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          alert('Failed to delete user. Please try again.');
        }
      });
    }
  }
}
