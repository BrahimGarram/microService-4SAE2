# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for deploying the microservices application across different environments.

## Directory Structure

```
k8s/
├── base/                    # Base Kubernetes manifests
│   ├── namespace.yaml
│   ├── eureka-deployment.yaml
│   ├── mysql-deployment.yaml
│   ├── rabbitmq-deployment.yaml
│   ├── user-service-deployment.yaml
│   └── api-gateway-deployment.yaml
├── dev/                     # Development environment
│   └── kustomization.yaml
├── staging/                 # Staging environment
│   └── kustomization.yaml
└── prod/                    # Production environment
    └── kustomization.yaml
```

## Prerequisites

1. **Kubernetes Cluster**: Ensure you have access to a Kubernetes cluster
2. **kubectl**: Install kubectl CLI tool
3. **kustomize**: Install kustomize (or use kubectl with -k flag)
4. **Docker Images**: All services must be built and pushed to Docker registry

## Deployment Stages

### 1. Development Environment

Deploy to dev with minimal resources (1 replica per service):

```bash
kubectl apply -k k8s/dev/
```

### 2. Staging Environment

Deploy to staging with moderate resources (2 replicas per service):

```bash
kubectl apply -k k8s/staging/
```

### 3. Production Environment

Deploy to production with high availability (3 replicas per service):

```bash
kubectl apply -k k8s/prod/
```

## Manual Deployment Steps

### Step 1: Create Namespace

```bash
kubectl apply -f k8s/base/namespace.yaml
```

### Step 2: Deploy Infrastructure Services

```bash
kubectl apply -f k8s/base/mysql-deployment.yaml
kubectl apply -f k8s/base/rabbitmq-deployment.yaml
kubectl apply -f k8s/base/eureka-deployment.yaml
```

### Step 3: Wait for Infrastructure to be Ready

```bash
kubectl wait --for=condition=ready pod -l app=mysql -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices --timeout=300s
```

### Step 4: Deploy Application Services

```bash
kubectl apply -f k8s/base/user-service-deployment.yaml
kubectl apply -f k8s/base/api-gateway-deployment.yaml
```

### Step 5: Verify Deployment

```bash
kubectl get pods -n microservices
kubectl get services -n microservices
```

## CI/CD Pipeline

### GitLab CI/CD

The `.gitlab-ci.yml` file defines a multi-stage pipeline:

1. **Build**: Build Docker images for all services
2. **Test**: Run unit tests
3. **Deploy-Dev**: Auto-deploy to dev on `develop` branch
4. **Deploy-Staging**: Manual deploy to staging on `main` branch
5. **Deploy-Prod**: Manual deploy to production on `main` branch

### GitHub Actions

The `.github/workflows/deploy.yml` file provides:

- Automated builds on push to `main` or `develop`
- Parallel Docker image builds
- Environment-specific deployments
- Rollout status verification

## Environment Configuration

### Development
- 1 replica per service
- Minimal resource limits
- Auto-deployment on `develop` branch

### Staging
- 2 replicas per service
- Moderate resource limits
- Manual deployment approval required

### Production
- 3 replicas per service
- Full resource limits (512Mi-1Gi memory, 500m-1000m CPU)
- Manual deployment approval required
- High availability configuration

## Monitoring Deployment

### Check Pod Status

```bash
kubectl get pods -n microservices -w
```

### View Logs

```bash
kubectl logs -f deployment/user-service -n microservices
kubectl logs -f deployment/api-gateway -n microservices
```

### Check Service Endpoints

```bash
kubectl get svc -n microservices
```

### Access Services

For LoadBalancer services:
```bash
kubectl get svc api-gateway -n microservices
```

For port-forwarding (local testing):
```bash
kubectl port-forward svc/api-gateway 8090:8090 -n microservices
kubectl port-forward svc/eureka-server 8761:8761 -n microservices
```

## Rollback

If a deployment fails, rollback to previous version:

```bash
kubectl rollout undo deployment/user-service -n microservices
kubectl rollout undo deployment/api-gateway -n microservices
```

## Scaling

Scale deployments manually:

```bash
kubectl scale deployment user-service --replicas=5 -n microservices
```

## Secrets Management

For production, use Kubernetes secrets for sensitive data:

```bash
kubectl create secret generic mysql-secret \
  --from-literal=password=your-secure-password \
  -n microservices

kubectl create secret generic rabbitmq-secret \
  --from-literal=username=admin \
  --from-literal=password=your-secure-password \
  -n microservices
```

## Health Checks

All services include:
- **Liveness Probe**: Restarts container if unhealthy
- **Readiness Probe**: Removes from service if not ready

## Troubleshooting

### Pod not starting

```bash
kubectl describe pod <pod-name> -n microservices
kubectl logs <pod-name> -n microservices
```

### Service not accessible

```bash
kubectl get endpoints -n microservices
kubectl describe svc <service-name> -n microservices
```

### Database connection issues

```bash
kubectl exec -it <pod-name> -n microservices -- sh
# Test connection from inside pod
nc -zv mysql 3306
```

## Clean Up

Remove all resources:

```bash
kubectl delete namespace microservices
```

Or remove specific environment:

```bash
kubectl delete -k k8s/dev/
```
