import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  
  token: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  isLoading: boolean = false;
  error: string | null = null;
  success: boolean = false;
  
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private toastService: ToastService
  ) {}
  
  ngOnInit(): void {
    // Get token from query parameter
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if (!this.token) {
        this.error = 'Invalid reset link. Please request a new password reset.';
      }
    });
  }
  
  /**
   * Submit password reset
   */
  onSubmit(): void {
    // Validation
    if (!this.token) {
      this.error = 'Invalid reset link. Please request a new password reset.';
      return;
    }
    
    if (!this.newPassword || this.newPassword.length < 8) {
      this.error = 'Password must be at least 8 characters long';
      return;
    }
    
    if (this.newPassword !== this.confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }
    
    this.isLoading = true;
    this.error = null;
    
    this.userService.resetPassword(this.token, this.newPassword).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.success = true;
        this.toastService.show('Password reset successfully! You can now login with your new password.', 'success');
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (err) => {
        this.isLoading = false;
        const errorMessage = err.error && err.error.error 
          ? err.error.error 
          : 'Failed to reset password. The link may have expired. Please request a new password reset.';
        this.error = errorMessage;
        this.toastService.show(errorMessage, 'error');
        console.error('Error resetting password:', err);
      }
    });
  }
  
  /**
   * Navigate to login
   */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }
  
  /**
   * Navigate to forgot password
   */
  goToForgotPassword(): void {
    this.router.navigate(['/forgot-password']);
  }
}
