# Setup GitHub Actions Pipeline for microService-4SAE2

## 📍 Your Repository
**URL:** https://github.com/BrahimGarram/microService-4SAE2

---

## 🚀 Step-by-Step Setup Guide

### Step 1: Push Pipeline Files to GitHub

The pipeline files are already created in your local project. Now push them to GitHub:

```bash
# Make sure you're in the project directory
cd "C:\Users\lenovo\Downloads\microService-4SAE2-main\microService-4SAE2-main"

# Check current git status
git status

# Add all new files (including pipeline files)
git add .github/
git add k8s/
git add .gitlab-ci.yml
git add deploy-local.ps1
git add deploy-local.sh
git add *.md

# Commit the changes
git commit -m "Add Kubernetes CI/CD pipeline with multi-stage deployment"

# Push to main branch
git push origin main

# Create and push develop branch
git checkout -b develop
git push origin develop

# Go back to main
git checkout main
```

---

### Step 2: Configure GitHub Secrets

1. **Go to your repository settings:**
   - Open: https://github.com/BrahimGarram/microService-4SAE2/settings/secrets/actions

2. **Click "New repository secret"**

3. **Add these secrets one by one:**

#### Secret 1: DOCKER_USERNAME
```
Name: DOCKER_USERNAME
Value: brahim2002
```
Click "Add secret"

#### Secret 2: DOCKER_PASSWORD
```
Name: DOCKER_PASSWORD
Value: [Your Docker Hub password]
```
Click "Add secret"

#### Secret 3: KUBE_CONFIG_DEV
```bash
# First, get your kubeconfig
kubectl config view --raw --minify

# Copy the entire output
```

```
Name: KUBE_CONFIG_DEV
Value: [Paste the kubeconfig output here]
```
Click "Add secret"

---

### Step 3: Enable GitHub Actions

1. **Go to Actions tab:**
   - Open: https://github.com/BrahimGarram/microService-4SAE2/actions

2. **Enable workflows** (if prompted)

3. **You should see:** "CI/CD Pipeline" workflow

---

### Step 4: Configure GitHub Environments

1. **Go to Settings → Environments:**
   - Open: https://github.com/BrahimGarram/microService-4SAE2/settings/environments

2. **Create "development" environment:**
   - Click "New environment"
   - Name: `development`
   - Click "Configure environment"
   - Add environment URL: `http://dev.example.com` (optional)
   - Click "Save protection rules"

3. **Create "staging" environment:**
   - Click "New environment"
   - Name: `staging`
   - Click "Configure environment"
   - Check "Required reviewers"
   - Add yourself as reviewer
   - Add environment URL: `http://staging.example.com` (optional)
   - Click "Save protection rules"

4. **Create "production" environment:**
   - Click "New environment"
   - Name: `production`
   - Click "Configure environment"
   - Check "Required reviewers"
   - Add yourself as reviewer
   - Add environment URL: `http://prod.example.com` (optional)
   - Click "Save protection rules"

---

### Step 5: Test the Pipeline

#### Test 1: Deploy to Dev (Automatic)

```bash
# Switch to develop branch
git checkout develop

# Make a small change
echo "# Pipeline Test" >> README.md

# Commit and push
git add README.md
git commit -m "Test CI/CD pipeline - deploy to dev"
git push origin develop
```

**Watch the pipeline:**
1. Go to: https://github.com/BrahimGarram/microService-4SAE2/actions
2. You should see a new workflow run
3. Click on it to see the progress

**Expected stages:**
- ✓ build-and-push (builds all 4 services)
- ✓ test (runs tests)
- ✓ deploy-dev (deploys to dev environment)

#### Test 2: Deploy to Staging (Manual Approval)

```bash
# Switch to main branch
git checkout main

# Merge develop
git merge develop

# Push to main
git push origin main
```

**Watch and approve:**
1. Go to: https://github.com/BrahimGarram/microService-4SAE2/actions
2. Click on the running workflow
3. When it reaches "deploy-staging", click "Review deployments"
4. Check "staging" and click "Approve and deploy"

---

## 🔍 Verify Pipeline is Working

### Check GitHub Actions Tab

Go to: https://github.com/BrahimGarram/microService-4SAE2/actions

You should see:
```
┌─────────────────────────────────────────┐
│ All workflows                            │
│                                          │
│ ✓ CI/CD Pipeline                        │
│   └─ Latest run: [timestamp]            │
│      ├─ ✓ build-and-push               │
│      ├─ ✓ test                          │
│      ├─ ✓ deploy-dev                    │
│      ├─ ⊙ deploy-staging (waiting)     │
│      └─ ⊙ deploy-prod (waiting)        │
└─────────────────────────────────────────┘
```

### Check Workflow File

Go to: https://github.com/BrahimGarram/microService-4SAE2/blob/main/.github/workflows/deploy.yml

You should see the pipeline configuration.

---

## 📊 Pipeline Workflow Visualization

```
Push to develop branch:
┌─────────┐
│  Push   │
│ develop │
└────┬────┘
     │
     ▼
┌─────────────────┐
│ Build & Push    │ (Parallel)
│ - Eureka        │
│ - User Service  │
│ - API Gateway   │
│ - Complaints    │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│     Test        │
│ - Maven tests   │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│   Deploy Dev    │ (Automatic)
│ - 1 replica     │
└─────────────────┘


Push to main branch:
┌─────────┐
│  Push   │
│  main   │
└────┬────┘
     │
     ▼
┌─────────────────┐
│ Build & Push    │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│     Test        │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│ Deploy Staging  │ (Manual Approval)
│ - 2 replicas    │
└────┬────────────┘
     │
     ▼
┌─────────────────┐
│  Deploy Prod    │ (Manual Approval)
│ - 3 replicas    │
└─────────────────┘
```

---

## 🎯 Quick Commands Reference

```bash
# Check current branch
git branch

# Switch to develop
git checkout develop

# Switch to main
git checkout main

# View git status
git status

# View remote URL
git remote -v

# Push all branches
git push --all origin

# View commit history
git log --oneline --graph --all
```

---

## 🐛 Troubleshooting

### Issue: Pipeline not showing in Actions tab

**Solution:**
```bash
# Make sure .github/workflows/deploy.yml exists
ls -la .github/workflows/

# Push the file
git add .github/workflows/deploy.yml
git commit -m "Add GitHub Actions workflow"
git push origin main
```

### Issue: Pipeline fails at build stage

**Solution:**
1. Check Docker Hub credentials in secrets
2. Make sure DOCKER_USERNAME = brahim2002
3. Make sure DOCKER_PASSWORD is correct

### Issue: Pipeline fails at deploy stage

**Solution:**
1. Check KUBE_CONFIG_DEV secret is set correctly
2. Verify Kubernetes cluster is accessible:
   ```bash
   kubectl cluster-info
   kubectl get nodes
   ```

### Issue: Can't push to GitHub

**Solution:**
```bash
# Check remote URL
git remote -v

# If not set, add it
git remote add origin https://github.com/BrahimGarram/microService-4SAE2.git

# Try push again
git push origin main
```

---

## 📞 Next Steps After Setup

1. **Monitor first pipeline run:**
   - https://github.com/BrahimGarram/microService-4SAE2/actions

2. **Check deployed services:**
   ```bash
   kubectl get pods -n microservices
   kubectl get svc -n microservices
   ```

3. **Access services:**
   ```bash
   kubectl port-forward svc/eureka-server 8761:8761 -n microservices
   kubectl port-forward svc/api-gateway 8090:8090 -n microservices
   ```

4. **View logs:**
   ```bash
   kubectl logs -f deployment/user-service -n microservices
   ```

---

## 🎉 Success Indicators

✅ Pipeline appears in Actions tab  
✅ Build stage completes successfully  
✅ Test stage passes  
✅ Deploy-dev stage completes  
✅ Services are running in Kubernetes  
✅ Can access services via port-forward  

---

## 📚 Useful Links

- **Your Repository:** https://github.com/BrahimGarram/microService-4SAE2
- **Actions Tab:** https://github.com/BrahimGarram/microService-4SAE2/actions
- **Settings:** https://github.com/BrahimGarram/microService-4SAE2/settings
- **Secrets:** https://github.com/BrahimGarram/microService-4SAE2/settings/secrets/actions
- **Environments:** https://github.com/BrahimGarram/microService-4SAE2/settings/environments

---

## 💡 Pro Tips

1. **Always test on develop branch first** before merging to main
2. **Use meaningful commit messages** to track deployments
3. **Monitor pipeline logs** to catch issues early
4. **Set up Slack/Email notifications** for pipeline status
5. **Use branch protection rules** to enforce code reviews

---

## 🔐 Security Best Practices

1. ✅ Never commit secrets to Git
2. ✅ Use GitHub Secrets for sensitive data
3. ✅ Enable branch protection on main
4. ✅ Require pull request reviews
5. ✅ Use environment protection rules for prod
