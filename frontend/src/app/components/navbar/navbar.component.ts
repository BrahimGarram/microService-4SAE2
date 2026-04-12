import { Component, HostListener, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { filter } from 'rxjs/operators';
import { forkJoin, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { AccessibilityService, AppLanguage, FontSize } from '../../services/accessibility.service';
import { NotificationService, Notification } from '../../services/notification.service';
import { UserService } from '../../services/user.service';
import { ProjectService, Project } from '../../services/project.service';
import { ProposalService } from '../../services/proposal.service';
import { isCollaborateurOnlyAccount } from '../../utils/role-utils';

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
  isCollaborateurOnlyUser = false;
  showServicesDropdown = false;
  showDiscoverDropdown = false;
  showProjectsDropdown = false;
  showProfileDropdown = false;
  showAccessibilityDropdown = false;
  showNotificationsDropdown = false;
  notifications: Notification[] = [];
  unreadCount = 0;
  loadingNotifications = false;
  liveProjects: Project[] = [];
  loadingLiveProjects = false;

  constructor(
    private router: Router,
    private keycloakService: KeycloakService,
    public accessibility: AccessibilityService,
    private notificationService: NotificationService,
    private userService: UserService,
    private projectService: ProjectService,
    private proposalService: ProposalService
  ) {}
  
  async ngOnInit() {
    await this.checkAuthStatus();
    if (this.isAuthenticated) this.loadNotifications();

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
    
    // Periodically check auth status (every 1 second) to catch login changes
    setInterval(async () => {
      await this.checkAuthStatus();
    }, 1000);
    // Refresh notification badge every 30s so new notifications appear without reopening dropdown
    setInterval(() => {
      if (this.isAuthenticated) this.refreshUnreadCount();
    }, 30000);
  }

  refreshUnreadCount() {
    if (!this.isAuthenticated) return;
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => { this.unreadCount = res?.count ?? 0; },
      error: () => { this.unreadCount = 0; }
    });
  }

  loadNotifications() {
    if (!this.isAuthenticated) return;
    this.loadingNotifications = true;
    this.notificationService.getMyNotifications().subscribe({
      next: (list) => {
        this.notifications = list || [];
        this.loadingNotifications = false;
      },
      error: () => { this.loadingNotifications = false; }
    });
    this.refreshUnreadCount();
  }

  toggleNotificationsDropdown() {
    this.showNotificationsDropdown = !this.showNotificationsDropdown;
    if (this.showNotificationsDropdown) this.loadNotifications();
    this.showAccessibilityDropdown = false;
    this.showServicesDropdown = false;
    this.showDiscoverDropdown = false;
    this.showProjectsDropdown = false;
    this.showProfileDropdown = false;
  }

  toggleDiscoverDropdown() {
    this.showDiscoverDropdown = !this.showDiscoverDropdown;
    this.showServicesDropdown = false;
    this.showProjectsDropdown = false;
    this.showProfileDropdown = false;
  }

  closeDiscoverDropdown() {
    this.showDiscoverDropdown = false;
  }

  toggleProfileDropdown() {
    this.showProfileDropdown = !this.showProfileDropdown;
    this.showServicesDropdown = false;
    this.showDiscoverDropdown = false;
    this.showProjectsDropdown = false;
  }

  closeProfileDropdown() {
    this.showProfileDropdown = false;
  }

  toggleProjectsDropdown() {
    this.showProjectsDropdown = !this.showProjectsDropdown;
    if (this.showProjectsDropdown) this.loadLiveProjects();
    this.showAccessibilityDropdown = false;
    this.showServicesDropdown = false;
    this.showDiscoverDropdown = false;
    this.showProfileDropdown = false;
  }

  closeProjectsDropdown() {
    this.showProjectsDropdown = false;
  }

  /** Load projects where current user is client or assigned freelancer (IN_PROGRESS / COMPLETED). */
  loadLiveProjects() {
    if (!this.isAuthenticated || this.loadingLiveProjects) return;
    this.loadingLiveProjects = true;
    this.liveProjects = [];
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        const userId = user.id!;
        const ownerProjects$ = this.projectService.getProjectsByOwner(userId).pipe(
          map(projects => (projects || []).filter(p => p.status === 'IN_PROGRESS' || p.status === 'COMPLETED'))
        );
        const freelancerProjects$ = this.proposalService.getMyProposals(userId).pipe(
          switchMap(proposals => {
            const accepted = (proposals || []).filter(p => p.status === 'ACCEPTED');
            if (accepted.length === 0) return of([]);
            const uniqueIds = [...new Set(accepted.map(a => a.projectId))];
            return forkJoin(uniqueIds.map(id => this.projectService.getProjectById(id))).pipe(
              map(projects => projects.filter(p => p && (p.status === 'IN_PROGRESS' || p.status === 'COMPLETED')))
            );
          })
        );
        forkJoin({ owner: ownerProjects$, freelancer: freelancerProjects$ }).subscribe({
          next: ({ owner, freelancer }) => {
            const seen = new Set<number>();
            const combined: Project[] = [];
            [...owner, ...freelancer].forEach(p => {
              const id = p.id!;
              if (id && !seen.has(id)) {
                seen.add(id);
                combined.push(p);
              }
            });
            this.liveProjects = combined;
            this.loadingLiveProjects = false;
          },
          error: () => { this.loadingLiveProjects = false; }
        });
      },
      error: () => { this.loadingLiveProjects = false; }
    });
  }

  goToProjectRoom(projectId: number) {
    this.closeProjectsDropdown();
    this.router.navigate(['/projects', projectId, 'dashboard']);
    this.isMobileMenuOpen = false;
  }

  closeNotificationsDropdown() {
    this.showNotificationsDropdown = false;
  }

  markAsReadAndGo(n: Notification) {
    if (!n.read) {
      this.notificationService.markAsRead(n.id).subscribe({
        next: () => { n.read = true; this.unreadCount = Math.max(0, this.unreadCount - 1); }
      });
    }
    this.showNotificationsDropdown = false;
    this.router.navigate(['/projects']);
  }

  getNotificationTitle(type: string): string {
    if (type === 'NEW_PROPOSAL') return 'Nouvelle proposition';
    if (type === 'PROPOSAL_ACCEPTED') return 'Proposition acceptée';
    if (type === 'PROPOSAL_REJECTED') return 'Proposition refusée';
    return type;
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
      this.isCollaborateurOnlyUser = isCollaborateurOnlyAccount();
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
            this.isCollaborateurOnlyUser = isCollaborateurOnlyAccount();
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
          this.isCollaborateurOnlyUser = false;
        }
      } catch (error) {
        // If KeycloakService fails, check localStorage as fallback
        this.isAuthenticated = !!token;
        if (!this.isAuthenticated) {
          this.username = '';
          this.isAdmin = false;
          this.isCollaborateurOnlyUser = false;
        }
      }
    }
    
    // If authentication status changed, log it
    if (wasAuthenticated !== this.isAuthenticated) {
      console.log('Navbar: Authentication status changed to:', this.isAuthenticated);
      if (this.isAuthenticated) this.loadNotifications();
    }
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
    this.showAccessibilityDropdown = false;
    this.showDiscoverDropdown = false;
    this.showProjectsDropdown = false;
    this.showProfileDropdown = false;
  }

  closeServicesDropdown() {
    this.showServicesDropdown = false;
  }

  toggleAccessibilityDropdown() {
    this.showAccessibilityDropdown = !this.showAccessibilityDropdown;
    this.showServicesDropdown = false;
    this.showDiscoverDropdown = false;
    this.showProjectsDropdown = false;
    this.showProfileDropdown = false;
  }

  closeAccessibilityDropdown() {
    this.showAccessibilityDropdown = false;
  }

  setLang(lang: AppLanguage) {
    this.accessibility.setLanguage(lang);
    this.closeAccessibilityDropdown();
  }

  setFontSize(size: FontSize) {
    this.accessibility.setFontSize(size);
  }

  toggleHighContrast() {
    this.accessibility.setHighContrast(!this.accessibility.isHighContrast);
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
