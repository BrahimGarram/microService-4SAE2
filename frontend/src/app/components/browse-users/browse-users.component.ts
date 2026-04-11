import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService, User } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-browse-users',
  templateUrl: './browse-users.component.html',
  styleUrls: ['./browse-users.component.css']
})
export class BrowseUsersComponent implements OnInit {
  
  allUsers: User[] = [];
  filteredUsers: User[] = [];
  loading = false;
  errorMessage: string | null = null;
  
  // Search filters
  searchTerm: string = '';
  selectedRole: string = 'ALL';
  selectedAvailability: string = 'ALL';
  
  // Role options
  roleOptions = [
    { value: 'ALL', label: 'All Roles' },
    { value: 'USER', label: 'User' },
    { value: 'FREELANCER', label: 'Freelancer' },
    { value: 'ADMIN', label: 'Admin' }
  ];
  
  // Availability options
  availabilityOptions = [
    { value: 'ALL', label: 'All Status' },
    { value: 'ONLINE', label: 'Online' },
    { value: 'BUSY', label: 'Busy' },
    { value: 'OFFLINE', label: 'Offline' }
  ];
  
  constructor(
    private userService: UserService,
    private router: Router,
    private toastService: ToastService
  ) {}
  
  ngOnInit() {
    this.loadUsers();
  }
  
  /**
   * Load public users (filtered on frontend)
   * Uses the public endpoint that returns only public information
   */
  loadUsers() {
    this.loading = true;
    this.errorMessage = null;
    
    this.userService.getPublicUsers().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.errorMessage = 'Failed to load users. Please try again.';
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
  
  /**
   * Apply search and filter criteria
   */
  applyFilters() {
    this.filteredUsers = this.allUsers.filter(user => {
      // Search term filter (username, email, bio, skills)
      if (this.searchTerm) {
        const searchLower = this.searchTerm.toLowerCase();
        const matchesSearch = 
          user.username?.toLowerCase().includes(searchLower) ||
          user.email?.toLowerCase().includes(searchLower) ||
          user.bio?.toLowerCase().includes(searchLower) ||
          user.skills?.some(skill => skill.name?.toLowerCase().includes(searchLower)) ||
          user.city?.toLowerCase().includes(searchLower);
        
        if (!matchesSearch) return false;
      }
      
      // Role filter
      if (this.selectedRole !== 'ALL' && user.role !== this.selectedRole) {
        return false;
      }
      
      // Availability filter
      if (this.selectedAvailability !== 'ALL' && user.availability !== this.selectedAvailability) {
        return false;
      }
      
      return true;
    });
  }
  
  /**
   * On search input change
   */
  onSearchChange() {
    this.applyFilters();
  }
  
  /**
   * On role filter change
   */
  onRoleChange() {
    this.applyFilters();
  }
  
  /**
   * On availability filter change
   */
  onAvailabilityChange() {
    this.applyFilters();
  }
  
  /**
   * Clear all filters
   */
  clearFilters() {
    this.searchTerm = '';
    this.selectedRole = 'ALL';
    this.selectedAvailability = 'ALL';
    this.applyFilters();
  }
  
  /**
   * Navigate to user profile
   */
  viewProfile(userId: number) {
    this.router.navigate(['/user', userId]);
  }
  
  /**
   * Get availability label
   */
  getAvailabilityLabel(value: string | undefined): string {
    const option = this.availabilityOptions.find(opt => opt.value === value);
    return option ? option.label : value || 'Offline';
  }
  
  /**
   * Get availability color
   */
  getAvailabilityColor(value: string | undefined): string {
    switch (value) {
      case 'ONLINE': return '#48bb78';
      case 'BUSY': return '#ed8936';
      case 'OFFLINE': return '#a0aec0';
      default: return '#a0aec0';
    }
  }
}
