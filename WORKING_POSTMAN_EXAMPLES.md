# Working Postman Examples - Subscription Service

## ⚠️ IMPORTANT: Default Plans Already Exist

The service automatically creates these 4 plans on startup:
- **FREE** (ID: 1)
- **BASIC** (ID: 2)
- **PRO** (ID: 3)
- **ENTERPRISE** (ID: 4)

**You CANNOT create plans with these names again!**

---

## ✅ WORKING EXAMPLES

### 1. GET All Plans (Always Works)
```
GET http://localhost:8091/api/subscriptions/plans
```
**No body needed**

---

### 2. POST Create Plan - Use DIFFERENT Name
```
POST http://localhost:8091/api/subscriptions/plans
Content-Type: application/json

Body (raw JSON):
{
  "planName": "PLATINUM",
  "description": "Platinum plan for agencies",
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

**Other unique names you can use:**
- PLATINUM
- GOLD
- SILVER
- DIAMOND
- ULTIMATE
- STARTER
- PROFESSIONAL
- BUSINESS

---

### 3. POST Create Subscription (Always Works)
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

---

### 4. POST Payment Webhook - Activate Subscription
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

---

### 5. GET Active Subscription
```
GET http://localhost:8091/api/subscriptions/user/1/active
```
**No body needed**

---

### 6. GET Can Place Bid
```
GET http://localhost:8091/api/subscriptions/user/1/can-bid
```
**No body needed**

---

### 7. POST Increment Bid
```
POST http://localhost:8091/api/subscriptions/user/1/increment-bid
```
**No body needed**

---

### 8. POST Validate Feature Access
```
POST http://localhost:8091/api/subscriptions/validate-access
Content-Type: application/json

Body (raw JSON):
{
  "userId": 1,
  "featureName": "BID"
}
```

---

### 9. POST Change Plan
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

---

### 10. GET User History
```
GET http://localhost:8091/api/subscriptions/user/1/history
```
**No body needed**

---

### 11. POST Cancel Subscription
```
POST http://localhost:8091/api/subscriptions/cancel/1?reason=Testing
```
**No body needed**

---

## 🎯 RECOMMENDED TEST FLOW (Copy-Paste Ready)

### Step 1: View Plans
```
GET http://localhost:8091/api/subscriptions/plans
```

### Step 2: Create Subscription for User 100
```
POST http://localhost:8091/api/subscriptions/create

{
  "userId": 100,
  "planId": 2,
  "autoRenew": true
}
```

### Step 3: Activate with Payment
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

### Step 4: Check Active Subscription
```
GET http://localhost:8091/api/subscriptions/user/100/active
```

### Step 5: Check Bid Permission
```
GET http://localhost:8091/api/subscriptions/user/100/can-bid
```

### Step 6: Use a Bid
```
POST http://localhost:8091/api/subscriptions/user/100/increment-bid
```

### Step 7: Check Remaining Bids
```
POST http://localhost:8091/api/subscriptions/validate-access

{
  "userId": 100,
  "featureName": "BID"
}
```

### Step 8: Upgrade to PRO
```
POST http://localhost:8091/api/subscriptions/change-plan

{
  "userId": 100,
  "newPlanId": 3,
  "immediate": true
}
```

### Step 9: View History
```
GET http://localhost:8091/api/subscriptions/user/100/history
```

### Step 10: Cancel
```
POST http://localhost:8091/api/subscriptions/cancel/100?reason=Testing%20complete
```

---

## 🔧 POSTMAN SETUP

1. **Create New Request**
2. **Set Method** (GET or POST)
3. **Paste URL**
4. **For POST requests:**
   - Go to "Body" tab
   - Select "raw"
   - Select "JSON" from dropdown
   - Paste the JSON body
5. **Click Send**

---

## ❌ COMMON ERRORS

### Error: "Violation d'index unique"
**Problem:** Plan name already exists
**Solution:** Use a different plan name (PLATINUM, GOLD, SILVER, etc.)

### Error: "User already has an active subscription"
**Problem:** User already subscribed
**Solution:** Use a different userId or cancel existing subscription first

### Error: "No active subscription found"
**Problem:** Subscription not activated yet
**Solution:** Send payment webhook to activate subscription

### Error: "Subscription not found"
**Problem:** Wrong subscription ID
**Solution:** Check the ID returned from create subscription endpoint

---

## 📊 CHECK DATABASE

Open H2 Console to see data:
```
http://localhost:8091/h2-console

JDBC URL: jdbc:h2:mem:freelancify-subscriptions
Username: sa
Password: (empty)
```

Run this query to see all plans:
```sql
SELECT * FROM SUBSCRIPTION_PLAN;
```

Run this to see all subscriptions:
```sql
SELECT * FROM SUBSCRIPTION;
```
