#!/bin/bash

# Local Kubernetes Deployment Script
# This script deploys the microservices to local Kubernetes (Docker Desktop)

set -e

echo "🚀 Starting Kubernetes Deployment..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if kubectl is installed
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed. Please install it first."
    exit 1
fi

# Check if cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Is Docker Desktop Kubernetes enabled?"
    exit 1
fi

print_status "Kubernetes cluster is accessible"

# Step 1: Create namespace
echo ""
echo "📦 Creating namespace..."
kubectl apply -f k8s/base/namespace.yaml
print_status "Namespace created"

# Step 2: Deploy infrastructure services
echo ""
echo "🗄️  Deploying infrastructure services..."
kubectl apply -f k8s/base/mysql-deployment.yaml
kubectl apply -f k8s/base/rabbitmq-deployment.yaml
kubectl apply -f k8s/base/eureka-deployment.yaml
print_status "Infrastructure services deployed"

# Step 3: Wait for infrastructure to be ready
echo ""
echo "⏳ Waiting for infrastructure services to be ready (this may take 2-3 minutes)..."

echo "  Waiting for MySQL..."
kubectl wait --for=condition=ready pod -l app=mysql -n microservices --timeout=300s
print_status "MySQL is ready"

echo "  Waiting for RabbitMQ..."
kubectl wait --for=condition=ready pod -l app=rabbitmq -n microservices --timeout=300s
print_status "RabbitMQ is ready"

echo "  Waiting for Eureka..."
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices --timeout=300s
print_status "Eureka is ready"

# Step 4: Deploy application services
echo ""
echo "🚀 Deploying application services..."
kubectl apply -f k8s/base/user-service-deployment.yaml
kubectl apply -f k8s/base/api-gateway-deployment.yaml
print_status "Application services deployed"

# Step 5: Wait for application services
echo ""
echo "⏳ Waiting for application services to be ready..."

echo "  Waiting for User Service..."
kubectl wait --for=condition=ready pod -l app=user-service -n microservices --timeout=300s
print_status "User Service is ready"

echo "  Waiting for API Gateway..."
kubectl wait --for=condition=ready pod -l app=api-gateway -n microservices --timeout=300s
print_status "API Gateway is ready"

# Step 6: Display deployment status
echo ""
echo "📊 Deployment Status:"
echo ""
kubectl get pods -n microservices
echo ""
kubectl get svc -n microservices

# Step 7: Display access information
echo ""
echo "🎉 Deployment completed successfully!"
echo ""
echo "📍 Access your services:"
echo "   Eureka Dashboard:  kubectl port-forward svc/eureka-server 8761:8761 -n microservices"
echo "                      Then open: http://localhost:8761"
echo ""
echo "   API Gateway:       kubectl port-forward svc/api-gateway 8090:8090 -n microservices"
echo "                      Then open: http://localhost:8090"
echo ""
echo "   RabbitMQ UI:       kubectl port-forward svc/rabbitmq 15672:15672 -n microservices"
echo "                      Then open: http://localhost:15672 (guest/guest)"
echo ""
echo "📝 View logs:"
echo "   kubectl logs -f deployment/user-service -n microservices"
echo "   kubectl logs -f deployment/api-gateway -n microservices"
echo ""
echo "🧹 Clean up:"
echo "   kubectl delete namespace microservices"
