# CI/CD Pipeline Overview

## 📂 Pipeline Files in Your Project

```
Your Project Root/
├── .github/
│   └── workflows/
│       └── deploy.yml          ← GitHub Actions Pipeline (MAIN)
│
├── .gitlab-ci.yml              ← GitLab CI/CD Pipeline (ALTERNATIVE)
│
└── k8s/                        ← Kubernetes Manifests
    ├── base/                   ← Base configurations
    ├── dev/                    ← Dev environment
    ├── staging/                ← Staging environment
    └── prod/                   ← Production environment
```

---

## 🔄 Pipeline Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         PIPELINE STAGES                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  BUILD   │───▶│   TEST   │───▶│ DEPLOY   │───▶│ DEPLOY   │───▶│ DEPLOY   │
│          │    │          │    │   DEV    │    │ STAGING  │    │   PROD   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
     │               │                │                │                │
     │               │                │                │                │
     ▼               ▼                ▼                ▼                ▼
  Docker         Maven Test      Auto Deploy     Manual Approve   Manual Approve
  Build All      User Service    (develop)       (main branch)    (main branch)
  Services                        1 replica       2 replicas       3 replicas
```

---

## 📋 GitHub Actions Pipeline (`.github/workflows/deploy.yml`)

### Pipeline Stages:

#### 1️⃣ **Build and Push** (Parallel)
```yaml
Triggers: Push to main or develop branch
Jobs:
  - Build Eureka image
  - Build User Service image
  - Build API Gateway image
  - Build Complaints Service image
  
Output: Docker images pushed to Docker Hub
Tags: 
  - brahim2002/service-name:latest
  - brahim2002/service-name:<commit-sha>
```

#### 2️⃣ **Test**
```yaml
Triggers: After build completes
Jobs:
  - Run Maven tests for User Service
  - Run Maven tests for other services
  
Output: Test results
```

#### 3️⃣ **Deploy to Dev**
```yaml
Triggers: Push to develop branch (automatic)
Environment: development
Jobs:
  - Apply Kubernetes manifests
  - Update deployments with new images
  - Wait for rollout to complete
  
Replicas: 1 per service
URL: http://dev.example.com
```

#### 4️⃣ **Deploy to Staging**
```yaml
Triggers: Push to main branch (automatic)
Environment: staging
Jobs:
  - Apply Kubernetes manifests
  - Update deployments with new images
  - Wait for rollout to complete
  
Replicas: 2 per service
URL: http://staging.example.com
Approval: Required (GitHub Environment Protection)
```

#### 5️⃣ **Deploy to Production**
```yaml
Triggers: After staging deployment (manual)
Environment: production
Jobs:
  - Apply Kubernetes manifests
  - Update deployments with new images
  - Wait for rollout to complete
  - Verify deployment
  
Replicas: 3 per service
URL: http://prod.example.com
Approval: Required (GitHub Environment Protection)
```

---

## 🔍 How to View the Pipeline

### GitHub Actions

1. **Push your code to GitHub:**
   ```bash
   git add .
   git commit -m "Add Kubernetes deployment"
   git push origin main
   ```

2. **View pipeline in GitHub:**
   - Go to your repository on GitHub
   - Click the **"Actions"** tab
   - You'll see the pipeline running

3. **Pipeline visualization:**
   ```
   GitHub Repository → Actions Tab
   
   ┌─────────────────────────────────────────────┐
   │  CI/CD Pipeline                              │
   │  ├─ build-and-push (4 jobs in parallel)     │
   │  ├─ test                                     │
   │  ├─ deploy-dev (if develop branch)          │
   │  ├─ deploy-staging (if main branch)         │
   │  └─ deploy-prod (manual approval)           │
   └─────────────────────────────────────────────┘
   ```

### GitLab CI/CD

1. **Push your code to GitLab:**
   ```bash
   git remote add gitlab https://gitlab.com/YOUR_USERNAME/YOUR_REPO.git
   git push gitlab main
   ```

2. **View pipeline in GitLab:**
   - Go to your repository on GitLab
   - Click **"CI/CD"** → **"Pipelines"**
   - You'll see the pipeline running

---

## 🚀 How to Trigger the Pipeline

### Automatic Triggers

#### Deploy to Dev (Automatic)
```bash
# Work on develop branch
git checkout develop
git add .
git commit -m "New feature"
git push origin develop

# Pipeline automatically:
# 1. Builds images
# 2. Runs tests
# 3. Deploys to dev environment
```

#### Deploy to Staging (Automatic after merge)
```bash
# Merge develop to main
git checkout main
git merge develop
git push origin main

# Pipeline automatically:
# 1. Builds images
# 2. Runs tests
# 3. Deploys to staging (requires approval)
```

#### Deploy to Production (Manual)
```bash
# After staging is deployed
# Go to GitHub Actions or GitLab Pipelines
# Click "Review deployments" or "Play" button
# Approve production deployment
```

### Manual Triggers

#### GitHub Actions
```bash
# Go to Actions tab
# Select workflow
# Click "Run workflow"
# Choose branch
# Click "Run workflow" button
```

#### GitLab CI/CD
```bash
# Go to CI/CD → Pipelines
# Click "Run pipeline"
# Select branch
# Click "Run pipeline" button
```

---

## 📊 Pipeline Status Indicators

### GitHub Actions
```
✓ Success  - Green checkmark
✗ Failed   - Red X
⊙ Running  - Yellow circle
⊘ Cancelled - Grey circle
```

### GitLab CI/CD
```
✓ Passed   - Green checkmark
✗ Failed   - Red X
⊙ Running  - Blue circle
⊘ Cancelled - Grey circle
```

---

## 🔐 Required Secrets/Variables

### For GitHub Actions

Go to: **Repository → Settings → Secrets and variables → Actions**

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `DOCKER_USERNAME` | Docker Hub username | brahim2002 |
| `DOCKER_PASSWORD` | Docker Hub password | your_password |
| `KUBE_CONFIG_DEV` | Kubernetes config for dev | (kubeconfig content) |
| `KUBE_CONFIG_STAGING` | Kubernetes config for staging | (kubeconfig content) |
| `KUBE_CONFIG_PROD` | Kubernetes config for prod | (kubeconfig content) |

### For GitLab CI/CD

Go to: **Repository → Settings → CI/CD → Variables**

| Variable Name | Description | Protected | Masked |
|---------------|-------------|-----------|--------|
| `DOCKER_USER` | Docker Hub username | No | No |
| `DOCKER_PASSWORD` | Docker Hub password | Yes | Yes |
| `KUBE_CONFIG_DEV` | Kubernetes config for dev | No | Yes |
| `KUBE_CONFIG_STAGING` | Kubernetes config for staging | Yes | Yes |
| `KUBE_CONFIG_PROD` | Kubernetes config for prod | Yes | Yes |

---

## 📈 Monitoring Pipeline Execution

### View Logs

#### GitHub Actions
```
1. Go to Actions tab
2. Click on the running workflow
3. Click on a job (e.g., "build-and-push")
4. View real-time logs
```

#### GitLab CI/CD
```
1. Go to CI/CD → Pipelines
2. Click on the running pipeline
3. Click on a job (e.g., "build-eureka")
4. View real-time logs
```

### Check Deployment Status

```bash
# After pipeline deploys to Kubernetes
kubectl get pods -n microservices
kubectl get deployments -n microservices
kubectl rollout status deployment/user-service -n microservices
```

---

## 🐛 Troubleshooting Pipeline Issues

### Build Stage Fails
```bash
# Check Docker build logs in pipeline
# Common issues:
# - Dockerfile syntax errors
# - Missing dependencies
# - Network issues

# Fix and push again:
git add .
git commit -m "Fix build issue"
git push
```

### Test Stage Fails
```bash
# Check test logs in pipeline
# Common issues:
# - Test failures
# - Missing test dependencies
# - Configuration issues

# Run tests locally first:
cd services/backend1
mvn clean test
```

### Deploy Stage Fails
```bash
# Check deployment logs in pipeline
# Common issues:
# - Invalid kubeconfig
# - Insufficient permissions
# - Kubernetes cluster not accessible

# Verify kubeconfig:
kubectl cluster-info
kubectl get nodes
```

---

## 🎯 Next Steps

1. **Set up GitHub/GitLab repository**
2. **Configure secrets/variables**
3. **Push code to trigger pipeline**
4. **Monitor pipeline execution**
5. **Approve staging/production deployments**

---

## 📞 Quick Commands

```bash
# View pipeline files
ls -la .github/workflows/
cat .gitlab-ci.yml

# Test pipeline locally (GitHub Actions)
# Install act: https://github.com/nektos/act
act -l  # List workflows
act push  # Run push event

# Validate GitLab CI/CD
# Install gitlab-ci-local: https://github.com/firecow/gitlab-ci-local
gitlab-ci-local --list
gitlab-ci-local build-eureka
```

---

## 📚 Additional Resources

- GitHub Actions Docs: https://docs.github.com/en/actions
- GitLab CI/CD Docs: https://docs.gitlab.com/ee/ci/
- Kubernetes Docs: https://kubernetes.io/docs/
- Docker Hub: https://hub.docker.com
