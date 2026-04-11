# Subscription Service - Navigation Guide

## 🧭 How to Add Subscription Links to Your App

### Option 1: Add to Main Navbar

**Location**: `frontend/src/app/components/navbar/navbar.component.html`

```html
<!-- Add these links to your existing navbar -->

<!-- For Regular Users -->
<li class="nav-item">
  <a class="nav-link" routerLink="/subscription-plans" routerLinkActive="active">
    <i class="bi bi-lightning-charge me-2"></i>
    Plans
  </a>
</li>

<li class="nav-item">
  <a class="nav-link" routerLink="/my-subscription" routerLinkActive="active">
    <i class="bi bi-credit-card me-2"></i>
    My Subscription
  </a>
</li>

<!-- For Admins -->
<li class="nav-item" *ngIf="isAdmin">
  <a class="nav-link" routerLink="/admin/subscriptions" routerLinkActive="active">
    <i class="bi bi-shield-check me-2"></i>
    Subscriptions
  </a>
</li>
```

---

### Option 2: Add to Sidebar

**Location**: `frontend/src/app/components/sidebar/sidebar.component.html`

```html
<!-- User Section -->
<div class="sidebar-section">
  <h6 class="sidebar-heading">Subscription</h6>
  <ul class="nav flex-column">
    <li class="nav-item">
      <a class="nav-link" routerLink="/subscription-plans" routerLinkActive="active">
        <i class="bi bi-lightning-charge"></i>
        <span>Browse Plans</span>
      </a>
    </li>
    <li class="nav-item">
      <a class="nav-link" routerLink="/my-subscription" routerLinkActive="active">
        <i class="bi bi-credit-card"></i>
        <span>My Subscription</span>
      </a>
    </li>
  </ul>
</div>

<!-- Admin Section -->
<div class="sidebar-section" *ngIf="isAdmin">
  <h6 class="sidebar-heading">Admin</h6>
  <ul class="nav flex-column">
    <li class="nav-item">
      <a class="nav-link" routerLink="/admin/subscriptions" routerLinkActive="active">
        <i class="bi bi-shield-check"></i>
        <span>Manage Subscriptions</span>
      </a>
    </li>
  </ul>
</div>
```

---

### Option 3: Add to Dashboard Cards

**Location**: `frontend/src/app/components/dashboard/dashboard.component.html`

```html
<!-- Quick Access Cards -->
<div class="row">
  <div class="col-md-4">
    <div class="card dashboard-card" routerLink="/subscription-plans">
      <div class="card-body text-center">
        <i class="bi bi-lightning-charge display-4 text-primary"></i>
        <h5 class="mt-3">Subscription Plans</h5>
        <p class="text-muted">View and upgrade your plan</p>
      </div>
    </div>
  </div>
  
  <div class="col-md-4">
    <div class="card dashboard-card" routerLink="/my-subscription">
      <div class="card-body text-center">
        <i class="bi bi-credit-card display-4 text-success"></i>
        <h5 class="mt-3">My Subscription</h5>
        <p class="text-muted">Manage your subscription</p>
      </div>
    </div>
  </div>
</div>
```

---

### Option 4: Add to User Profile Dropdown

**Location**: `frontend/src/app/components/header/header.component.html`

```html
<!-- User Dropdown Menu -->
<div class="dropdown">
  <button class="btn btn-link dropdown-toggle" data-bs-toggle="dropdown">
    <i class="bi bi-person-circle"></i>
    {{ username }}
  </button>
  <ul class="dropdown-menu dropdown-menu-end">
    <li>
      <a class="dropdown-item" routerLink="/profile">
        <i class="bi bi-person me-2"></i>
        Profile
      </a>
    </li>
    <li>
      <a class="dropdown-item" routerLink="/my-subscription">
        <i class="bi bi-credit-card me-2"></i>
        My Subscription
      </a>
    </li>
    <li>
      <a class="dropdown-item" routerLink="/subscription-plans">
        <i class="bi bi-lightning-charge me-2"></i>
        Upgrade Plan
      </a>
    </li>
    <li><hr class="dropdown-divider"></li>
    <li>
      <a class="dropdown-item" (click)="logout()">
        <i class="bi bi-box-arrow-right me-2"></i>
        Logout
      </a>
    </li>
  </ul>
</div>
```

---

### Option 5: Add Subscription Badge to Navbar

**Show current plan in navbar:**

```html
<!-- In navbar -->
<li class="nav-item">
  <a class="nav-link" routerLink="/my-subscription">
    <span class="badge bg-primary">
      {{ currentPlan || 'FREE' }}
    </span>
  </a>
</li>
```

**Component TypeScript:**
```typescript
// In navbar.component.ts
export class NavbarComponent implements OnInit {
  currentPlan: string = '';
  
  ngOnInit() {
    this.loadCurrentPlan();
  }
  
  loadCurrentPlan() {
    const userId = this.getUserId();
    this.subscriptionService.getActiveSubscription(userId).subscribe({
      next: (subscription) => {
        this.currentPlan = subscription.plan.planName;
      },
      error: () => {
        this.currentPlan = 'FREE';
      }
    });
  }
}
```

---

## 🎨 Styling Examples

### Navbar Link with Badge

```html
<li class="nav-item position-relative">
  <a class="nav-link" routerLink="/my-subscription">
    <i class="bi bi-credit-card me-2"></i>
    My Subscription
    <span class="badge bg-success position-absolute top-0 start-100 translate-middle" 
          *ngIf="hasActiveSubscription">
      Active
    </span>
  </a>
</li>
```

### Sidebar with Icons

```html
<style>
.sidebar-link {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  color: #6c757d;
  text-decoration: none;
  transition: all 0.3s ease;
}

.sidebar-link:hover {
  background-color: #f8f9fa;
  color: #0d6efd;
  padding-left: 1.5rem;
}

.sidebar-link i {
  font-size: 1.25rem;
  margin-right: 0.75rem;
}

.sidebar-link.active {
  background-color: #e7f1ff;
  color: #0d6efd;
  border-left: 3px solid #0d6efd;
}
</style>

<a class="sidebar-link" routerLink="/subscription-plans" routerLinkActive="active">
  <i class="bi bi-lightning-charge"></i>
  <span>Plans</span>
</a>
```

---

## 🔔 Add Upgrade Prompts

### Show Upgrade Banner for FREE Users

```html
<!-- In dashboard or any page -->
<div class="alert alert-info d-flex align-items-center" *ngIf="isFreePlan">
  <i class="bi bi-info-circle fs-4 me-3"></i>
  <div class="flex-grow-1">
    <strong>Upgrade to unlock more features!</strong>
    Get unlimited bids, lower commission, and priority support.
  </div>
  <button class="btn btn-primary" routerLink="/subscription-plans">
    View Plans
  </button>
</div>
```

### Show Feature Locked Modal

```html
<!-- Feature locked modal -->
<div class="modal" *ngIf="showUpgradeModal">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header bg-primary text-white">
        <h5 class="modal-title">
          <i class="bi bi-lock me-2"></i>
          Premium Feature
        </h5>
        <button type="button" class="btn-close btn-close-white" (click)="closeModal()"></button>
      </div>
      <div class="modal-body text-center py-4">
        <i class="bi bi-star display-1 text-warning mb-3"></i>
        <h4>Upgrade to Access This Feature</h4>
        <p class="text-muted">
          This feature is available on PRO and ENTERPRISE plans.
        </p>
        <ul class="list-unstyled text-start mt-4">
          <li class="mb-2">
            <i class="bi bi-check-circle text-success me-2"></i>
            Unlimited bids per month
          </li>
          <li class="mb-2">
            <i class="bi bi-check-circle text-success me-2"></i>
            Lower commission rates
          </li>
          <li class="mb-2">
            <i class="bi bi-check-circle text-success me-2"></i>
            Priority support
          </li>
        </ul>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" (click)="closeModal()">
          Maybe Later
        </button>
        <button class="btn btn-primary" routerLink="/subscription-plans" (click)="closeModal()">
          <i class="bi bi-lightning-charge me-2"></i>
          View Plans
        </button>
      </div>
    </div>
  </div>
</div>
```

---

## 🎯 Context-Aware Navigation

### Show Different Links Based on Subscription Status

```typescript
// In your component
export class NavigationComponent implements OnInit {
  hasSubscription = false;
  isFreePlan = false;
  canUpgrade = false;
  
  ngOnInit() {
    this.checkSubscriptionStatus();
  }
  
  checkSubscriptionStatus() {
    const userId = this.getUserId();
    this.subscriptionService.getActiveSubscription(userId).subscribe({
      next: (subscription) => {
        this.hasSubscription = true;
        this.isFreePlan = subscription.plan.planName === 'FREE';
        this.canUpgrade = subscription.plan.planName !== 'ENTERPRISE';
      },
      error: () => {
        this.hasSubscription = false;
        this.isFreePlan = true;
        this.canUpgrade = true;
      }
    });
  }
}
```

```html
<!-- Show different links based on status -->
<li class="nav-item" *ngIf="!hasSubscription">
  <a class="nav-link" routerLink="/subscription-plans">
    <i class="bi bi-lightning-charge me-2"></i>
    Get Started
  </a>
</li>

<li class="nav-item" *ngIf="hasSubscription && canUpgrade">
  <a class="nav-link" routerLink="/subscription-plans">
    <i class="bi bi-arrow-up-circle me-2"></i>
    Upgrade
  </a>
</li>

<li class="nav-item" *ngIf="hasSubscription">
  <a class="nav-link" routerLink="/my-subscription">
    <i class="bi bi-credit-card me-2"></i>
    My Subscription
  </a>
</li>
```

---

## 📱 Mobile Navigation

### Bottom Navigation Bar (Mobile)

```html
<!-- Mobile bottom nav -->
<nav class="mobile-bottom-nav d-md-none">
  <a routerLink="/home" routerLinkActive="active">
    <i class="bi bi-house"></i>
    <span>Home</span>
  </a>
  <a routerLink="/projects" routerLinkActive="active">
    <i class="bi bi-briefcase"></i>
    <span>Projects</span>
  </a>
  <a routerLink="/subscription-plans" routerLinkActive="active">
    <i class="bi bi-lightning-charge"></i>
    <span>Plans</span>
  </a>
  <a routerLink="/my-subscription" routerLinkActive="active">
    <i class="bi bi-credit-card"></i>
    <span>Subscription</span>
  </a>
  <a routerLink="/profile" routerLinkActive="active">
    <i class="bi bi-person"></i>
    <span>Profile</span>
  </a>
</nav>

<style>
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: space-around;
  padding: 0.5rem 0;
  z-index: 1000;
}

.mobile-bottom-nav a {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #6c757d;
  text-decoration: none;
  font-size: 0.75rem;
  padding: 0.25rem;
}

.mobile-bottom-nav a i {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.mobile-bottom-nav a.active {
  color: #0d6efd;
}
</style>
```

---

## 🎨 Custom Navigation Styles

### Gradient Navigation Button

```html
<a class="nav-link gradient-btn" routerLink="/subscription-plans">
  <i class="bi bi-lightning-charge me-2"></i>
  Upgrade Now
</a>

<style>
.gradient-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white !important;
  border-radius: 20px;
  padding: 0.5rem 1.5rem !important;
  font-weight: 600;
  transition: all 0.3s ease;
}

.gradient-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}
</style>
```

### Animated Badge

```html
<a class="nav-link position-relative" routerLink="/my-subscription">
  <i class="bi bi-credit-card me-2"></i>
  My Subscription
  <span class="badge bg-success pulse-badge">Active</span>
</a>

<style>
.pulse-badge {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.05);
  }
}
</style>
```

---

## 🔗 Quick Links Summary

### User Routes
- `/subscription-plans` - Browse and subscribe to plans
- `/my-subscription` - Manage current subscription

### Admin Routes
- `/admin/subscriptions` - Manage plans and view analytics

### Icons to Use
- `bi-lightning-charge` - Plans/Subscribe
- `bi-credit-card` - My Subscription
- `bi-shield-check` - Admin
- `bi-star` - Premium/Upgrade
- `bi-arrow-up-circle` - Upgrade
- `bi-check-circle` - Active status

---

## ✅ Implementation Checklist

- [ ] Add links to main navbar
- [ ] Add links to sidebar (if applicable)
- [ ] Add dashboard cards (if applicable)
- [ ] Add user dropdown menu items
- [ ] Add mobile navigation
- [ ] Add upgrade prompts for FREE users
- [ ] Add feature locked modals
- [ ] Add subscription status badge
- [ ] Test all navigation links
- [ ] Verify route guards work

---

**Choose the navigation style that best fits your app's design!** 🎨
