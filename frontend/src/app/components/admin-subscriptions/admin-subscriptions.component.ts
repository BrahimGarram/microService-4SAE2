import { Component, OnInit } from '@angular/core';
import { SubscriptionService, SubscriptionPlan, Subscription } from '../../services/subscription.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-admin-subscriptions',
  templateUrl: './admin-subscriptions.component.html',
  styleUrls: ['./admin-subscriptions.component.css']
})
export class AdminSubscriptionsComponent implements OnInit {
  activeTab: 'plans' | 'subscriptions' | 'analytics' = 'plans';
  
  // Plans
  plans: SubscriptionPlan[] = [];
  plansLoading = true;
  
  // New Plan Form
  showPlanModal = false;
  editingPlan: SubscriptionPlan | null = null;
  planForm: Partial<SubscriptionPlan> = this.getEmptyPlanForm();
  
  // Subscriptions (mock data - in production, fetch from backend)
  allSubscriptions: any[] = [];
  subscriptionsLoading = true;
  
  // Analytics
  analytics = {
    totalSubscriptions: 0,
    activeSubscriptions: 0,
    monthlyRevenue: 0,
    churnRate: 0,
    planDistribution: [] as any[]
  };

  constructor(
    public subscriptionService: SubscriptionService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadPlans();
    this.loadAnalytics();
  }

  // Plans Management
  loadPlans(): void {
    this.plansLoading = true;
    this.subscriptionService.getAllPlans().subscribe({
      next: (plans) => {
        this.plans = plans;
        this.plansLoading = false;
      },
      error: (error) => {
        console.error('Error loading plans:', error);
        this.toastService.error('Failed to load plans');
        this.plansLoading = false;
      }
    });
  }

  openPlanModal(plan?: SubscriptionPlan): void {
    if (plan) {
      this.editingPlan = plan;
      this.planForm = { ...plan };
    } else {
      this.editingPlan = null;
      this.planForm = this.getEmptyPlanForm();
    }
    this.showPlanModal = true;
  }

  closePlanModal(): void {
    this.showPlanModal = false;
    this.editingPlan = null;
    this.planForm = this.getEmptyPlanForm();
  }

  savePlan(): void {
    if (!this.validatePlanForm()) {
      this.toastService.warning('Please fill all required fields');
      return;
    }

    const planData = this.planForm as SubscriptionPlan;
    
    this.subscriptionService.createPlan(planData).subscribe({
      next: (plan) => {
        this.toastService.success(
          this.editingPlan ? 'Plan updated successfully' : 'Plan created successfully'
        );
        this.closePlanModal();
        this.loadPlans();
      },
      error: (error) => {
        console.error('Error saving plan:', error);
        this.toastService.error('Failed to save plan');
      }
    });
  }

  validatePlanForm(): boolean {
    return !!(
      this.planForm.planName &&
      this.planForm.description &&
      this.planForm.price !== undefined &&
      this.planForm.billingCycle &&
      this.planForm.maxBidsPerMonth !== undefined &&
      this.planForm.commissionRate !== undefined
    );
  }

  getEmptyPlanForm(): Partial<SubscriptionPlan> {
    return {
      planName: '',
      description: '',
      price: 0,
      billingCycle: 'MONTHLY',
      maxBidsPerMonth: 0,
      commissionRate: 0,
      hasAnalyticsAccess: false,
      hasProfileBoost: false,
      hasPrioritySupport: false,
      hasFeaturedListing: false,
      maxProjects: 0,
      isActive: true
    };
  }

  // Analytics
  loadAnalytics(): void {
    // Mock analytics data - in production, fetch from backend
    this.analytics = {
      totalSubscriptions: 1250,
      activeSubscriptions: 980,
      monthlyRevenue: 45670,
      churnRate: 5.2,
      planDistribution: [
        { name: 'FREE', count: 450, percentage: 36 },
        { name: 'BASIC', count: 380, percentage: 30.4 },
        { name: 'PRO', count: 320, percentage: 25.6 },
        { name: 'ENTERPRISE', count: 100, percentage: 8 }
      ]
    };
  }

  // Utility Methods
  getPlanBadgeClass(planName: string): string {
    return `badge bg-${this.subscriptionService.getPlanColor(planName)}`;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  formatPercentage(value: number): string {
    return `${value.toFixed(1)}%`;
  }
}
