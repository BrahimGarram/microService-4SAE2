// Pipeline monitoring K8s — 6 étapes uniquement (Prometheus / Grafana via Helm).
// Prérequis agent Linux : kubectl, helm 3.
// Credential Jenkins : Secret file « kubeconfig » (ID paramétrable ci-dessous).
// Dépôt Git : configurer le job Jenkins (Pipeline from SCM) — ex. https://github.com/BrahimGarram/microService-4SAE2.git

pipeline {
  agent any

  options {
    timestamps()
    timeout(time: 45, unit: 'MINUTES')
    buildDiscarder(logRotator(numToKeepStr: '20'))
  }

  parameters {
    string(name: 'K8S_CONTEXT', defaultValue: '', description: 'Contexte kubectl (vide = défaut du kubeconfig)')
    string(name: 'MONITORING_NAMESPACE', defaultValue: 'freelance-monitoring', description: 'Namespace (voir k8s/namespace.yaml)')
    string(name: 'KUBECONFIG_CREDENTIALS_ID', defaultValue: 'kubeconfig', description: 'ID Jenkins du secret fichier kubeconfig')
    string(name: 'HELM_RELEASE_PROMETHEUS', defaultValue: 'prometheus', description: 'Release Helm Prometheus')
    string(name: 'HELM_RELEASE_GRAFANA', defaultValue: 'grafana', description: 'Release Helm Grafana')
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Setup K8s context') {
      steps {
        script {
          def kubeCtx = params.K8S_CONTEXT?.trim() ?: ''
          withCredentials([file(credentialsId: params.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
            sh """#!/usr/bin/env bash
set -euo pipefail
export KUBECONFIG
NS="${params.MONITORING_NAMESPACE}"
CTX="${kubeCtx}"
KC=(kubectl)
if [ -n "\$CTX" ]; then KC+=(--context="\$CTX"); fi
if [ -f k8s/namespace.yaml ]; then
  "\${KC[@]}" apply -f k8s/namespace.yaml
else
  echo "[WARN] k8s/namespace.yaml absent — création du namespace \$NS"
  "\${KC[@]}" create namespace "\$NS" --dry-run=client -o yaml | "\${KC[@]}" apply -f -
fi
if [ -d k8s/prometheus ] && [ "\$(find k8s/prometheus -type f 2>/dev/null | wc -l)" -gt 0 ]; then
  "\${KC[@]}" apply -f k8s/prometheus/ -n "\$NS" --recursive || true
fi
if [ -d k8s/grafana ] && [ "\$(find k8s/grafana -type f 2>/dev/null | wc -l)" -gt 0 ]; then
  "\${KC[@]}" apply -f k8s/grafana/ -n "\$NS" --recursive || true
fi
"\${KC[@]}" get ns "\$NS" -o wide
"""
          }
        }
      }
    }

    stage('Add Helm repos') {
      steps {
        sh '''#!/usr/bin/env bash
set -euo pipefail
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add grafana https://grafana.github.io/helm-charts || true
helm repo update
helm repo list
'''
      }
    }

    stage('Deploy Prometheus') {
      steps {
        script {
          def kubeCtx = params.K8S_CONTEXT?.trim() ?: ''
          withCredentials([file(credentialsId: params.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
            sh """#!/usr/bin/env bash
set -euo pipefail
export KUBECONFIG
NS="${params.MONITORING_NAMESPACE}"
REL="${params.HELM_RELEASE_PROMETHEUS}"
CTX="${kubeCtx}"
HELM_CTX=()
if [ -n "\$CTX" ]; then HELM_CTX=(--kube-context "\$CTX"); fi
if [ ! -f helm/values-prometheus.yaml ]; then
  echo "[ERROR] helm/values-prometheus.yaml manquant"
  exit 1
fi
helm "\${HELM_CTX[@]}" upgrade --install "\$REL" prometheus-community/prometheus \\
  -n "\$NS" -f helm/values-prometheus.yaml
"""
          }
        }
      }
    }

    stage('Deploy Grafana') {
      steps {
        script {
          def kubeCtx = params.K8S_CONTEXT?.trim() ?: ''
          withCredentials([file(credentialsId: params.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
            sh """#!/usr/bin/env bash
set -euo pipefail
export KUBECONFIG
NS="${params.MONITORING_NAMESPACE}"
REL="${params.HELM_RELEASE_GRAFANA}"
CTX="${kubeCtx}"
HELM_CTX=()
if [ -n "\$CTX" ]; then HELM_CTX=(--kube-context "\$CTX"); fi
if [ ! -f helm/values-grafana.yaml ]; then
  echo "[ERROR] helm/values-grafana.yaml manquant"
  exit 1
fi
helm "\${HELM_CTX[@]}" upgrade --install "\$REL" grafana/grafana \\
  -n "\$NS" -f helm/values-grafana.yaml
"""
          }
        }
      }
    }

    stage('Verify deployments') {
      steps {
        script {
          def kubeCtx = params.K8S_CONTEXT?.trim() ?: ''
          withCredentials([file(credentialsId: params.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
            sh """#!/usr/bin/env bash
set -euo pipefail
export KUBECONFIG
NS="${params.MONITORING_NAMESPACE}"
CTX="${kubeCtx}"
export KUBE_CONTEXT="${kubeCtx}"
KC=(kubectl)
if [ -n "\$CTX" ]; then KC+=(--context="\$CTX"); fi
for d in \$("\${KC[@]}" get deploy -n "\$NS" -o jsonpath='{.items[*].metadata.name}' 2>/dev/null); do
  [ -z "\$d" ] && continue
  "\${KC[@]}" rollout status "deployment/\$d" -n "\$NS" --timeout=300s
done
if [ -f scripts/verify.sh ]; then
  chmod +x scripts/verify.sh
  ./scripts/verify.sh "\$NS"
else
  echo "[INFO] scripts/verify.sh absent — vérif kubectl rollout uniquement."
fi
"""
          }
        }
      }
    }
  }

  post {
    success { echo 'Pipeline monitoring terminé avec succès.' }
    failure { echo 'Échec — voir les logs des stages.' }
    unstable { echo 'Pipeline instable.' }
  }
}
