import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit {
  
  email: string = '';
  isLoading: boolean = false;
  isSubmitted: boolean = false;
  error: string | null = null;
  
  constructor(
    private userService: UserService,
    private router: Router,
    private toastService: ToastService
  ) {}
  
  ngOnInit(): void {}
  
  /**
   * Submit forgot password request
   */
  onSubmit(): void {
    if (!this.email || !this.email.trim()) {
      this.error = 'Please enter your email address';
      return;
    }
    
    // Basic email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.error = 'Please enter a valid email address';
      return;
    }
    
    this.isLoading = true;
    this.error = null;
    
    this.userService.forgotPassword(this.email.trim()).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.isSubmitted = true;
        this.toastService.show('Password reset email sent! Please check your inbox.', 'success');
      },
      error: (err) => {
        this.isLoading = false;
        // Always show success message for security (even if user doesn't exist)
        this.isSubmitted = true;
        this.toastService.show('If an account with that email exists, a password reset link has been sent.', 'info');
        console.error('Error sending password reset email:', err);
      }
    });
  }
  
  /**
   * Navigate back to login
   */
  backToLogin(): void {
    this.router.navigate(['/login']);
  }
}
