import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { SubscriptionService, Subscription } from '../../services/subscription.service';

@Component({
  selector: 'app-subscription-button',
  templateUrl: './subscription-button.component.html',
  styleUrls: ['./subscription-button.component.css']
})
export class SubscriptionButtonComponent implements OnInit {
  @Input() style: 'gradient' | 'pulse' | 'glow' | 'minimal' | 'premium' = 'gradient';
  @Input() size: 'small' | 'medium' | 'large' = 'medium';
  @Input() showIcon: boolean = true;
  @Input() customText?: string;
  
  currentSubscription: Subscription | null = null;
  loading = true;
  userId: number = 0;

  constructor(
    private router: Router,
    private subscriptionService: SubscriptionService
  ) {}

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.userId = user.id;
      this.loadSubscription();
    } else {
      this.loading = false;
    }
  }

  loadSubscription(): void {
    this.subscriptionService.getActiveSubscription(this.userId).subscribe({
      next: (subscription) => {
        this.currentSubscription = subscription;
        this.loading = false;
      },
      error: () => {
        this.currentSubscription = null;
        this.loading = false;
      }
    });
  }

  navigateToSubscription(): void {
    if (this.currentSubscription) {
      this.router.navigate(['/my-subscription']);
    } else {
      this.router.navigate(['/subscription-plans']);
    }
  }

  getButtonText(): string {
    if (this.customText) return this.customText;
    
    if (this.loading) return 'Loading...';
    
    if (this.currentSubscription) {
      const planName = this.currentSubscription.plan.planName;
      if (planName === 'FREE') {
        return 'Upgrade Now';
      }
      return 'My Subscription';
    }
    
    return 'Get Started';
  }

  getButtonClass(): string {
    const baseClass = `subscription-btn subscription-btn-${this.style} subscription-btn-${this.size}`;
    
    if (this.currentSubscription) {
      const planName = this.currentSubscription.plan.planName;
      if (planName === 'FREE') {
        return `${baseClass} upgrade-btn`;
      }
      return `${baseClass} active-btn`;
    }
    
    return baseClass;
  }

  getIcon(): string {
    if (this.currentSubscription) {
      const planName = this.currentSubscription.plan.planName;
      if (planName === 'FREE') {
        return 'bi-arrow-up-circle';
      }
      return 'bi-credit-card';
    }
    return 'bi-lightning-charge';
  }
}
