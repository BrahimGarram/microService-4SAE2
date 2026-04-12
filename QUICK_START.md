# Quick Start Guide - Kubernetes Deployment

## 🚀 Deploy in 5 Minutes

### Prerequisites Check
```bash
# 1. Check Docker Desktop is running
docker ps

# 2. Check Kubernetes is enabled
kubectl get nodes
# Should show: docker-desktop   Ready
```

### Option 1: Automated Deployment (Recommended)

**Windows (PowerShell):**
```powershell
.\deploy-local.ps1
```

**Linux/Mac:**
```bash
chmod +x deploy-local.sh
./deploy-local.sh
```

### Option 2: Manual Deployment

```bash
# 1. Create namespace
kubectl apply -f k8s/base/namespace.yaml

# 2. Deploy infrastructure (MySQL, RabbitMQ, Eureka)
kubectl apply -f k8s/base/mysql-deployment.yaml
kubectl apply -f k8s/base/rabbitmq-deployment.yaml
kubectl apply -f k8s/base/eureka-deployment.yaml

# 3. Wait 2-3 minutes, then deploy services
kubectl apply -f k8s/base/user-service-deployment.yaml
kubectl apply -f k8s/base/api-gateway-deployment.yaml

# 4. Check status
kubectl get pods -n microservices
```

### Option 3: Deploy with Kustomize

```bash
# Deploy to dev environment
kubectl apply -k k8s/dev/
```

---

## 📍 Access Services

### Port Forward Commands

Open **3 separate terminals** and run:

**Terminal 1 - Eureka:**
```bash
kubectl port-forward svc/eureka-server 8761:8761 -n microservices
```
Open: http://localhost:8761

**Terminal 2 - API Gateway:**
```bash
kubectl port-forward svc/api-gateway 8090:8090 -n microservices
```
Open: http://localhost:8090

**Terminal 3 - RabbitMQ:**
```bash
kubectl port-forward svc/rabbitmq 15672:15672 -n microservices
```
Open: http://localhost:15672 (guest/guest)

---

## 🔍 Monitor Deployment

### Check Everything
```bash
# All resources
kubectl get all -n microservices

# Just pods
kubectl get pods -n microservices

# Watch pods in real-time
kubectl get pods -n microservices -w
```

### View Logs
```bash
# User Service logs
kubectl logs -f deployment/user-service -n microservices

# API Gateway logs
kubectl logs -f deployment/api-gateway -n microservices

# All logs from a service
kubectl logs -l app=user-service -n microservices --tail=100
```

---

## 🧪 Test Deployment

```bash
# 1. Port forward API Gateway (in separate terminal)
kubectl port-forward svc/api-gateway 8090:8090 -n microservices

# 2. Test health endpoint
curl http://localhost:8090/actuator/health

# 3. Port forward Eureka (in separate terminal)
kubectl port-forward svc/eureka-server 8761:8761 -n microservices

# 4. Check registered services
curl http://localhost:8761/eureka/apps
```

---

## 🔄 Update Deployment

### Update Image
```bash
# Build new image
docker build -t brahim2002/user-service:v2 ./services/backend1

# Push to registry
docker push brahim2002/user-service:v2

# Update deployment
kubectl set image deployment/user-service \
  user-service=brahim2002/user-service:v2 \
  -n microservices

# Watch rollout
kubectl rollout status deployment/user-service -n microservices
```

### Rollback
```bash
# Rollback to previous version
kubectl rollout undo deployment/user-service -n microservices
```

---

## 📈 Scale Services

```bash
# Scale user-service to 3 replicas
kubectl scale deployment user-service --replicas=3 -n microservices

# Verify
kubectl get pods -n microservices -l app=user-service
```

---

## 🧹 Clean Up

### Remove Everything
```bash
kubectl delete namespace microservices
```

### Remove Specific Service
```bash
kubectl delete deployment user-service -n microservices
kubectl delete svc user-service -n microservices
```

---

## 🐛 Troubleshooting

### Pod Not Starting
```bash
# Check pod details
kubectl describe pod <pod-name> -n microservices

# Check logs
kubectl logs <pod-name> -n microservices

# Check events
kubectl get events -n microservices --sort-by='.lastTimestamp'
```

### Service Not Accessible
```bash
# Check service endpoints
kubectl get endpoints -n microservices

# Check service details
kubectl describe svc <service-name> -n microservices
```

### Database Connection Issues
```bash
# Get MySQL pod name
kubectl get pods -n microservices -l app=mysql

# Connect to MySQL pod
kubectl exec -it <mysql-pod-name> -n microservices -- mysql -uroot -proot

# Test connection from app pod
kubectl exec -it <app-pod-name> -n microservices -- sh
nc -zv mysql 3306
```

---

## 📚 Common Commands

```bash
# Get cluster info
kubectl cluster-info

# Get all namespaces
kubectl get namespaces

# Get all resources in namespace
kubectl get all -n microservices

# Describe resource
kubectl describe <resource-type> <resource-name> -n microservices

# Execute command in pod
kubectl exec -it <pod-name> -n microservices -- sh

# Copy file from pod
kubectl cp microservices/<pod-name>:/path/to/file ./local-file

# View resource usage
kubectl top pods -n microservices
kubectl top nodes
```

---

## 🎯 Next Steps

1. **Set up CI/CD**: Follow DEPLOYMENT_GUIDE.md
2. **Configure monitoring**: Install Prometheus & Grafana
3. **Add ingress**: Set up Nginx Ingress Controller
4. **Enable HTTPS**: Configure cert-manager

---

## 📞 Need Help?

1. Check full guide: `DEPLOYMENT_GUIDE.md`
2. View logs: `kubectl logs -f deployment/<service> -n microservices`
3. Check events: `kubectl get events -n microservices`
4. Describe pod: `kubectl describe pod <pod-name> -n microservices`
