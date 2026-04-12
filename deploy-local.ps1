# Local Kubernetes Deployment Script (PowerShell)
# This script deploys the microservices to local Kubernetes (Docker Desktop)

$ErrorActionPreference = "Stop"

Write-Host "🚀 Starting Kubernetes Deployment..." -ForegroundColor Cyan

function Print-Status {
    param($Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Print-Warning {
    param($Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Print-Error {
    param($Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

# Check if kubectl is installed
try {
    kubectl version --client | Out-Null
    Print-Status "kubectl is installed"
} catch {
    Print-Error "kubectl is not installed. Please install it first."
    exit 1
}

# Check if cluster is accessible
try {
    kubectl cluster-info | Out-Null
    Print-Status "Kubernetes cluster is accessible"
} catch {
    Print-Error "Cannot connect to Kubernetes cluster. Is Docker Desktop Kubernetes enabled?"
    exit 1
}

# Step 1: Create namespace
Write-Host ""
Write-Host "📦 Creating namespace..." -ForegroundColor Cyan
kubectl apply -f k8s/base/namespace.yaml
Print-Status "Namespace created"

# Step 2: Deploy infrastructure services
Write-Host ""
Write-Host "🗄️  Deploying infrastructure services..." -ForegroundColor Cyan
kubectl apply -f k8s/base/mysql-deployment.yaml
kubectl apply -f k8s/base/rabbitmq-deployment.yaml
kubectl apply -f k8s/base/eureka-deployment.yaml
Print-Status "Infrastructure services deployed"

# Step 3: Wait for infrastructure to be ready
Write-Host ""
Write-Host "⏳ Waiting for infrastructure services to be ready (this may take 2-3 minutes)..." -ForegroundColor Cyan

Write-Host "  Waiting for MySQL..."
kubectl wait --for=condition=ready pod -l app=mysql -n microservices --timeout=300s
Print-Status "MySQL is ready"

Write-Host "  Waiting for RabbitMQ..."
kubectl wait --for=condition=ready pod -l app=rabbitmq -n microservices --timeout=300s
Print-Status "RabbitMQ is ready"

Write-Host "  Waiting for Eureka..."
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices --timeout=300s
Print-Status "Eureka is ready"

# Step 4: Deploy application services
Write-Host ""
Write-Host "🚀 Deploying application services..." -ForegroundColor Cyan
kubectl apply -f k8s/base/user-service-deployment.yaml
kubectl apply -f k8s/base/api-gateway-deployment.yaml
Print-Status "Application services deployed"

# Step 5: Wait for application services
Write-Host ""
Write-Host "⏳ Waiting for application services to be ready..." -ForegroundColor Cyan

Write-Host "  Waiting for User Service..."
kubectl wait --for=condition=ready pod -l app=user-service -n microservices --timeout=300s
Print-Status "User Service is ready"

Write-Host "  Waiting for API Gateway..."
kubectl wait --for=condition=ready pod -l app=api-gateway -n microservices --timeout=300s
Print-Status "API Gateway is ready"

# Step 6: Display deployment status
Write-Host ""
Write-Host "📊 Deployment Status:" -ForegroundColor Cyan
Write-Host ""
kubectl get pods -n microservices
Write-Host ""
kubectl get svc -n microservices

# Step 7: Display access information
Write-Host ""
Write-Host "🎉 Deployment completed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "📍 Access your services:" -ForegroundColor Yellow
Write-Host "   Eureka Dashboard:  kubectl port-forward svc/eureka-server 8761:8761 -n microservices"
Write-Host "                      Then open: http://localhost:8761"
Write-Host ""
Write-Host "   API Gateway:       kubectl port-forward svc/api-gateway 8090:8090 -n microservices"
Write-Host "                      Then open: http://localhost:8090"
Write-Host ""
Write-Host "   RabbitMQ UI:       kubectl port-forward svc/rabbitmq 15672:15672 -n microservices"
Write-Host "                      Then open: http://localhost:15672 (guest/guest)"
Write-Host ""
Write-Host "📝 View logs:" -ForegroundColor Yellow
Write-Host "   kubectl logs -f deployment/user-service -n microservices"
Write-Host "   kubectl logs -f deployment/api-gateway -n microservices"
Write-Host ""
Write-Host "🧹 Clean up:" -ForegroundColor Yellow
Write-Host "   kubectl delete namespace microservices"
