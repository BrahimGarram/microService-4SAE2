import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';
import { ThemeService } from '../../services/theme.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  title: string = 'Dashboard';
  showNotifications = false;
  showProfileMenu = false;
  showSearch = false;
  isDarkMode = false;
  notifications = [
    { id: 1, message: 'New user registered', time: '2 hours ago', read: false, type: 'user' },
    { id: 2, message: 'Project completed', time: '5 hours ago', read: false, type: 'project' },
    { id: 3, message: 'Payment received', time: '1 day ago', read: true, type: 'payment' }
  ];

  unreadCount = this.notifications.filter(n => !n.read).length;

  constructor(
    private router: Router,
    private keycloakService: KeycloakService,
    private themeService: ThemeService,
    private toastService: ToastService
  ) {}

  ngOnInit() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.updateTitle(event.url);
    });
    this.updateTitle(this.router.url);

    this.themeService.isDarkMode$.subscribe(isDark => {
      this.isDarkMode = isDark;
    });
  }

  updateTitle(url: string) {
    if (url.includes('/admin/profile')) this.title = 'Profile';
    else if (url.includes('/users')) this.title = 'Users Management';
    else if (url.includes('/projects')) this.title = 'Projects';
    else if (url.includes('/settings')) this.title = 'Settings';
    else if (url.includes('/admin/dashboard')) this.title = 'Dashboard';
    else this.title = 'Admin Panel';
  }

  toggleTheme() {
    this.themeService.toggleTheme();
    this.toastService.info(this.isDarkMode ? 'Switched to light mode' : 'Switched to dark mode');
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    this.showProfileMenu = false;
    this.showSearch = false;
  }

  toggleProfileMenu() {
    this.showProfileMenu = !this.showProfileMenu;
    this.showNotifications = false;
    this.showSearch = false;
  }

  toggleSearch() {
    this.showSearch = !this.showSearch;
    this.showNotifications = false;
    this.showProfileMenu = false;
  }

  hideSearch() {
    setTimeout(() => {
      this.showSearch = false;
    }, 200);
  }

  markAsRead(id: number) {
    const notification = this.notifications.find(n => n.id === id);
    if (notification) {
      notification.read = true;
      this.unreadCount = this.notifications.filter(n => !n.read).length;
    }
  }

  markAllAsRead() {
    this.notifications.forEach(n => n.read = true);
    this.unreadCount = 0;
    this.toastService.success('All notifications marked as read');
  }

  async logout() {
    // Clear localStorage tokens
    localStorage.removeItem('kc-access-token');
    localStorage.removeItem('kc-refresh-token');
    localStorage.removeItem('kc-id-token');
    localStorage.removeItem('kc-user-id');
    localStorage.removeItem('kc-username');
    localStorage.removeItem('kc-roles');
    
    this.toastService.info('You have been logged out');
    
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
