# Complete Subscription Service Endpoints

Base URL: `http://localhost:8091`

---

## 📋 SUBSCRIPTION PLANS (3 endpoints)

### 1. GET All Plans
```
GET http://localhost:8091/api/subscriptions/plans
```
**Response:** List of all active subscription plans

---

### 2. GET Plan by ID
```
GET http://localhost:8091/api/subscriptions/plans/1
GET http://localhost:8091/api/subscriptions/plans/2
GET http://localhost:8091/api/subscriptions/plans/3
GET http://localhost:8091/api/subscriptions/plans/4
```
**Response:** Single plan details

---

### 3. POST Create Plan
```
POST http://localhost:8091/api/subscriptions/plans
Content-Type: application/json

{
  "planName": "PLATINUM",
  "description": "Platinum plan",
  "price": 149.99,
  "billingCycle": "MONTHLY",
  "maxBidsPerMonth": 500,
  "commissionRate": 2.5,
  "hasAnalyticsAccess": true,
  "hasProfileBoost": true,
  "hasPrioritySupport": true,
  "hasFeaturedListing": true,
  "maxProjects": 200,
  "isActive": true
}
```

---

## 💳 SUBSCRIPTIONS (8 endpoints)

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
**Response:** Subscription object with payment URL

---

### 5. GET Active Subscription
```
GET http://localhost:8091/api/subscriptions/user/1/active
```
**Response:** User's active subscription or 404

---

### 6. GET User History
```
GET http://localhost:8091/api/subscriptions/user/1/history
```
**Response:** List of all subscriptions for the user

---

### 7. GET All Subscriptions
```
GET http://localhost:8091/api/subscriptions/all
```
**Response:** List of ALL subscriptions in the system

---

### 8. POST Cancel Subscription
```
POST http://localhost:8091/api/subscriptions/cancel/1?reason=Testing
```
**Response:** Cancelled subscription object

---

### 9. POST Change Plan
```
POST http://localhost:8091/api/subscriptions/change-plan
Content-Type: application/json

{
  "userId": 1,
  "newPlanId": 3,
  "immediate": true
}
```
**Response:** Updated subscription object

---

### 10. GET User Summary
```
GET http://localhost:8091/api/subscriptions/user/1/summary
```
**Response:** Complete user subscription summary including:
- Active subscription
- Total subscriptions count
- Subscription history
- Current plan
- Bids remaining
- Bids used

---

### 11. GET All Users
```
GET http://localhost:8091/api/subscriptions/users
```
**Response:** List of all unique users with their latest subscription info
```json
[
  {
    "userId": 1,
    "subscription": {...},
    "currentPlan": "BASIC",
    "status": "ACTIVE",
    "hasActiveSubscription": true
  },
  {
    "userId": 2,
    "subscription": {...},
    "currentPlan": "PRO",
    "status": "ACTIVE",
    "hasActiveSubscription": true
  }
]
```

---

## 🎯 FEATURE ACCESS (3 endpoints)

### 12. POST Validate Feature Access
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

{
  "userId": 1,
  "featureName": "BID"
}
```
**Feature Names:** BID, ANALYTICS, PROFILE_BOOST, COMMISSION

**Response:**
```json
{
  "hasAccess": true,
  "message": "Bid allowed",
  "remainingBids": 19,
  "commissionRate": null
}
```

---

### 13. GET Can Place Bid
```
GET http://localhost:8091/api/subscriptions/user/1/can-bid
```
**Response:**
```json
{
  "canBid": true
}
```

---

### 14. POST Increment Bid
```
POST http://localhost:8091/api/subscriptions/user/1/increment-bid
```
**Response:**
```json
{
  "message": "Bid usage incremented"
}
```

---

## 💰 PAYMENT WEBHOOK (2 endpoints)

### 15. POST Payment Success
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

---

### 16. POST Payment Failed
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

## 🧪 COMPLETE TEST FLOW

### Scenario: Create User, Subscribe, and Manage

#### Step 1: View Available Plans
```
GET http://localhost:8091/api/subscriptions/plans
```

#### Step 2: Create Subscription for User 100
```
POST http://localhost:8091/api/subscriptions/create

{
  "userId": 100,
  "planId": 2,
  "autoRenew": true
}
```
**Note the subscriptionId from response (e.g., 1)**

#### Step 3: Activate with Payment
```
POST http://localhost:8091/api/subscriptions/webhook/payment

{
  "transactionId": "TXN100",
  "subscriptionId": 1,
  "status": "SUCCESS",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

#### Step 4: Get User Summary
```
GET http://localhost:8091/api/subscriptions/user/100/summary
```

#### Step 5: Check Bid Permission
```
GET http://localhost:8091/api/subscriptions/user/100/can-bid
```

#### Step 6: Place 3 Bids
```
POST http://localhost:8091/api/subscriptions/user/100/increment-bid
POST http://localhost:8091/api/subscriptions/user/100/increment-bid
POST http://localhost:8091/api/subscriptions/user/100/increment-bid
```

#### Step 7: Check Remaining Bids
```
POST http://localhost:8091/api/subscriptions/validate-access

{
  "userId": 100,
  "featureName": "BID"
}
```

#### Step 8: Upgrade to PRO
```
POST http://localhost:8091/api/subscriptions/change-plan

{
  "userId": 100,
  "newPlanId": 3,
  "immediate": true
}
```

#### Step 9: View All Users
```
GET http://localhost:8091/api/subscriptions/users
```

#### Step 10: Get Updated Summary
```
GET http://localhost:8091/api/subscriptions/user/100/summary
```

---

## 📊 SUMMARY OF ALL ENDPOINTS

**Total: 16 Endpoints**

- **Plans:** 3 endpoints
- **Subscriptions:** 8 endpoints
- **Feature Access:** 3 endpoints
- **Payment Webhooks:** 2 endpoints

---

## 🎯 NEW ENDPOINTS ADDED

### GET All Users
```
GET http://localhost:8091/api/subscriptions/users
```
Returns all unique users with their subscription information.

### GET User Summary
```
GET http://localhost:8091/api/subscriptions/user/{userId}/summary
```
Returns complete summary for a specific user including:
- Active subscription
- Total subscriptions
- History
- Current plan
- Bids remaining/used

---

## 📝 RESPONSE EXAMPLES

### Get All Users Response
```json
[
  {
    "userId": 1,
    "subscription": {
      "id": 1,
      "userId": 1,
      "plan": {
        "id": 2,
        "planName": "BASIC",
        "price": 9.99
      },
      "status": "ACTIVE",
      "startDate": "2026-04-13T...",
      "endDate": "2026-05-13T...",
      "bidsUsedThisMonth": 3,
      "autoRenew": true
    },
    "currentPlan": "BASIC",
    "status": "ACTIVE",
    "hasActiveSubscription": true
  },
  {
    "userId": 2,
    "subscription": {...},
    "currentPlan": "PRO",
    "status": "ACTIVE",
    "hasActiveSubscription": true
  }
]
```

### Get User Summary Response
```json
{
  "userId": 1,
  "activeSubscription": {
    "id": 1,
    "userId": 1,
    "plan": {
      "id": 2,
      "planName": "BASIC",
      "price": 9.99,
      "maxBidsPerMonth": 20
    },
    "status": "ACTIVE",
    "bidsUsedThisMonth": 3
  },
  "totalSubscriptions": 2,
  "subscriptionHistory": [...],
  "currentPlan": "BASIC",
  "bidsRemaining": 17,
  "bidsUsed": 3
}
```

---

## 🔧 POSTMAN COLLECTION

Import these into Postman for easy testing:

1. Create a new collection: "Subscription Service"
2. Add environment variable: `baseUrl = http://localhost:8091`
3. Import all 16 endpoints above
4. Test in sequence

---

## ✅ TESTING CHECKLIST

- [ ] Get all plans
- [ ] Create subscription for user 1
- [ ] Activate with payment webhook
- [ ] Get active subscription
- [ ] Check bid permission
- [ ] Increment bid usage
- [ ] Validate feature access
- [ ] Get user summary
- [ ] Get all users
- [ ] Change plan
- [ ] Get subscription history
- [ ] Cancel subscription
- [ ] Verify cancellation

---

## 🎉 ALL DONE!

You now have complete user and subscription management with 16 fully functional endpoints!
