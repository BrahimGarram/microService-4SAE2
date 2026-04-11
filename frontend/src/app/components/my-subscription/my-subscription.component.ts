import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { SubscriptionService, Subscription, FeatureAccessResponse } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-my-subscription',
  templateUrl: './my-subscription.component.html',
  styleUrls: ['./my-subscription.component.css']
})
export class MySubscriptionComponent implements OnInit {
  subscription: Subscription | null = null;
  subscriptionHistory: Subscription[] = [];
  loading = true;
  cancelling = false;
  userId: number = 0;
  
  // Usage stats
  canBid = false;
  remainingBids = 0;
  commissionRate = 0;
  hasAnalytics = false;
  hasProfileBoost = false;

  // Cancel modal
  showCancelModal = false;
  cancellationReason = '';

  constructor(
    private subscriptionService: SubscriptionService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.userId = user.id;
      this.loadSubscription();
      this.loadHistory();
    } else {
      this.router.navigate(['/login']);
    }
  }

  loadSubscription(): void {
    this.subscriptionService.getActiveSubscription(this.userId).subscribe({
      next: (subscription) => {
        this.subscription = subscription;
        this.loadUsageStats();
        this.loading = false;
      },
      error: () => {
        this.subscription = null;
        this.loading = false;
      }
    });
  }

  loadHistory(): void {
    this.subscriptionService.getUserSubscriptionHistory(this.userId).subscribe({
      next: (history) => {
        this.subscriptionHistory = history;
      },
      error: (error) => {
        console.error('Error loading history:', error);
      }
    });
  }

  loadUsageStats(): void {
    if (!this.subscription) return;

    // Check bid access
    this.subscriptionService.validateFeatureAccess(this.userId, 'BID').subscribe({
      next: (response: FeatureAccessResponse) => {
        this.canBid = response.hasAccess;
        this.remainingBids = response.remainingBids || 0;
      }
    });

    // Check commission rate
    this.subscriptionService.validateFeatureAccess(this.userId, 'COMMISSION').subscribe({
      next: (response: FeatureAccessResponse) => {
        this.commissionRate = response.commissionRate || 0;
      }
    });

    // Check analytics access
    this.subscriptionService.validateFeatureAccess(this.userId, 'ANALYTICS').subscribe({
      next: (response: FeatureAccessResponse) => {
        this.hasAnalytics = response.hasAccess;
      }
    });

    // Check profile boost
    this.subscriptionService.validateFeatureAccess(this.userId, 'PROFILE_BOOST').subscribe({
      next: (response: FeatureAccessResponse) => {
        this.hasProfileBoost = response.hasAccess;
      }
    });
  }

  getDaysRemaining(): number {
    if (!this.subscription) return 0;
    return this.subscriptionService.getDaysRemaining(this.subscription);
  }

  getProgressPercentage(): number {
    if (!this.subscription) return 0;
    const total = this.subscription.plan.maxBidsPerMonth;
    const used = this.subscription.bidsUsedThisMonth;
    return (used / total) * 100;
  }

  getProgressColor(): string {
    const percentage = this.getProgressPercentage();
    if (percentage < 50) return 'success';
    if (percentage < 80) return 'warning';
    return 'danger';
  }

  openCancelModal(): void {
    this.showCancelModal = true;
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancellationReason = '';
  }

  confirmCancellation(): void {
    if (!this.cancellationReason.trim()) {
      this.toastService.warning('Please provide a reason for cancellation');
      return;
    }

    this.cancelling = true;
    this.subscriptionService.cancelSubscription(this.userId, this.cancellationReason).subscribe({
      next: () => {
        this.toastService.success('Subscription cancelled successfully');
        this.closeCancelModal();
        this.cancelling = false;
        this.loadSubscription();
      },
      error: (error) => {
        console.error('Error cancelling subscription:', error);
        this.toastService.error('Failed to cancel subscription');
        this.cancelling = false;
      }
    });
  }

  upgradePlan(): void {
    this.router.navigate(['/subscription-plans']);
  }

  getStatusBadgeClass(status: string): string {
    return `badge bg-${this.subscriptionService.getStatusColor(status)}`;
  }

  getPlanBadgeClass(planName: string): string {
    const name = planName.toLowerCase();
    if (name === 'free') return 'free';
    if (name === 'basic') return 'basic';
    if (name === 'pro') return 'pro';
    if (name === 'enterprise') return 'enterprise';
    return 'basic';
  }

  getStatusClass(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower === 'active') return 'active';
    if (statusLower === 'cancelled') return 'cancelled';
    if (statusLower === 'expired') return 'expired';
    return 'pending';
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  isExpiringSoon(): boolean {
    return this.getDaysRemaining() <= 7 && this.getDaysRemaining() > 0;
  }

  isExpired(): boolean {
    return this.getDaysRemaining() <= 0;
  }
}
