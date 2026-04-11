import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface SubscriptionPlan {
  id: number;
  planName: string;
  description: string;
  price: number;
  billingCycle: 'MONTHLY' | 'QUARTERLY' | 'YEARLY';
  maxBidsPerMonth: number;
  commissionRate: number;
  hasAnalyticsAccess: boolean;
  hasProfileBoost: boolean;
  hasPrioritySupport: boolean;
  hasFeaturedListing: boolean;
  maxProjects: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Subscription {
  id: number;
  userId: number;
  plan: SubscriptionPlan;
  status: 'PENDING_PAYMENT' | 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'SUSPENDED' | 'PAYMENT_FAILED';
  startDate: string;
  endDate: string;
  cancelledAt?: string;
  cancellationReason?: string;
  paymentTransactionId?: string;
  amountPaid: number;
  bidsUsedThisMonth: number;
  lastBidResetDate: string;
  autoRenew: boolean;
  lastRenewalAttempt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface FeatureAccessResponse {
  hasAccess: boolean;
  message: string;
  remainingBids?: number;
  commissionRate?: number;
}

@Injectable({
  providedIn: 'root'
})
export class SubscriptionService {
  private apiUrl = 'http://localhost:8091/api/subscriptions';
  private currentSubscriptionSubject = new BehaviorSubject<Subscription | null>(null);
  public currentSubscription$ = this.currentSubscriptionSubject.asObservable();

  constructor(private http: HttpClient) {}

  // Plans
  getAllPlans(): Observable<SubscriptionPlan[]> {
    return this.http.get<SubscriptionPlan[]>(`${this.apiUrl}/plans`);
  }

  getPlanById(planId: number): Observable<SubscriptionPlan> {
    return this.http.get<SubscriptionPlan>(`${this.apiUrl}/plans/${planId}`);
  }

  createPlan(plan: SubscriptionPlan): Observable<SubscriptionPlan> {
    return this.http.post<SubscriptionPlan>(`${this.apiUrl}/plans`, plan);
  }

  // Subscriptions
  createSubscription(userId: number, planId: number, autoRenew: boolean = true): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/subscriptions/create`, {
      userId,
      planId,
      autoRenew
    });
  }

  getActiveSubscription(userId: number): Observable<Subscription> {
    return this.http.get<Subscription>(`${this.apiUrl}/subscriptions/user/${userId}/active`)
      .pipe(
        tap(subscription => this.currentSubscriptionSubject.next(subscription))
      );
  }

  getUserSubscriptionHistory(userId: number): Observable<Subscription[]> {
    return this.http.get<Subscription[]>(`${this.apiUrl}/subscriptions/user/${userId}/history`);
  }

  cancelSubscription(userId: number, reason?: string): Observable<any> {
    const params = reason ? new HttpParams().set('reason', reason) : new HttpParams();
    return this.http.post<any>(`${this.apiUrl}/subscriptions/cancel/${userId}`, null, { params })
      .pipe(
        tap(() => this.currentSubscriptionSubject.next(null))
      );
  }

  changePlan(userId: number, newPlanId: number, immediate: boolean = true): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/subscriptions/change-plan`, {
      userId,
      newPlanId,
      immediate
    }).pipe(
      tap(response => this.currentSubscriptionSubject.next(response.subscription))
    );
  }

  // Feature Access
  validateFeatureAccess(userId: number, featureName: string): Observable<FeatureAccessResponse> {
    return this.http.post<FeatureAccessResponse>(`${this.apiUrl}/subscriptions/validate-access`, {
      userId,
      featureName
    });
  }

  canPlaceBid(userId: number): Observable<{ canBid: boolean }> {
    return this.http.get<{ canBid: boolean }>(`${this.apiUrl}/subscriptions/user/${userId}/can-bid`);
  }

  incrementBidUsage(userId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/subscriptions/user/${userId}/increment-bid`, null);
  }

  // Payment Webhook (for testing)
  simulatePaymentWebhook(subscriptionId: number, status: 'SUCCESS' | 'FAILED', amount: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/webhook/payment`, {
      transactionId: `TXN-${Date.now()}`,
      subscriptionId,
      status,
      amount,
      paymentMethod: 'credit_card'
    });
  }

  // Helper methods
  isSubscriptionActive(subscription: Subscription | null): boolean {
    if (!subscription) return false;
    return subscription.status === 'ACTIVE' && new Date(subscription.endDate) > new Date();
  }

  getDaysRemaining(subscription: Subscription): number {
    const endDate = new Date(subscription.endDate);
    const today = new Date();
    const diffTime = endDate.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'ACTIVE': 'success',
      'PENDING_PAYMENT': 'warning',
      'EXPIRED': 'danger',
      'CANCELLED': 'secondary',
      'SUSPENDED': 'warning',
      'PAYMENT_FAILED': 'danger'
    };
    return colors[status] || 'secondary';
  }

  getPlanColor(planName: string): string {
    const colors: { [key: string]: string } = {
      'FREE': 'secondary',
      'BASIC': 'info',
      'PRO': 'primary',
      'ENTERPRISE': 'success'
    };
    return colors[planName] || 'primary';
  }
}
