import { Component, OnInit } from '@angular/core';
import { UserService, User, UpdateUserRequest } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-admin-profile',
  templateUrl: './admin-profile.component.html',
  styleUrls: ['./admin-profile.component.css']
})
export class AdminProfileComponent implements OnInit {
  
  user: User | null = null;
  updateRequest: UpdateUserRequest = {};
  loading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  isEditMode = false;
  
  constructor(
    private userService: UserService,
    private toastService: ToastService
  ) {}
  
  ngOnInit() {
    // Always start in view mode (not edit mode)
    this.isEditMode = false;
    this.errorMessage = null;
    this.successMessage = null;
    this.loadUser();
  }
  
  /**
   * Load current user profile
   */
  loadUser() {
    this.loading = true;
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.updateRequest = {
          username: user.username,
          email: user.email
        };
        this.loading = false;
        this.errorMessage = null;
      },
      error: (err) => {
        console.error('Error loading user:', err);
        this.errorMessage = 'Failed to load user profile. Please try again.';
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
  
  /**
   * Toggle edit mode
   */
  toggleEditMode() {
    if (this.isEditMode) {
      this.cancelEdit();
    } else {
      this.isEditMode = true;
      // Reset form to current user values
      if (this.user) {
        this.updateRequest = {
          username: this.user.username,
          email: this.user.email
        };
      }
    }
  }
  
  /**
   * Cancel edit mode
   */
  cancelEdit() {
    this.isEditMode = false;
    this.errorMessage = null;
    this.successMessage = null;
    // Reset form to current user values
    if (this.user) {
      this.updateRequest = {
        username: this.user.username,
        email: this.user.email
      };
    }
  }
  
  /**
   * Update user profile
   */
  updateProfile() {
    if (!this.user) return;
    
    // Validate form
    if (!this.updateRequest.username || !this.updateRequest.email) {
      this.errorMessage = 'Please fill in all required fields.';
      this.toastService.error(this.errorMessage);
      return;
    }
    
    this.loading = true;
    this.successMessage = null;
    this.errorMessage = null;
    
    this.userService.updateUser(this.user.id, this.updateRequest).subscribe({
      next: (updatedUser) => {
        this.user = updatedUser;
        this.successMessage = 'Profile updated successfully!';
        this.toastService.success(this.successMessage);
        this.isEditMode = false;
        this.loading = false;
        
        // Clear success message after 3 seconds
        setTimeout(() => {
          this.successMessage = null;
        }, 3000);
      },
      error: (err) => {
        console.error('Error updating user:', err);
        let errorMsg = 'Failed to update profile. Please try again.';
        
        if (err.error?.message) {
          errorMsg = err.error.message;
        } else if (err.status === 403) {
          errorMsg = 'You do not have permission to update this profile.';
        } else if (err.status === 404) {
          errorMsg = 'User profile not found.';
        }
        
        this.errorMessage = errorMsg;
        this.toastService.error(this.errorMessage);
        this.loading = false;
      }
    });
  }
}
