# Complete Subscription Service API Endpoints

Base URL: `http://localhost:8091`

---

## 📋 SUBSCRIPTION PLAN ENDPOINTS

### 1. Get All Active Plans
**GET** `/api/subscriptions/plans`

```bash
curl http://localhost:8091/api/subscriptions/plans
```

**Response:**
```json
[
  {
    "id": 1,
    "planName": "FREE",
    "description": "Basic access for new freelancers",
    "price": 0.00,
    "billingCycle": "MONTHLY",
    "maxBidsPerMonth": 5,
    "commissionRate": 10.00,
    "hasAnalyticsAccess": false,
    "hasProfileBoost": false,
    "hasPrioritySupport": false,
    "hasFeaturedListing": false,
    "maxProjects": 3,
    "isActive": true
  }
]
```

---

### 2. Get Plan by ID
**GET** `/api/subscriptions/plans/{planId}`

```bash
curl http://localhost:8091/api/subscriptions/plans/1
curl http://localhost:8091/api/subscriptions/plans/2
curl http://localhost:8091/api/subscriptions/plans/3
curl http://localhost:8091/api/subscriptions/plans/4
```

---

### 3. Create New Plan (Admin)
**POST** `/api/subscriptions/plans`

```bash
curl -X POST http://localhost:8091/api/subscriptions/plans \
  -H "Content-Type: application/json" \
  -d "{
    \"planName\": \"PREMIUM\",
    \"description\": \"Premium plan for agencies\",
    \"price\": 49.99,
    \"billingCycle\": \"MONTHLY\",
    \"maxBidsPerMonth\": 150,
    \"commissionRate\": 4.5,
    \"hasAnalyticsAccess\": true,
    \"hasProfileBoost\": true,
    \"hasPrioritySupport\": true,
    \"hasFeaturedListing\": true,
    \"maxProjects\": 75,
    \"isActive\": true
  }"
```

---

## 💳 SUBSCRIPTION MANAGEMENT ENDPOINTS

### 4. Create Subscription
**POST** `/api/subscriptions/create`

```bash
# User 1 subscribes to FREE plan
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"planId\": 1, \"autoRenew\": true}"

# User 2 subscribes to BASIC plan
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 2, \"planId\": 2, \"autoRenew\": true}"

# User 3 subscribes to PRO plan
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 3, \"planId\": 3, \"autoRenew\": false}"

# User 4 subscribes to ENTERPRISE plan
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 4, \"planId\": 4, \"autoRenew\": true}"
```

**Response:**
```json
{
  "subscription": {
    "id": 1,
    "userId": 1,
    "plan": {...},
    "status": "PENDING_PAYMENT",
    "startDate": "2026-04-13T...",
    "endDate": "2026-05-13T...",
    "autoRenew": true
  },
  "paymentUrl": "http://payment-gateway.com/pay?amount=0.00&subscriptionId=1",
  "message": "Subscription created. Please complete payment."
}
```

---

### 5. Get Active Subscription for User
**GET** `/api/subscriptions/user/{userId}/active`

```bash
curl http://localhost:8091/api/subscriptions/user/1/active
curl http://localhost:8091/api/subscriptions/user/2/active
curl http://localhost:8091/api/subscriptions/user/3/active
```

**Response:**
```json
{
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
  "bidsUsedThisMonth": 0,
  "autoRenew": true
}
```

---

### 6. Get User Subscription History
**GET** `/api/subscriptions/user/{userId}/history`

```bash
curl http://localhost:8091/api/subscriptions/user/1/history
curl http://localhost:8091/api/subscriptions/user/2/history
```

**Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "plan": {...},
    "status": "ACTIVE",
    "startDate": "2026-04-13T...",
    "endDate": "2026-05-13T..."
  },
  {
    "id": 2,
    "userId": 1,
    "plan": {...},
    "status": "CANCELLED",
    "startDate": "2026-03-13T...",
    "endDate": "2026-04-13T..."
  }
]
```

---

### 7. Cancel Subscription
**POST** `/api/subscriptions/cancel/{userId}`

```bash
# Cancel with reason
curl -X POST "http://localhost:8091/api/subscriptions/cancel/1?reason=Too%20expensive"

# Cancel without reason
curl -X POST http://localhost:8091/api/subscriptions/cancel/2
```

**Response:**
```json
{
  "message": "Subscription cancelled successfully",
  "subscription": {
    "id": 1,
    "status": "CANCELLED",
    "cancelledAt": "2026-04-13T...",
    "cancellationReason": "Too expensive"
  }
}
```

---

### 8. Change Plan (Upgrade/Downgrade)
**POST** `/api/subscriptions/change-plan`

```bash
# Upgrade from BASIC to PRO (immediate)
curl -X POST http://localhost:8091/api/subscriptions/change-plan \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"newPlanId\": 3, \"immediate\": true}"

# Downgrade from PRO to BASIC (at end of period)
curl -X POST http://localhost:8091/api/subscriptions/change-plan \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 2, \"newPlanId\": 2, \"immediate\": false}"

# Upgrade to ENTERPRISE
curl -X POST http://localhost:8091/api/subscriptions/change-plan \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 3, \"newPlanId\": 4, \"immediate\": true}"
```

**Response:**
```json
{
  "message": "Plan changed successfully",
  "subscription": {
    "id": 1,
    "userId": 1,
    "plan": {
      "id": 3,
      "planName": "PRO"
    },
    "status": "ACTIVE"
  }
}
```

---

## 🎯 FEATURE ACCESS ENDPOINTS

### 9. Validate Feature Access
**POST** `/api/subscriptions/validate-access`

```bash
# Check BID access
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"featureName\": \"BID\"}"

# Check ANALYTICS access
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"featureName\": \"ANALYTICS\"}"

# Check PROFILE_BOOST access
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"featureName\": \"PROFILE_BOOST\"}"

# Check COMMISSION rate
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"featureName\": \"COMMISSION\"}"
```

**Response:**
```json
{
  "hasAccess": true,
  "message": "Bid allowed",
  "remainingBids": 20,
  "commissionRate": null
}
```

---

### 10. Check if User Can Place Bid
**GET** `/api/subscriptions/user/{userId}/can-bid`

```bash
curl http://localhost:8091/api/subscriptions/user/1/can-bid
curl http://localhost:8091/api/subscriptions/user/2/can-bid
curl http://localhost:8091/api/subscriptions/user/3/can-bid
```

**Response:**
```json
{
  "canBid": true
}
```

---

### 11. Increment Bid Usage
**POST** `/api/subscriptions/user/{userId}/increment-bid`

```bash
# User 1 places a bid
curl -X POST http://localhost:8091/api/subscriptions/user/1/increment-bid

# User 2 places a bid
curl -X POST http://localhost:8091/api/subscriptions/user/2/increment-bid
```

**Response:**
```json
{
  "message": "Bid usage incremented"
}
```

---

## 💰 PAYMENT WEBHOOK ENDPOINTS

### 12. Payment Webhook - Success
**POST** `/api/subscriptions/webhook/payment`

```bash
# Successful payment for subscription 1
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{
    \"transactionId\": \"TXN123456\",
    \"subscriptionId\": 1,
    \"status\": \"SUCCESS\",
    \"amount\": 9.99,
    \"paymentMethod\": \"CREDIT_CARD\"
  }"

# Successful payment for subscription 2
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{
    \"transactionId\": \"TXN789012\",
    \"subscriptionId\": 2,
    \"status\": \"SUCCESS\",
    \"amount\": 29.99,
    \"paymentMethod\": \"PAYPAL\"
  }"
```

---

### 13. Payment Webhook - Failed
**POST** `/api/subscriptions/webhook/payment`

```bash
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{
    \"transactionId\": \"TXN999999\",
    \"subscriptionId\": 1,
    \"status\": \"FAILED\",
    \"amount\": 9.99,
    \"paymentMethod\": \"CREDIT_CARD\"
  }"
```

**Response:**
```json
{
  "message": "Webhook processed successfully"
}
```

---

## 🧪 COMPLETE TEST FLOW

### Test Scenario 1: New User Subscription Flow
```bash
# 1. Get available plans
curl http://localhost:8091/api/subscriptions/plans

# 2. User 100 creates subscription to BASIC plan
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 100, \"planId\": 2, \"autoRenew\": true}"

# 3. Simulate successful payment
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{\"transactionId\": \"TXN100\", \"subscriptionId\": 1, \"status\": \"SUCCESS\", \"amount\": 9.99, \"paymentMethod\": \"CARD\"}"

# 4. Verify active subscription
curl http://localhost:8091/api/subscriptions/user/100/active

# 5. Check bid permission
curl http://localhost:8091/api/subscriptions/user/100/can-bid

# 6. Place 3 bids
curl -X POST http://localhost:8091/api/subscriptions/user/100/increment-bid
curl -X POST http://localhost:8091/api/subscriptions/user/100/increment-bid
curl -X POST http://localhost:8091/api/subscriptions/user/100/increment-bid

# 7. Check remaining bids
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 100, \"featureName\": \"BID\"}"
```

---

### Test Scenario 2: Upgrade Flow
```bash
# 1. User 200 starts with FREE plan
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 200, \"planId\": 1, \"autoRenew\": true}"

# 2. Activate subscription
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{\"transactionId\": \"TXN200\", \"subscriptionId\": 2, \"status\": \"SUCCESS\", \"amount\": 0, \"paymentMethod\": \"FREE\"}"

# 3. Check analytics access (should be false)
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 200, \"featureName\": \"ANALYTICS\"}"

# 4. Upgrade to PRO plan
curl -X POST http://localhost:8091/api/subscriptions/change-plan \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 200, \"newPlanId\": 3, \"immediate\": true}"

# 5. Check analytics access again (should be true)
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 200, \"featureName\": \"ANALYTICS\"}"

# 6. View subscription history
curl http://localhost:8091/api/subscriptions/user/200/history
```

---

### Test Scenario 3: Cancellation Flow
```bash
# 1. Create subscription
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 300, \"planId\": 3, \"autoRenew\": true}"

# 2. Activate
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{\"transactionId\": \"TXN300\", \"subscriptionId\": 3, \"status\": \"SUCCESS\", \"amount\": 29.99, \"paymentMethod\": \"CARD\"}"

# 3. Verify active
curl http://localhost:8091/api/subscriptions/user/300/active

# 4. Cancel subscription
curl -X POST "http://localhost:8091/api/subscriptions/cancel/300?reason=Found%20better%20alternative"

# 5. Try to check active (should return 404)
curl http://localhost:8091/api/subscriptions/user/300/active

# 6. View history (should show cancelled)
curl http://localhost:8091/api/subscriptions/user/300/history
```

---

## 📊 DATABASE ACCESS

### H2 Console
**URL:** http://localhost:8091/h2-console

**Connection Settings:**
- JDBC URL: `jdbc:h2:mem:freelancify-subscriptions`
- Username: `sa`
- Password: (empty)

### Useful SQL Queries
```sql
-- View all plans
SELECT * FROM SUBSCRIPTION_PLAN;

-- View all subscriptions
SELECT * FROM SUBSCRIPTION;

-- View active subscriptions
SELECT * FROM SUBSCRIPTION WHERE STATUS = 'ACTIVE';

-- View subscription history
SELECT * FROM SUBSCRIPTION_HISTORY ORDER BY CREATED_AT DESC;

-- Count subscriptions by plan
SELECT p.PLAN_NAME, COUNT(s.ID) as TOTAL
FROM SUBSCRIPTION_PLAN p
LEFT JOIN SUBSCRIPTION s ON p.ID = s.PLAN_ID
GROUP BY p.PLAN_NAME;

-- View users with most bids used
SELECT USER_ID, BIDS_USED_THIS_MONTH, p.PLAN_NAME
FROM SUBSCRIPTION s
JOIN SUBSCRIPTION_PLAN p ON s.PLAN_ID = p.ID
WHERE STATUS = 'ACTIVE'
ORDER BY BIDS_USED_THIS_MONTH DESC;
```

---

## 🔧 Testing Tools

### Using PowerShell
```powershell
# Get plans
Invoke-RestMethod -Uri "http://localhost:8091/api/subscriptions/plans" -Method Get

# Create subscription
$body = @{
    userId = 1
    planId = 2
    autoRenew = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8091/api/subscriptions/create" -Method Post -Body $body -ContentType "application/json"
```

### Using Python
```python
import requests

# Get plans
response = requests.get('http://localhost:8091/api/subscriptions/plans')
print(response.json())

# Create subscription
data = {
    'userId': 1,
    'planId': 2,
    'autoRenew': True
}
response = requests.post('http://localhost:8091/api/subscriptions/create', json=data)
print(response.json())
```

---

## 📝 Response Status Codes

- **200 OK** - Successful GET/POST request
- **201 CREATED** - Resource created successfully
- **400 BAD REQUEST** - Invalid request data
- **404 NOT FOUND** - Resource not found
- **500 INTERNAL SERVER ERROR** - Server error

---

## 🎯 Feature Names for Validation

- `BID` - Check if user can place bids
- `ANALYTICS` - Check analytics access
- `PROFILE_BOOST` - Check profile boost feature
- `COMMISSION` - Get commission rate
- `PRIORITY_SUPPORT` - Check priority support access
- `FEATURED_LISTING` - Check featured listing access

---

## 📦 Default Plans Created on Startup

| ID | Plan Name   | Price   | Bids/Month | Commission | Features                    |
|----|-------------|---------|------------|------------|-----------------------------|
| 1  | FREE        | $0.00   | 5          | 10%        | Basic access                |
| 2  | BASIC       | $9.99   | 20         | 7%         | Analytics                   |
| 3  | PRO         | $29.99  | 100        | 5%         | Analytics, Boost, Support   |
| 4  | ENTERPRISE  | $99.99  | Unlimited  | 3%         | All features                |
