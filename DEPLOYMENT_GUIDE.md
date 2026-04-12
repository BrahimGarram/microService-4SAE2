# Complete Kubernetes CI/CD Deployment Guide

This guide will walk you through setting up a complete CI/CD pipeline with Kubernetes deployment stages.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Setup](#local-setup)
3. [Kubernetes Cluster Setup](#kubernetes-cluster-setup)
4. [CI/CD Configuration](#cicd-configuration)
5. [Deploy to Kubernetes](#deploy-to-kubernetes)
6. [Verify Deployment](#verify-deployment)

---

## Prerequisites

### Required Tools

1. **Docker Desktop** (already installed)
   - Enable Kubernetes in Docker Desktop settings
   - Settings → Kubernetes → Enable Kubernetes

2. **kubectl** - Kubernetes CLI
   ```bash
   # Windows (using Chocolatey)
   choco install kubernetes-cli
   
   # Or download from: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
   ```

3. **Git** (for version control)
   ```bash
   git --version
   ```

4. **Docker Hub Account**
   - Sign up at https://hub.docker.com
   - Note your username (currently: brahim2002)

---

## Local Setup

### Step 1: Enable Kubernetes in Docker Desktop

1. Open Docker Desktop
2. Go to Settings (gear icon)
3. Click on "Kubernetes" tab
4. Check "Enable Kubernetes"
5. Click "Apply & Restart"
6. Wait for Kubernetes to start (green indicator)

### Step 2: Verify Kubernetes Installation

```bash
# Check kubectl is installed
kubectl version --client

# Check cluster is running
kubectl cluster-info

# Check nodes
kubectl get nodes
```

You should see:
```
NAME             STATUS   ROLES           AGE   VERSION
docker-desktop   Ready    control-plane   1m    v1.xx.x
```

### Step 3: Install Kustomize (Optional but Recommended)

```bash
# Windows (using Chocolatey)
choco install kustomize

# Or download from: https://kubectl.docs.kubernetes.io/installation/kustomize/
```

---

## Kubernetes Cluster Setup

### Option A: Local Development (Docker Desktop Kubernetes)

**Already configured!** Docker Desktop Kubernetes is ready to use.

### Option B: Cloud Kubernetes (For Staging/Production)

Choose one of these providers:

#### Google Kubernetes Engine (GKE)
```bash
# Install gcloud CLI
# https://cloud.google.com/sdk/docs/install

# Create cluster
gcloud container clusters create microservices-cluster \
  --zone us-central1-a \
  --num-nodes 3

# Get credentials
gcloud container clusters get-credentials microservices-cluster --zone us-central1-a
```

#### Amazon EKS
```bash
# Install eksctl
# https://eksctl.io/installation/

# Create cluster
eksctl create cluster \
  --name microservices-cluster \
  --region us-east-1 \
  --nodes 3

# Update kubeconfig
aws eks update-kubeconfig --name microservices-cluster --region us-east-1
```

#### Azure AKS
```bash
# Install Azure CLI
# https://docs.microsoft.com/en-us/cli/azure/install-azure-cli

# Create resource group
az group create --name microservices-rg --location eastus

# Create cluster
az aks create \
  --resource-group microservices-rg \
  --name microservices-cluster \
  --node-count 3 \
  --enable-addons monitoring \
  --generate-ssh-keys

# Get credentials
az aks get-credentials --resource-group microservices-rg --name microservices-cluster
```

---

## CI/CD Configuration

### Option 1: GitHub Actions Setup

#### Step 1: Push Code to GitHub

```bash
# Initialize git (if not already done)
git init

# Add remote repository
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git

# Add all files
git add .

# Commit
git commit -m "Initial commit with Kubernetes deployment"

# Push to main branch
git push -u origin main

# Create develop branch
git checkout -b develop
git push -u origin develop
```

#### Step 2: Configure GitHub Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**

Add these secrets:

**DOCKER_USERNAME**
```
Value: brahim2002
```

**DOCKER_PASSWORD**
```
Value: your_docker_hub_password
```

**KUBE_CONFIG_DEV** (for development)
```bash
# Get your kubeconfig
kubectl config view --raw --minify

# Copy the output and paste as secret value
```

**KUBE_CONFIG_STAGING** (for staging - if using cloud)
```bash
# Switch to staging cluster context
kubectl config use-context staging-cluster-context

# Get kubeconfig
kubectl config view --raw --minify
```

**KUBE_CONFIG_PROD** (for production - if using cloud)
```bash
# Switch to production cluster context
kubectl config use-context prod-cluster-context

# Get kubeconfig
kubectl config view --raw --minify
```

#### Step 3: Create GitHub Environments

1. Go to **Settings** → **Environments**
2. Click **New environment**
3. Create three environments:
   - `development`
   - `staging` (add protection rules: require approval)
   - `production` (add protection rules: require approval)

### Option 2: GitLab CI/CD Setup

#### Step 1: Push Code to GitLab

```bash
# Add GitLab remote
git remote add gitlab https://gitlab.com/YOUR_USERNAME/YOUR_REPO.git

# Push to GitLab
git push -u gitlab main
git push -u gitlab develop
```

#### Step 2: Configure GitLab CI/CD Variables

1. Go to your GitLab project
2. Click **Settings** → **CI/CD** → **Variables**
3. Click **Add variable**

Add these variables:

| Key | Value | Protected | Masked |
|-----|-------|-----------|--------|
| DOCKER_USER | brahim2002 | No | No |
| DOCKER_PASSWORD | your_docker_hub_password | Yes | Yes |
| KUBE_CONFIG_DEV | (kubeconfig content) | No | Yes |
| KUBE_CONFIG_STAGING | (kubeconfig content) | Yes | Yes |
| KUBE_CONFIG_PROD | (kubeconfig content) | Yes | Yes |

#### Step 3: Configure GitLab Environments

1. Go to **Deployments** → **Environments**
2. Environments will be created automatically on first deployment
3. Configure protection rules:
   - Settings → CI/CD → Protected Environments
   - Add `staging` and `production` as protected

---

## Deploy to Kubernetes

### Manual Deployment (Local Testing)

#### Step 1: Build Docker Images

```bash
# Build all images
docker-compose build

# Or build individually
docker build -t brahim2002/eureka:latest ./Eureka
docker build -t brahim2002/user-service:latest ./services/backend1
docker build -t brahim2002/api-gateway:latest ./ApiGateway4sae2
docker build -t brahim2002/complaints-service:latest ./services/complaints-service
```

#### Step 2: Push Images to Docker Hub

```bash
# Login to Docker Hub
docker login

# Push images
docker push brahim2002/eureka:latest
docker push brahim2002/user-service:latest
docker push brahim2002/api-gateway:latest
docker push brahim2002/complaints-service:latest
```

#### Step 3: Deploy to Local Kubernetes

```bash
# Create namespace
kubectl apply -f k8s/base/namespace.yaml

# Deploy infrastructure services
kubectl apply -f k8s/base/mysql-deployment.yaml
kubectl apply -f k8s/base/rabbitmq-deployment.yaml
kubectl apply -f k8s/base/eureka-deployment.yaml

# Wait for infrastructure to be ready (2-3 minutes)
kubectl wait --for=condition=ready pod -l app=mysql -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=eureka-server -n microservices --timeout=300s

# Deploy application services
kubectl apply -f k8s/base/user-service-deployment.yaml
kubectl apply -f k8s/base/api-gateway-deployment.yaml

# Wait for application services
kubectl wait --for=condition=ready pod -l app=user-service -n microservices --timeout=300s
kubectl wait --for=condition=ready pod -l app=api-gateway -n microservices --timeout=300s
```

#### Step 4: Alternative - Deploy Using Kustomize

```bash
# Deploy to dev environment
kubectl apply -k k8s/dev/

# Or deploy to staging
kubectl apply -k k8s/staging/

# Or deploy to production
kubectl apply -k k8s/prod/
```

### Automated Deployment (CI/CD)

#### Using GitHub Actions

1. **Push to develop branch** (auto-deploys to dev):
   ```bash
   git checkout develop
   git add .
   git commit -m "Deploy to dev"
   git push origin develop
   ```

2. **Push to main branch** (deploys to staging, then prod with approval):
   ```bash
   git checkout main
   git merge develop
   git push origin main
   ```

3. **Monitor deployment**:
   - Go to GitHub → Actions tab
   - Watch the pipeline progress
   - Approve staging/production deployments when prompted

#### Using GitLab CI/CD

1. **Push to develop branch**:
   ```bash
   git push gitlab develop
   ```

2. **Push to main branch**:
   ```bash
   git push gitlab main
   ```

3. **Monitor and approve**:
   - Go to GitLab → CI/CD → Pipelines
   - Click on the running pipeline
   - Approve manual stages (staging/production)

---

## Verify Deployment

### Step 1: Check Pods Status

```bash
# List all pods
kubectl get pods -n microservices

# Watch pods in real-time
kubectl get pods -n microservices -w

# Expected output:
# NAME                            READY   STATUS    RESTARTS   AGE
# eureka-server-xxx               1/1     Running   0          5m
# mysql-xxx                       1/1     Running   0          5m
# rabbitmq-xxx                    1/1     Running   0          5m
# user-service-xxx                1/1     Running   0          3m
# api-gateway-xxx                 1/1     Running   0          3m
```

### Step 2: Check Services

```bash
# List all services
kubectl get svc -n microservices

# Expected output:
# NAME            TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)
# eureka-server   ClusterIP      10.x.x.x        <none>        8761/TCP
# mysql           ClusterIP      10.x.x.x        <none>        3306/TCP
# rabbitmq        ClusterIP      10.x.x.x        <none>        5672/TCP,15672/TCP
# user-service    ClusterIP      10.x.x.x        <none>        8081/TCP
# api-gateway     LoadBalancer   10.x.x.x        localhost     8090/TCP
```

### Step 3: Access Services

#### Port Forward (Local Access)

```bash
# Access Eureka Dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n microservices
# Open: http://localhost:8761

# Access API Gateway
kubectl port-forward svc/api-gateway 8090:8090 -n microservices
# Open: http://localhost:8090

# Access RabbitMQ Management
kubectl port-forward svc/rabbitmq 15672:15672 -n microservices
# Open: http://localhost:15672 (guest/guest)
```

#### LoadBalancer (Cloud)

```bash
# Get external IP
kubectl get svc api-gateway -n microservices

# Access via external IP
curl http://<EXTERNAL-IP>:8090
```

### Step 4: View Logs

```bash
# View logs for a specific pod
kubectl logs -f deployment/user-service -n microservices

# View logs for all pods with label
kubectl logs -l app=user-service -n microservices --tail=50

# View logs from previous container (if crashed)
kubectl logs deployment/user-service -n microservices --previous
```

### Step 5: Test the Application

```bash
# Test API Gateway health
curl http://localhost:8090/actuator/health

# Test User Service through Gateway
curl http://localhost:8090/api/users

# Check Eureka registered services
curl http://localhost:8761/eureka/apps
```

---

## Common Operations

### Scale Deployment

```bash
# Scale user-service to 3 replicas
kubectl scale deployment user-service --replicas=3 -n microservices

# Verify scaling
kubectl get pods -n microservices -l app=user-service
```

### Update Deployment

```bash
# Update image
kubectl set image deployment/user-service \
  user-service=brahim2002/user-service:v2.0 \
  -n microservices

# Check rollout status
kubectl rollout status deployment/user-service -n microservices
```

### Rollback Deployment

```bash
# Rollback to previous version
kubectl rollout undo deployment/user-service -n microservices

# Rollback to specific revision
kubectl rollout undo deployment/user-service --to-revision=2 -n microservices

# View rollout history
kubectl rollout history deployment/user-service -n microservices
```

### Debug Pod Issues

```bash
# Describe pod
kubectl describe pod <pod-name> -n microservices

# Get pod events
kubectl get events -n microservices --sort-by='.lastTimestamp'

# Execute command in pod
kubectl exec -it <pod-name> -n microservices -- sh

# Check pod resource usage
kubectl top pod -n microservices
```

---

## Troubleshooting

### Issue: Pods stuck in Pending

```bash
# Check pod events
kubectl describe pod <pod-name> -n microservices

# Common causes:
# - Insufficient resources
# - PVC not bound
# - Image pull errors
```

### Issue: Pods CrashLoopBackOff

```bash
# View logs
kubectl logs <pod-name> -n microservices --previous

# Common causes:
# - Application errors
# - Missing environment variables
# - Database connection issues
```

### Issue: Service not accessible

```bash
# Check endpoints
kubectl get endpoints -n microservices

# Check service
kubectl describe svc <service-name> -n microservices

# Test from another pod
kubectl run test-pod --image=busybox -it --rm -n microservices -- sh
wget -O- http://user-service:8081/actuator/health
```

---

## Clean Up

### Remove Specific Deployment

```bash
# Delete by environment
kubectl delete -k k8s/dev/

# Delete specific resources
kubectl delete deployment user-service -n microservices
kubectl delete svc user-service -n microservices
```

### Remove Everything

```bash
# Delete entire namespace (removes all resources)
kubectl delete namespace microservices
```

---

## Next Steps

1. **Set up monitoring**: Install Prometheus & Grafana
2. **Configure ingress**: Set up Nginx Ingress Controller
3. **Add SSL/TLS**: Configure cert-manager for HTTPS
4. **Implement autoscaling**: Configure HPA (Horizontal Pod Autoscaler)
5. **Set up logging**: Deploy ELK stack or use cloud logging

---

## Quick Reference Commands

```bash
# Get all resources
kubectl get all -n microservices

# Watch deployments
kubectl get deployments -n microservices -w

# Port forward multiple services
kubectl port-forward svc/eureka-server 8761:8761 -n microservices &
kubectl port-forward svc/api-gateway 8090:8090 -n microservices &
kubectl port-forward svc/rabbitmq 15672:15672 -n microservices &

# View cluster info
kubectl cluster-info
kubectl get nodes
kubectl top nodes

# Switch contexts (if multiple clusters)
kubectl config get-contexts
kubectl config use-context <context-name>
```

---

## Support

For issues or questions:
1. Check logs: `kubectl logs -f deployment/<service-name> -n microservices`
2. Check events: `kubectl get events -n microservices`
3. Describe resources: `kubectl describe <resource-type> <resource-name> -n microservices`
