# Subscription Service - Postman Collection

**Base URL:** `http://localhost:8091`

---

## 📋 SUBSCRIPTION PLANS

### 1. GET All Plans
```
GET http://localhost:8091/api/subscriptions/plans
```

### 2. GET Plan by ID
```
GET http://localhost:8091/api/subscriptions/plans/1
GET http://localhost:8091/api/subscriptions/plans/2
GET http://localhost:8091/api/subscriptions/plans/3
GET http://localhost:8091/api/subscriptions/plans/4
```

### 3. POST Create Plan
```
POST http://localhost:8091/api/subscriptions/plans
Content-Type: application/json

{
  "planName": "PREMIUM",
  "description": "Premium plan",
  "price": 49.99,
  "billingCycle": "MONTHLY",
  "maxBidsPerMonth": 150,
  "commissionRate": 4.5,
  "hasAnalyticsAccess": true,
  "hasProfileBoost": true,
  "hasPrioritySupport": true,
  "hasFeaturedListing": true,
  "maxProjects": 75,
  "isActive": true
}
```

---

## 💳 SUBSCRIPTIONS

### 4. POST Create Subscription
```
POST http://localhost:8091/api/subscriptions/create
Content-Type: application/json

{
  "userId": 1,
  "planId": 2,
  "autoRenew": true
}
```

### 5. GET Active Subscription
```
GET http://localhost:8091/api/subscriptions/user/1/active
```

### 6. GET User History
```
GET http://localhost:8091/api/subscriptions/user/1/history
```

### 7. POST Cancel Subscription
```
POST http://localhost:8091/api/subscriptions/cancel/1?reason=Testing
```

### 8. POST Change Plan
```
POST http://localhost:8091/api/subscriptions/change-plan
Content-Type: application/json

{
  "userId": 1,
  "newPlanId": 3,
  "immediate": true
}
```

---

## 🎯 FEATURE ACCESS

### 9. POST Validate Feature Access
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

{
  "userId": 1,
  "featureName": "BID"
}
```

**Feature Names:** BID, ANALYTICS, PROFILE_BOOST, COMMISSION

### 10. GET Can Place Bid
```
GET http://localhost:8091/api/subscriptions/user/1/can-bid
```

### 11. POST Increment Bid
```
POST http://localhost:8091/api/subscriptions/user/1/increment-bid
```

---

## 💰 PAYMENT WEBHOOK

### 12. POST Payment Success
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

{
  "transactionId": "TXN123456",
  "subscriptionId": 1,
  "status": "SUCCESS",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

### 13. POST Payment Failed
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

{
  "transactionId": "TXN999999",
  "subscriptionId": 1,
  "status": "FAILED",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

---

## 🧪 QUICK TEST SEQUENCE

1. **GET** `/api/subscriptions/plans` - View all plans
2. **POST** `/api/subscriptions/create` - Create subscription (userId: 1, planId: 2)
3. **POST** `/api/subscriptions/webhook/payment` - Activate with payment
4. **GET** `/api/subscriptions/user/1/active` - Check active subscription
5. **GET** `/api/subscriptions/user/1/can-bid` - Check bid permission
6. **POST** `/api/subscriptions/user/1/increment-bid` - Use a bid
7. **POST** `/api/subscriptions/validate-access` - Validate BID feature
8. **POST** `/api/subscriptions/change-plan` - Upgrade to PRO
9. **GET** `/api/subscriptions/user/1/history` - View history
10. **POST** `/api/subscriptions/cancel/1` - Cancel subscription

---

## 📊 H2 Database Console
```
http://localhost:8091/h2-console

JDBC URL: jdbc:h2:mem:freelancify-subscriptions
Username: sa
Password: (empty)
```
