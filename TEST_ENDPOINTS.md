# Subscription Service - Local Testing Guide

## Prerequisites
- Java 17 installed
- Maven installed
- No need for Docker!

## Step 1: Start the Subscription Service Locally

Open a terminal in the subscription service directory and run:

```bash
cd services/subscription-service
mvnw spring-boot:run
```

Or if you're on Windows:
```bash
cd services\subscription-service
mvnw.cmd spring-boot:run
```

The service will start on **http://localhost:8091**

## Step 2: Test Endpoints

### 1. Get All Subscription Plans
```bash
curl http://localhost:8091/api/subscriptions/plans
```

**Expected Response:** List of 4 plans (FREE, BASIC, PRO, ENTERPRISE)

---

### 2. Get Specific Plan by ID
```bash
curl http://localhost:8091/api/subscriptions/plans/1
```

---

### 3. Create a Subscription
```bash
curl -X POST http://localhost:8091/api/subscriptions/create \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"planId\": 2, \"autoRenew\": true}"
```

**Expected Response:** Subscription created with payment URL

---

### 4. Get Active Subscription for User
```bash
curl http://localhost:8091/api/subscriptions/user/1/active
```

---

### 5. Get User Subscription History
```bash
curl http://localhost:8091/api/subscriptions/user/1/history
```

---

### 6. Check if User Can Place Bid
```bash
curl http://localhost:8091/api/subscriptions/user/1/can-bid
```

---

### 7. Validate Feature Access
```bash
curl -X POST http://localhost:8091/api/subscriptions/validate-access \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"featureName\": \"BID\"}"
```

**Feature Names:** BID, ANALYTICS, PROFILE_BOOST, COMMISSION

---

### 8. Increment Bid Usage
```bash
curl -X POST http://localhost:8091/api/subscriptions/user/1/increment-bid
```

---

### 9. Cancel Subscription
```bash
curl -X POST "http://localhost:8091/api/subscriptions/cancel/1?reason=Testing"
```

---

### 10. Change Plan (Upgrade/Downgrade)
```bash
curl -X POST http://localhost:8091/api/subscriptions/change-plan \
  -H "Content-Type: application/json" \
  -d "{\"userId\": 1, \"newPlanId\": 3, \"immediate\": true}"
```

---

### 11. Payment Webhook (Simulate Payment Success)
```bash
curl -X POST http://localhost:8091/api/subscriptions/webhook/payment \
  -H "Content-Type: application/json" \
  -d "{\"transactionId\": \"TXN123\", \"subscriptionId\": 1, \"status\": \"SUCCESS\", \"amount\": 9.99, \"paymentMethod\": \"CARD\"}"
```

---

### 12. Create a New Plan (Admin)
```bash
curl -X POST http://localhost:8091/api/subscriptions/plans \
  -H "Content-Type: application/json" \
  -d "{\"planName\": \"PREMIUM\", \"description\": \"Premium plan\", \"price\": 49.99, \"billingCycle\": \"MONTHLY\", \"maxBidsPerMonth\": 200, \"commissionRate\": 4.0, \"hasAnalyticsAccess\": true, \"hasProfileBoost\": true, \"hasPrioritySupport\": true, \"hasFeaturedListing\": true, \"maxProjects\": 100, \"isActive\": true}"
```

---

## Access H2 Database Console

While the service is running, open your browser:

**URL:** http://localhost:8091/h2-console

**Settings:**
- JDBC URL: `jdbc:h2:mem:freelancify-subscriptions`
- Username: `sa`
- Password: (leave empty)

**Useful Queries:**
```sql
-- View all plans
SELECT * FROM SUBSCRIPTION_PLAN;

-- View all subscriptions
SELECT * FROM SUBSCRIPTION;

-- View subscription history
SELECT * FROM SUBSCRIPTION_HISTORY;
```

---

## Testing with Postman/Insomnia

Import this collection or create requests manually:

1. **Base URL:** `http://localhost:8091`
2. **Headers:** `Content-Type: application/json`
3. Use the endpoints above

---

## Complete Test Flow

1. **Start service** → Service initializes with 4 default plans
2. **Get plans** → Verify FREE, BASIC, PRO, ENTERPRISE exist
3. **Create subscription** → User 1 subscribes to BASIC plan
4. **Simulate payment** → Send webhook to activate subscription
5. **Check active subscription** → Verify user has active BASIC plan
6. **Check bid permission** → Should return true (20 bids available)
7. **Increment bid** → Use one bid
8. **Check bid permission again** → Should show 19 remaining
9. **Upgrade plan** → Change to PRO plan
10. **Cancel subscription** → Cancel with reason

---

## Troubleshooting

**Port already in use?**
Change the port in `application.yml`:
```yaml
server:
  port: 8092  # or any available port
```

**Maven not found?**
Install Maven or use the wrapper:
- Windows: `mvnw.cmd`
- Linux/Mac: `./mvnw`

**Need to rebuild?**
```bash
mvnw clean install
```
