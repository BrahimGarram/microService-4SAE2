@echo off
echo ========================================
echo Subscription Service API Tests
echo ========================================
echo.

echo [1/12] Testing: Get All Plans
curl -s http://localhost:8091/api/subscriptions/plans
echo.
echo.

echo [2/12] Testing: Get Plan by ID (Plan 1)
curl -s http://localhost:8091/api/subscriptions/plans/1
echo.
echo.

echo [3/12] Testing: Create Subscription for User 1
curl -s -X POST http://localhost:8091/api/subscriptions/create -H "Content-Type: application/json" -d "{\"userId\": 1, \"planId\": 2, \"autoRenew\": true}"
echo.
echo.

echo [4/12] Testing: Simulate Payment Success
curl -s -X POST http://localhost:8091/api/subscriptions/webhook/payment -H "Content-Type: application/json" -d "{\"transactionId\": \"TXN123\", \"subscriptionId\": 1, \"status\": \"SUCCESS\", \"amount\": 9.99, \"paymentMethod\": \"CARD\"}"
echo.
echo.

echo [5/12] Testing: Get Active Subscription for User 1
curl -s http://localhost:8091/api/subscriptions/user/1/active
echo.
echo.

echo [6/12] Testing: Get User History
curl -s http://localhost:8091/api/subscriptions/user/1/history
echo.
echo.

echo [7/12] Testing: Check if User Can Bid
curl -s http://localhost:8091/api/subscriptions/user/1/can-bid
echo.
echo.

echo [8/12] Testing: Validate BID Feature Access
curl -s -X POST http://localhost:8091/api/subscriptions/validate-access -H "Content-Type: application/json" -d "{\"userId\": 1, \"featureName\": \"BID\"}"
echo.
echo.

echo [9/12] Testing: Increment Bid Usage
curl -s -X POST http://localhost:8091/api/subscriptions/user/1/increment-bid
echo.
echo.

echo [10/12] Testing: Check Bid Permission Again
curl -s http://localhost:8091/api/subscriptions/user/1/can-bid
echo.
echo.

echo [11/12] Testing: Upgrade to PRO Plan
curl -s -X POST http://localhost:8091/api/subscriptions/change-plan -H "Content-Type: application/json" -d "{\"userId\": 1, \"newPlanId\": 3, \"immediate\": true}"
echo.
echo.

echo [12/12] Testing: Cancel Subscription
curl -s -X POST "http://localhost:8091/api/subscriptions/cancel/1?reason=Testing"
echo.
echo.

echo ========================================
echo All tests completed!
echo ========================================
pause
