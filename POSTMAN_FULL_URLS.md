# Subscription Service - Complete URLs for Postman

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

Body (raw JSON):
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

Body (raw JSON):
{
  "userId": 1,
  "planId": 2,
  "autoRenew": true
}
```

### 5. GET Active Subscription for User 1
```
GET http://localhost:8091/api/subscriptions/user/1/active
```

### 6. GET Active Subscription for User 2
```
GET http://localhost:8091/api/subscriptions/user/2/active
```

### 7. GET Active Subscription for User 3
```
GET http://localhost:8091/api/subscriptions/user/3/active
```

### 8. GET User 1 History
```
GET http://localhost:8091/api/subscriptions/user/1/history
```

### 9. GET User 2 History
```
GET http://localhost:8091/api/subscriptions/user/2/history
```

### 10. POST Cancel Subscription User 1
```
POST http://localhost:8091/api/subscriptions/cancel/1?reason=Testing
```

### 11. POST Cancel Subscription User 2
```
POST http://localhost:8091/api/subscriptions/cancel/2?reason=Too%20expensive
```

### 12. POST Change Plan (Upgrade to PRO)
```
POST http://localhost:8091/api/subscriptions/change-plan
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "newPlanId": 3,
  "immediate": true
}
```

### 13. POST Change Plan (Downgrade to BASIC)
```
POST http://localhost:8091/api/subscriptions/change-plan
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "newPlanId": 2,
  "immediate": false
}
```

### 14. POST Change Plan (Upgrade to ENTERPRISE)
```
POST http://localhost:8091/api/subscriptions/change-plan
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "newPlanId": 4,
  "immediate": true
}
```

---

## 🎯 FEATURE ACCESS

### 15. POST Validate BID Feature
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "featureName": "BID"
}
```

### 16. POST Validate ANALYTICS Feature
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "featureName": "ANALYTICS"
}
```

### 17. POST Validate PROFILE_BOOST Feature
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "featureName": "PROFILE_BOOST"
}
```

### 18. POST Validate COMMISSION Feature
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "featureName": "COMMISSION"
}
```

### 19. GET Can User 1 Place Bid
```
GET http://localhost:8091/api/subscriptions/user/1/can-bid
```

### 20. GET Can User 2 Place Bid
```
GET http://localhost:8091/api/subscriptions/user/2/can-bid
```

### 21. GET Can User 3 Place Bid
```
GET http://localhost:8091/api/subscriptions/user/3/can-bid
```

### 22. POST Increment Bid for User 1
```
POST http://localhost:8091/api/subscriptions/user/1/increment-bid
```

### 23. POST Increment Bid for User 2
```
POST http://localhost:8091/api/subscriptions/user/2/increment-bid
```

### 24. POST Increment Bid for User 3
```
POST http://localhost:8091/api/subscriptions/user/3/increment-bid
```

---

## 💰 PAYMENT WEBHOOK

### 25. POST Payment Success for Subscription 1
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

Body (raw JSON):
{
  "transactionId": "TXN123456",
  "subscriptionId": 1,
  "status": "SUCCESS",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

### 26. POST Payment Success for Subscription 2
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

Body (raw JSON):
{
  "transactionId": "TXN789012",
  "subscriptionId": 2,
  "status": "SUCCESS",
  "amount": 29.99,
  "paymentMethod": "PAYPAL"
}
```

### 27. POST Payment Success for Subscription 3
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

Body (raw JSON):
{
  "transactionId": "TXN345678",
  "subscriptionId": 3,
  "status": "SUCCESS",
  "amount": 99.99,
  "paymentMethod": "DEBIT_CARD"
}
```

### 28. POST Payment Failed
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

Body (raw JSON):
{
  "transactionId": "TXN999999",
  "subscriptionId": 1,
  "status": "FAILED",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

---

## 🧪 RECOMMENDED TEST SEQUENCE

### Step 1: View Available Plans
```
GET http://localhost:8091/api/subscriptions/plans
```

### Step 2: Create Subscription for User 1 (BASIC Plan)
```
POST http://localhost:8091/api/subscriptions/create
Content-Type: application/json

{
  "userId": 1,
  "planId": 2,
  "autoRenew": true
}
```

### Step 3: Activate Subscription with Payment
```
POST http://localhost:8091/api/subscriptions/webhook/payment
Content-Type: application/json

{
  "transactionId": "TXN001",
  "subscriptionId": 1,
  "status": "SUCCESS",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

### Step 4: Check Active Subscription
```
GET http://localhost:8091/api/subscriptions/user/1/active
```

### Step 5: Check if User Can Bid
```
GET http://localhost:8091/api/subscriptions/user/1/can-bid
```

### Step 6: Place a Bid (Increment Usage)
```
POST http://localhost:8091/api/subscriptions/user/1/increment-bid
```

### Step 7: Validate BID Feature Access
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

{
  "userId": 1,
  "featureName": "BID"
}
```

### Step 8: Upgrade to PRO Plan
```
POST http://localhost:8091/api/subscriptions/change-plan
Content-Type: application/json

{
  "userId": 1,
  "newPlanId": 3,
  "immediate": true
}
```

### Step 9: Check Subscription History
```
GET http://localhost:8091/api/subscriptions/user/1/history
```

### Step 10: Cancel Subscription
```
POST http://localhost:8091/api/subscriptions/cancel/1?reason=Testing%20complete
```

---

## 📊 DATABASE ACCESS

### H2 Console
```
URL: http://localhost:8091/h2-console

JDBC URL: jdbc:h2:mem:freelancify-subscriptions
Username: sa
Password: (leave empty)
```

---

## 📝 NOTES FOR POSTMAN

1. **Set Content-Type Header** for all POST requests:
   - Key: `Content-Type`
   - Value: `application/json`

2. **Body Type** for POST requests:
   - Select: `raw`
   - Format: `JSON`

3. **No Authentication Required** - All endpoints are open for testing

4. **Default Plans Available** (IDs 1-4):
   - ID 1: FREE ($0.00)
   - ID 2: BASIC ($9.99)
   - ID 3: PRO ($29.99)
   - ID 4: ENTERPRISE ($99.99)

---

## 🎯 TOTAL ENDPOINTS: 28

- **3** Plan Management
- **11** Subscription Management
- **10** Feature Access
- **4** Payment Webhooks
