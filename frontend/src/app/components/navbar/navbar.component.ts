import { Component, HostListener, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { SubscriptionService } from '../../services/subscription.service';
import { BadgeService } from '../../services/badge.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {
  isScrolled = false;
  isMobileMenuOpen = false;
  isAuthenticated = false;
  username = '';
  isAdmin = false;
  showServicesDropdown = false;
  showUserDropdown = false;
  unreadCount = 0;
  currentPlan: string = '';
  freelancerLevel: string | null = null;
  freelancerBadgeName: string | null = null;

  constructor(
    public router: Router,
    private keycloakService: KeycloakService,
    private subscriptionService: SubscriptionService,
    private badgeService: BadgeService
  ) {}
  
  async ngOnInit() {
    await this.checkAuthStatus();
    
    // Listen to route changes to update auth status
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(async () => {
      await this.checkAuthStatus();
    });
    
    // Listen to storage changes (for login/logout from other tabs or after login)
    window.addEventListener('storage', async () => {
      await this.checkAuthStatus();
    });
    
    // Listen to custom login event
    window.addEventListener('user-logged-in', async () => {
      console.log('Navbar: Received user-logged-in event, updating auth status');
      await this.checkAuthStatus();
    });
    
    // Periodically check auth status (every 5 seconds, stop once authenticated)
    const interval = setInterval(async () => {
      await this.checkAuthStatus();
      if (this.isAuthenticated) clearInterval(interval);
    }, 5000);
  }

  async checkAuthStatus() {
    // First check localStorage (for REST API login) - this is faster and more reliable
    const token = localStorage.getItem('kc-access-token');
    const wasAuthenticated = this.isAuthenticated;
    
    if (token) {
      this.isAuthenticated = true;
      // Get username from localStorage
      this.username = localStorage.getItem('kc-username') || '';
      // Get roles from localStorage
      const rolesStr = localStorage.getItem('kc-roles');
      if (rolesStr) {
        try {
          const roles = JSON.parse(rolesStr);
          this.isAdmin = roles.some((role: string) => role.toUpperCase() === 'ADMIN');
        } catch (e) {
          this.isAdmin = false;
        }
      }
      
      // Load current subscription
      this.loadCurrentSubscription();
      this.loadFreelancerBadge();
    } else {
      // If no token in localStorage, check KeycloakService
      try {
        this.isAuthenticated = await this.keycloakService.isLoggedIn();
        if (this.isAuthenticated) {
          try {
            const userProfile = await this.keycloakService.loadUserProfile();
            this.username = userProfile.username || this.username || '';
            const roles = await this.keycloakService.getUserRoles();
            this.isAdmin = roles.some((role: string) => role.toUpperCase() === 'ADMIN');
            this.loadCurrentSubscription();
            this.loadFreelancerBadge();
          } catch (error) {
            console.error('Error loading user profile:', error);
            // Fallback to localStorage values if KeycloakService fails
            if (!this.username) {
              this.username = localStorage.getItem('kc-username') || '';
            }
          }
        } else {
          this.isAuthenticated = false;
          this.username = '';
          this.isAdmin = false;
          this.currentPlan = '';
        }
      } catch (error) {
        // If KeycloakService fails, check localStorage as fallback
        this.isAuthenticated = !!token;
        if (!this.isAuthenticated) {
          this.username = '';
          this.isAdmin = false;
          this.currentPlan = '';
        }
      }
    }
    
    // If authentication status changed, log it
    if (wasAuthenticated !== this.isAuthenticated) {
      console.log('Navbar: Authentication status changed to:', this.isAuthenticated);
    }
  }

  loadCurrentSubscription(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        this.subscriptionService.getActiveSubscription(user.id).subscribe({
          next: (subscription) => {
            this.currentPlan = subscription.plan.planName;
          },
          error: () => {
            this.currentPlan = 'FREE';
          }
        });
      } catch (e) {
        this.currentPlan = '';
      }
    }
  }

  loadFreelancerBadge(): void {
    try {
      const token = localStorage.getItem('kc-access-token');
      if (!token) return;
      const payload = JSON.parse(atob(token.split('.')[1]));
      const email = payload.email;
      if (!email) return;

      // Only load badge for freelancers, not clients or admins
      const roles: string[] = payload.realm_access?.roles || [];
      const isFreelancer = roles.some((r: string) => r.toUpperCase() === 'FREELANCE' || r.toUpperCase() === 'FREELANCER');
      if (!isFreelancer) return;

      this.badgeService.getFreelancerProfile(email).subscribe({
        next: (profile) => {
          this.freelancerLevel = profile.currentLevel;
          this.freelancerBadgeName = profile.currentBadgeName;
        },
        error: () => {
          this.freelancerLevel = null;
          this.freelancerBadgeName = null;
        }
      });
    } catch {
      this.freelancerLevel = null;
    }
  }

  getLevelIcon(level: string | null): string {
    const map: Record<string, string> = { BRONZE: 'fas fa-medal', SILVER: 'fas fa-medal', GOLD: 'fas fa-trophy', PLATINUM: 'fas fa-crown' };
    return level ? (map[level] || 'fas fa-award') : '';
  }

  getLevelColor(level: string | null): string {
    const map: Record<string, string> = { BRONZE: '#cd7f32', SILVER: '#808080', GOLD: '#ffa500', PLATINUM: '#a9a9a9' };
    return level ? (map[level] || '#718096') : '#718096';
  }

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled = window.scrollY > 50;
  }

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  toggleServicesDropdown() {
    this.showServicesDropdown = !this.showServicesDropdown;
  }

  closeServicesDropdown() {
    this.showServicesDropdown = false;
  }

  toggleUserDropdown() {
    this.showUserDropdown = !this.showUserDropdown;
  }

  closeUserDropdown() {
    this.showUserDropdown = false;
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
    this.isMobileMenuOpen = false;
  }

  scrollToSection(sectionId: string) {
    if (this.router.url === '/' || this.router.url === '/home') {
      const element = document.getElementById(sectionId);
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    } else {
      this.router.navigate(['/']).then(() => {
        setTimeout(() => {
          const element = document.getElementById(sectionId);
          if (element) {
            element.scrollIntoView({ behavior: 'smooth', block: 'start' });
          }
        }, 100);
      });
    }
    this.isMobileMenuOpen = false;
  }

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
    this.isMobileMenuOpen = false;
  }
  
  async login() {
    await this.keycloakService.login({
      redirectUri: window.location.origin
    });
  }
}
