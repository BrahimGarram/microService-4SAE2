# Subscription Service - Frontend Integration Guide

## Overview

Complete Angular frontend implementation for the Subscription Service with both user and admin interfaces.

## Components Created

### 1. User Components

#### Subscription Plans (`/subscription-plans`)
- **Location**: `src/app/components/subscription-plans/`
- **Features**:
  - Display all available subscription plans
  - Compare plan features side-by-side
  - Subscribe to new plans
  - Upgrade/downgrade existing subscriptions
  - Responsive card-based layout
  - Real-time subscription status
  - FAQ section

#### My Subscription (`/my-subscription`)
- **Location**: `src/app/components/my-subscription/`
- **Features**:
  - View current subscription details
  - Monitor usage statistics (bids, commission rate)
  - Track days remaining
  - Cancel subscription with reason
  - View subscription history
  - Upgrade plan button
  - Feature access indicators

### 2. Admin Components

#### Admin Subscriptions (`/admin/subscriptions`)
- **Location**: `src/app/components/admin-subscriptions/`
- **Features**:
  - **Plans Tab**: Create, edit, and manage subscription plans
  - **Subscriptions Tab**: View all user subscriptions
  - **Analytics Tab**: 
    - Total subscriptions count
    - Active subscriptions
    - Monthly revenue
    - Churn rate
    - Plan distribution chart
    - Recent activity feed

### 3. Service

#### Subscription Service
- **Location**: `src/app/services/subscription.service.ts`
- **Methods**:
  - `getAllPlans()` - Get all subscription plans
  - `getPlanById(id)` - Get specific plan
  - `createPlan(plan)` - Create new plan (admin)
  - `createSubscription(userId, planId)` - Subscribe to plan
  - `getActiveSubscription(userId)` - Get user's active subscription
  - `getUserSubscriptionHistory(userId)` - Get subscription history
  - `cancelSubscription(userId, reason)` - Cancel subscription
  - `changePlan(userId, newPlanId, immediate)` - Upgrade/downgrade
  - `validateFeatureAccess(userId, feature)` - Check feature access
  - `canPlaceBid(userId)` - Check bid permission
  - `incrementBidUsage(userId)` - Track bid usage
  - `simulatePaymentWebhook()` - Test payment (development)

## Routes Added

### User Routes
```typescript
{ path: 'subscription-plans', component: SubscriptionPlansComponent, canActivate: [AuthGuard] }
{ path: 'my-subscription', component: MySubscriptionComponent, canActivate: [AuthGuard] }
```

### Admin Routes
```typescript
{ path: 'admin/subscriptions', component: AdminSubscriptionsComponent, canActivate: [AdminGuard] }
```

## Setup Instructions

### 1. Install Dependencies
All required dependencies are already in your `package.json`:
- `@angular/common`
- `@angular/forms`
- `@angular/router`
- `rxjs`

### 2. Update Navigation

Add to your navbar/sidebar:

**User Navigation:**
```html
<li class="nav-item">
  <a class="nav-link" routerLink="/subscription-plans">
    <i class="bi bi-lightning-charge"></i> Plans
  </a>
</li>
<li class="nav-item">
  <a class="nav-link" routerLink="/my-subscription">
    <i class="bi bi-credit-card"></i> My Subscription
  </a>
</li>
```

**Admin Navigation:**
```html
<li class="nav-item">
  <a class="nav-link" routerLink="/admin/subscriptions">
    <i class="bi bi-shield-check"></i> Subscriptions
  </a>
</li>
```

### 3. Start Backend Service

Ensure the subscription service is running:
```bash
cd subscription-service
./mvnw spring-boot:run
```

Service should be available at: `http://localhost:8091/api/subscriptions`

### 4. Start Frontend

```bash
cd frontend
npm start
```

Frontend will be available at: `http://localhost:4200`

## Usage Guide

### For Users

#### 1. View Plans
Navigate to `/subscription-plans` to see all available plans.

#### 2. Subscribe
- Click "Subscribe" on any plan
- For FREE plan: Instant activation
- For paid plans: Redirects to payment (simulated in demo)
- Subscription activates automatically after payment

#### 3. Manage Subscription
Navigate to `/my-subscription` to:
- View current plan details
- Monitor usage (bids used, remaining)
- Check days until renewal
- Upgrade to higher tier
- Cancel subscription

#### 4. Upgrade/Downgrade
- From `/subscription-plans`: Click "Upgrade" or "Downgrade"
- Changes apply immediately with proration
- Or schedule for next billing cycle

### For Admins

#### 1. Manage Plans
- Navigate to `/admin/subscriptions`
- Click "Plans" tab
- Create new plans with custom features
- Edit existing plans
- Activate/deactivate plans

#### 2. View Subscriptions
- Click "Subscriptions" tab
- View all user subscriptions
- Search and filter
- Monitor subscription status

#### 3. Analytics
- Click "Analytics" tab
- View key metrics:
  - Total subscriptions
  - Active subscriptions
  - Monthly revenue
  - Churn rate
- Plan distribution chart
- Recent activity feed

## API Integration

### Base URL
```typescript
private apiUrl = 'http://localhost:8091/api/subscriptions';
```

### Example API Calls

**Get All Plans:**
```typescript
this.subscriptionService.getAllPlans().subscribe(plans => {
  console.log('Plans:', plans);
});
```

**Subscribe to Plan:**
```typescript
this.subscriptionService.createSubscription(userId, planId).subscribe(response => {
  console.log('Subscription created:', response);
  // Redirect to payment URL
  window.location.href = response.paymentUrl;
});
```

**Check Feature Access:**
```typescript
this.subscriptionService.validateFeatureAccess(userId, 'BID').subscribe(response => {
  if (response.hasAccess) {
    // Allow bid placement
  } else {
    // Show upgrade prompt
  }
});
```

## Feature Access Integration

### Before Placing a Bid

```typescript
// In your project/bid component
placeBid() {
  this.subscriptionService.canPlaceBid(this.userId).subscribe(response => {
    if (response.canBid) {
      // Place the bid
      this.projectService.createBid(bidData).subscribe(() => {
        // Increment usage counter
        this.subscriptionService.incrementBidUsage(this.userId).subscribe();
      });
    } else {
      this.toastService.showWarning('Bid limit reached. Upgrade your plan!');
      this.router.navigate(['/subscription-plans']);
    }
  });
}
```

### Check Analytics Access

```typescript
// In your analytics component
ngOnInit() {
  this.subscriptionService.validateFeatureAccess(this.userId, 'ANALYTICS').subscribe(response => {
    if (!response.hasAccess) {
      this.router.navigate(['/subscription-plans']);
      this.toastService.showWarning('Upgrade to access analytics');
    }
  });
}
```

### Get Commission Rate

```typescript
// In your payment component
calculateCommission(amount: number) {
  this.subscriptionService.validateFeatureAccess(this.userId, 'COMMISSION').subscribe(response => {
    const rate = response.commissionRate;
    const commission = amount * (rate / 100);
    console.log(`Commission: ${commission} (${rate}%)`);
  });
}
```

## Styling

All components use Bootstrap 5 with custom CSS:
- Responsive design
- Smooth animations
- Hover effects
- Modern card layouts
- Color-coded status badges
- Progress bars for usage tracking

### Bootstrap Icons Used
- `bi-lightning-charge` - Subscribe/Plans
- `bi-credit-card` - Subscription
- `bi-shield-check` - Admin
- `bi-check-circle` - Active/Success
- `bi-x-circle` - Cancelled/Failed
- `bi-graph-up` - Analytics
- `bi-people` - Users
- `bi-currency-dollar` - Revenue

## Testing

### Test Subscription Flow

1. **Login as User**
2. **Navigate to Plans**: `/subscription-plans`
3. **Subscribe to BASIC Plan**
4. **Payment Simulated**: Automatically activates
5. **View Subscription**: `/my-subscription`
6. **Check Usage**: See bids remaining
7. **Upgrade to PRO**: From plans page
8. **Cancel**: From my subscription page

### Test Admin Features

1. **Login as Admin**
2. **Navigate to Admin**: `/admin/subscriptions`
3. **Create New Plan**: Click "Create New Plan"
4. **Fill Form**: Set price, features, limits
5. **Save Plan**: Plan appears in list
6. **View Analytics**: Check metrics and charts

## Troubleshooting

### Issue: Plans Not Loading
**Solution**: Check backend is running on port 8091
```bash
curl http://localhost:8091/api/subscriptions/plans
```

### Issue: CORS Error
**Solution**: Backend CORS is configured for `http://localhost:4200`
Verify in `application.yml`:
```yaml
spring:
  web:
    cors:
      allowed-origins:
        - "http://localhost:4200"
```

### Issue: Subscription Not Activating
**Solution**: Check payment webhook simulation
```typescript
// In subscription-plans.component.ts
simulatePayment(subscriptionId: number, amount: number): void {
  this.subscriptionService.simulatePaymentWebhook(subscriptionId, 'SUCCESS', amount)
    .subscribe(/* ... */);
}
```

### Issue: Feature Access Always Denied
**Solution**: Ensure user has active subscription
```typescript
this.subscriptionService.getActiveSubscription(userId).subscribe(sub => {
  console.log('Active subscription:', sub);
  console.log('Status:', sub.status);
  console.log('End date:', sub.endDate);
});
```

## Production Considerations

### 1. Payment Gateway Integration
Replace simulated payment with actual gateway:
```typescript
// In subscription-plans.component.ts
createNewSubscription(plan: SubscriptionPlan): void {
  this.subscriptionService.createSubscription(this.userId, plan.id).subscribe(response => {
    // Redirect to actual payment gateway
    window.location.href = response.paymentUrl; // Stripe/PayPal URL
  });
}
```

### 2. Real-time Updates
Add WebSocket for real-time subscription updates:
```typescript
// Subscribe to subscription changes
this.websocketService.subscribe(`/user/${userId}/subscription`, (update) => {
  this.currentSubscription = update;
});
```

### 3. Caching
Cache subscription data to reduce API calls:
```typescript
// In subscription.service.ts
private subscriptionCache = new Map<number, Subscription>();

getActiveSubscription(userId: number): Observable<Subscription> {
  if (this.subscriptionCache.has(userId)) {
    return of(this.subscriptionCache.get(userId)!);
  }
  return this.http.get<Subscription>(`${this.apiUrl}/subscriptions/user/${userId}/active`)
    .pipe(tap(sub => this.subscriptionCache.set(userId, sub)));
}
```

### 4. Error Handling
Add comprehensive error handling:
```typescript
// In subscription.service.ts
private handleError(error: HttpErrorResponse) {
  if (error.status === 404) {
    return throwError(() => new Error('Subscription not found'));
  }
  if (error.status === 403) {
    return throwError(() => new Error('Access denied'));
  }
  return throwError(() => new Error('An error occurred'));
}
```

## Next Steps

1. ✅ Components created
2. ✅ Service implemented
3. ✅ Routes configured
4. ✅ Styling applied
5. ⏳ Test with real users
6. ⏳ Integrate payment gateway
7. ⏳ Add email notifications
8. ⏳ Implement analytics tracking
9. ⏳ Add subscription reminders
10. ⏳ Deploy to production

## Support

For issues or questions:
1. Check backend logs: `subscription-service/logs`
2. Check browser console for errors
3. Verify API endpoints with Postman
4. Review `TROUBLESHOOTING.md` in subscription-service

---

**Status**: ✅ Ready for Development Testing
**Frontend**: Complete with user and admin interfaces
**Backend Integration**: Fully connected to subscription-service API
