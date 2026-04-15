# Subscription Service Integration with User Service

## 🎯 Overview

The subscription service is now fully integrated with the user service. Users can manage their subscriptions through the user service endpoints.

---

## 📋 NEW ENDPOINTS IN USER SERVICE

Base URL: `http://localhost:8081/api/users/subscriptions`

### 1. Get All Plans
```
GET http://localhost:8081/api/users/subscriptions/plans
```
**Auth:** Not required
**Description:** View all available subscription plans

---

### 2. Get My Subscription
```
GET http://localhost:8081/api/users/subscriptions/my-subscription
```
**Auth:** Required (JWT Token)
**Description:** Get the authenticated user's active subscription

---

### 3. Get My History
```
GET http://localhost:8081/api/users/subscriptions/my-history
```
**Auth:** Required (JWT Token)
**Description:** Get the authenticated user's subscription history

---

### 4. Subscribe
```
POST http://localhost:8081/api/users/subscriptions/subscribe

Body:
{
  "planId": 2,
  "autoRenew": true
}
```
**Auth:** Required (JWT Token)
**Description:** Create a new subscription for the authenticated user

---

### 5. Cancel Subscription
```
POST http://localhost:8081/api/users/subscriptions/cancel?reason=Too%20expensive
```
**Auth:** Required (JWT Token)
**Description:** Cancel the authenticated user's subscription

---

### 6. Change Plan
```
POST http://localhost:8081/api/users/subscriptions/change-plan?newPlanId=3&immediate=true
```
**Auth:** Required (JWT Token)
**Description:** Upgrade or downgrade the authenticated user's plan

---

### 7. Can Place Bid
```
GET http://localhost:8081/api/users/subscriptions/can-bid
```
**Auth:** Required (JWT Token)
**Description:** Check if the authenticated user can place a bid

---

### 8. Increment Bid
```
POST http://localhost:8081/api/users/subscriptions/increment-bid
```
**Auth:** Required (JWT Token)
**Description:** Increment bid usage (called after placing a bid)

---

### 9. Get User Subscription (Admin)
```
GET http://localhost:8081/api/users/subscriptions/user/1
```
**Auth:** Required (Admin JWT Token)
**Description:** Get any user's active subscription

---

## 🔧 CONFIGURATION

### User Service (application.yml)
```yaml
subscription:
  service:
    url: ${SUBSCRIPTION_SERVICE_URL:http://localhost:8091}
```

### Docker Compose
```yaml
user-service:
  environment:
    SUBSCRIPTION_SERVICE_URL: http://subscription-service:8091
```

---

## 🚀 HOW IT WORKS

1. **User Service** acts as a proxy to the **Subscription Service**
2. User authentication is handled by the User Service (Keycloak JWT)
3. User ID is extracted from the JWT token
4. Requests are forwarded to the Subscription Service with the user ID
5. Responses are returned to the client

---

## 📊 ARCHITECTURE

```
Frontend (Angular)
    ↓
API Gateway (Port 18090)
    ↓
User Service (Port 8081)
    ↓ (REST calls)
Subscription Service (Port 8091)
    ↓
H2 Database (In-Memory)
```

---

## 🧪 TESTING FLOW

### Step 1: Get Available Plans
```
GET http://localhost:8081/api/users/subscriptions/plans
```

### Step 2: Login and Get JWT Token
```
POST http://localhost:8080/realms/projetpidev/protocol/openid-connect/token

Body (x-www-form-urlencoded):
grant_type: password
client_id: your-client-id
username: user@example.com
password: password
```

### Step 3: Subscribe to a Plan
```
POST http://localhost:8081/api/users/subscriptions/subscribe
Authorization: Bearer {JWT_TOKEN}

Body:
{
  "planId": 2,
  "autoRenew": true
}
```

### Step 4: Activate Subscription (Payment Webhook)
```
POST http://localhost:8091/api/subscriptions/webhook/payment

Body:
{
  "transactionId": "TXN123",
  "subscriptionId": 1,
  "status": "SUCCESS",
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
```

### Step 5: Check Active Subscription
```
GET http://localhost:8081/api/users/subscriptions/my-subscription
Authorization: Bearer {JWT_TOKEN}
```

### Step 6: Check Bid Permission
```
GET http://localhost:8081/api/users/subscriptions/can-bid
Authorization: Bearer {JWT_TOKEN}
```

### Step 7: Place a Bid (Project Service)
```
POST http://localhost:8082/api/projects/bids
Authorization: Bearer {JWT_TOKEN}

Body:
{
  "projectId": 1,
  "amount": 500,
  "description": "I can do this"
}
```

### Step 8: Increment Bid Usage
```
POST http://localhost:8081/api/users/subscriptions/increment-bid
Authorization: Bearer {JWT_TOKEN}
```

---

## 🔐 AUTHENTICATION

All user-specific endpoints require JWT authentication:

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

The JWT token must contain a `user_id` claim that identifies the user.

---

## 🎯 INTEGRATION WITH PROJECT SERVICE

When a user places a bid on a project, the Project Service should:

1. Check if user can bid:
```java
Boolean canBid = restTemplate.getForObject(
    "http://user-service:8081/api/users/subscriptions/can-bid",
    Boolean.class
);
```

2. If allowed, create the bid

3. Increment bid usage:
```java
restTemplate.postForEntity(
    "http://user-service:8081/api/users/subscriptions/increment-bid",
    null,
    Void.class
);
```

---

## 📝 SUBSCRIPTION PLANS

| Plan | Price | Bids/Month | Commission | Features |
|------|-------|------------|------------|----------|
| FREE | $0 | 5 | 10% | Basic |
| BASIC | $9.99 | 20 | 7% | Analytics |
| PRO | $29.99 | 100 | 5% | Analytics, Boost, Support |
| ENTERPRISE | $99.99 | Unlimited | 3% | All Features |

---

## 🐛 TROUBLESHOOTING

### Error: "Unable to extract user ID from authentication"
**Solution:** Ensure JWT token contains `user_id` claim

### Error: "Failed to fetch subscription plans"
**Solution:** Check if Subscription Service is running on port 8091

### Error: "User already has an active subscription"
**Solution:** Cancel existing subscription first or use change-plan endpoint

### Error: "No active subscription found"
**Solution:** User needs to subscribe first and activate payment

---

## 🔄 API GATEWAY ROUTES

The API Gateway already routes subscription requests:

```yaml
# Direct subscription service access
/api/subscriptions/** → subscription-service:8091

# User service subscription proxy
/api/users/subscriptions/** → user-service:8081
```

---

## 📦 FILES CREATED

1. `SubscriptionDTO.java` - Subscription data transfer object
2. `SubscriptionPlanDTO.java` - Plan data transfer object
3. `CreateSubscriptionRequest.java` - Request DTO for creating subscriptions
4. `SubscriptionClientService.java` - Service to communicate with Subscription Service
5. `SubscriptionController.java` - REST controller for subscription endpoints

---

## ✅ NEXT STEPS

1. Start both services:
   - Subscription Service: Port 8091
   - User Service: Port 8081

2. Test the integration with Postman

3. Integrate with Project Service for bid management

4. Add subscription checks to other features (analytics, profile boost, etc.)

5. Implement payment gateway integration (Stripe, PayPal, etc.)
