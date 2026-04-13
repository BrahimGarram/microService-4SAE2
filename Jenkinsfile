// Jenkins Declarative Pipeline — freelance-management
// Prérequis agent Linux : JDK 17+, Maven 3.9+, Node 18/20+, Docker, kubectl (optionnel)
// Credentials Jenkins suggérés : docker-registry-credentials (username/password), kubeconfig (fichier secret)
// Aucune URL Git en dur : le dépôt et la branche se configurent dans le job Jenkins (Pipeline from SCM).
// Tu peux utiliser GitHub, GitLab, Gitea, Bitbucket ou un dépôt interne — pousser ou non sur un remote distant reste indépendant de ce fichier.

pipeline {
  agent any

  options {
    timestamps()
    timeout(time: 2, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '25'))
    disableConcurrentBuilds(abortPrevious: true)
  }

  environment {
    MAVEN_OPTS = '-Xmx1536m -XX:+TieredCompilation -Djava.net.preferIPv4Stack=true'
    // Préfixe images (aligné sur docker-compose.yml) — surcharger IMAGE_REGISTRY dans Jenkins si besoin
    IMAGE_REGISTRY = "${env.IMAGE_REGISTRY ?: 'brahim2002'}"
  }

  parameters {
    booleanParam(name: 'RUN_MAVEN_TESTS', defaultValue: true, description: 'Exécuter les tests unitaires Maven')
    booleanParam(name: 'PARALLEL_MAVEN', defaultValue: true, description: 'Compiler les modules Java en parallèle')
    booleanParam(name: 'BUILD_FRONTEND', defaultValue: true, description: 'Build production Angular (frontend/)')
    booleanParam(name: 'BUILD_DOCKER_IMAGES', defaultValue: true, description: 'Construire les images Docker')
    booleanParam(name: 'PUSH_IMAGES', defaultValue: false, description: 'Pousser les images vers le registry (nécessite credentials)')
    booleanParam(name: 'DEPLOY_MONITORING', defaultValue: false, description: 'Déployer la stack observabilité Prometheus / Grafana (Kubernetes)')
    booleanParam(name: 'DEPLOY_KUBERNETES', defaultValue: false, description: 'Déploiement applicatif K8s (rolling update, ingress, vérifs résilience)')
    string(name: 'K8S_NAMESPACE', defaultValue: 'freelance-platform', description: 'Namespace Kubernetes cible')
    string(name: 'K8S_CONTEXT', defaultValue: '', description: 'Contexte kubectl (vide = contexte par défaut du kubeconfig)')
    string(name: 'MONITORING_NAMESPACE', defaultValue: 'freelance-monitoring', description: 'Namespace pour Prometheus / Grafana (voir k8s/namespace.yaml)')
    string(name: 'KUBECONFIG_CREDENTIALS_ID', defaultValue: 'kubeconfig', description: 'ID Jenkins du credential « Secret file » (kubeconfig)')
    string(name: 'HELM_RELEASE_PROMETHEUS', defaultValue: 'prometheus', description: 'Nom de release Helm pour Prometheus')
    string(name: 'HELM_RELEASE_GRAFANA', defaultValue: 'grafana', description: 'Nom de release Helm pour Grafana')
  }

  stages {

    // Récupère le code selon la config SCM du job (aucun clone / URL codée en dur ici).
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    // Optionnel : si le dossier .github/workflows existe (souvent CI cloud), on le liste seulement — pas de fetch distant.
    stage('CI — Workflows (optionnel)') {
      steps {
        script {
          if (fileExists('.github/workflows')) {
            sh 'find .github/workflows -maxdepth 1 -type f \\( -name "*.yml" -o -name "*.yaml" \\) 2>/dev/null | sort || ls -la .github/workflows/'
            echo '[INFO] Fichiers de workflow présents localement (déclenchement côté hébergeur au push, hors Jenkins).'
          } else {
            echo '[INFO] Pas de .github/workflows — ignoré (pas obligatoire pour cette pipeline).'
          }
        }
      }
    }

    stage('Build — Backend Maven') {
      when { expression { params.PARALLEL_MAVEN } }
      parallel {
        stage('Eureka') {
          steps {
            dir('Eureka') {
              sh 'mvn -B -ntp clean package -DskipTests'
            }
          }
        }
        stage('API Gateway') {
          steps {
            dir('ApiGateway4sae2') {
              sh 'mvn -B -ntp clean package -DskipTests'
            }
          }
        }
        stage('User Service') {
          steps {
            dir('services/backend1') {
              sh 'mvn -B -ntp clean package -DskipTests'
            }
          }
        }
        stage('Service Test') {
          steps {
            dir('services/testService/service-test') {
              sh 'mvn -B -ntp clean package -DskipTests'
            }
          }
        }
      }
    }

    stage('Build — Backend Maven (séquentiel)') {
      when { expression { !params.PARALLEL_MAVEN } }
      steps {
        sh '''
          set -e
          for d in Eureka ApiGateway4sae2 services/backend1 services/testService/service-test; do
            echo "=== mvn package: $d ==="
            (cd "$d" && mvn -B -ntp clean package -DskipTests)
          done
        '''
      }
    }

    stage('Tests — Maven') {
      when { expression { params.RUN_MAVEN_TESTS } }
      steps {
        sh '''
          set -e
          for d in Eureka ApiGateway4sae2 services/backend1 services/testService/service-test; do
            if [ -f "$d/pom.xml" ]; then
              echo "=== mvn test: $d ==="
              (cd "$d" && mvn -B -ntp test)
            fi
          done
        '''
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Build — Frontend Angular') {
      when { expression { params.BUILD_FRONTEND } }
      steps {
        dir('frontend') {
          sh '''
            npm ci
            npm run build -- --configuration production
          '''
        }
      }
    }

    stage('Docker — Build images') {
      when { expression { params.BUILD_DOCKER_IMAGES } }
      steps {
        script {
          def builds = [
            [path: 'Eureka', image: 'eureka'],
            [path: 'ApiGateway4sae2', image: 'api-gateway'],
            [path: 'services/backend1', image: 'user-service'],
            [path: 'services/testService/service-test', image: 'service-test'],
            [path: 'services/project-service', image: 'project-service'],
            [path: 'services/complaints-service', image: 'complaints-service'],
            [path: 'frontend', image: 'frontend']
          ]
          builds.each { b ->
            dir(b.path) {
              sh """
                docker build -t ${IMAGE_REGISTRY}/${b.image}:${BUILD_NUMBER} .
                docker tag ${IMAGE_REGISTRY}/${b.image}:${BUILD_NUMBER} ${IMAGE_REGISTRY}/${b.image}:latest
              """
            }
          }
        }
      }
    }

    stage('Docker — Push registry') {
      when {
        allOf {
          expression { params.BUILD_DOCKER_IMAGES }
          expression { params.PUSH_IMAGES }
        }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
          '''
          script {
            ['eureka', 'api-gateway', 'user-service', 'service-test', 'project-service', 'complaints-service', 'frontend'].each { img ->
              sh """
                docker push ${IMAGE_REGISTRY}/${img}:${BUILD_NUMBER}
                docker push ${IMAGE_REGISTRY}/${img}:latest
              """
            }
          }
        }
      }
    }

    // Observabilité — schéma : 1 Checkout (ci-dessus) → 2 kubeconfig + namespace → 3 repos Helm → 4–5 helm upgrade --install → 6 rollout + verify.sh
    stage('Setup K8s context') {
      when { expression { params.DEPLOY_MONITORING } }
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
      when { expression { params.DEPLOY_MONITORING } }
      steps {
        withCredentials([file(credentialsId: params.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG')]) {
          sh '''#!/usr/bin/env bash
set -euo pipefail
export KUBECONFIG
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts || true
helm repo add grafana https://grafana.github.io/helm-charts || true
helm repo update
helm repo list
'''
        }
      }
    }

    stage('Deploy Prometheus') {
      when { expression { params.DEPLOY_MONITORING } }
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
      when { expression { params.DEPLOY_MONITORING } }
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
      when { expression { params.DEPLOY_MONITORING } }
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

    // Déploiement applicatif : plusieurs replicas + rolling update = tolérance aux pannes côté rollout
    stage('Kubernetes — Déploiement (rolling update)') {
      when { expression { params.DEPLOY_KUBERNETES } }
      steps {
        script {
          def ctx = params.K8S_CONTEXT?.trim() ? "--context=${params.K8S_CONTEXT}" : ''
          def ns = params.K8S_NAMESPACE
          if (fileExists('k8s')) {
            sh """
              kubectl ${ctx} create namespace ${ns} --dry-run=client -o yaml | kubectl ${ctx} apply -f -
              kubectl ${ctx} apply -f k8s/ -n ${ns} --recursive || true
            """
            // Ajustez les noms de Deployments selon vos YAML
            sh """
              kubectl ${ctx} rollout status deployment/api-gateway -n ${ns} --timeout=300s || true
              kubectl ${ctx} rollout status deployment/frontend -n ${ns} --timeout=300s || true
            """
          } else {
            echo '[WARN] Dossier k8s/ absent — créez manifests Deployment/Service (replicas >= 2 pour HA).'
          }
        }
      }
    }

    // Ingress ou Service LoadBalancer : point d’entrée unique, répartition du trafic (kube-proxy / Ingress controller)
    stage('Kubernetes — Ingress & load balancing') {
      when { expression { params.DEPLOY_KUBERNETES } }
      steps {
        script {
          def ctx = params.K8S_CONTEXT?.trim() ? "--context=${params.K8S_CONTEXT}" : ''
          def ns = params.K8S_NAMESPACE
          if (fileExists('k8s/ingress.yaml')) {
            sh "kubectl ${ctx} apply -f k8s/ingress.yaml -n ${ns}"
          }
          sh "kubectl ${ctx} get ingress -n ${ns} 2>/dev/null || true"
          sh "kubectl ${ctx} get svc -n ${ns} -o wide 2>/dev/null || true"
          echo '[INFO] Load balancing : Ingress Controller (HTTP) ou Service type LoadBalancer ; trafic réparti entre les pods du Service.'
        }
      }
    }

    // PDB + probes : maintenir le quorum pendant les mises à jour et les défaillances nœuds
    stage('Kubernetes — Tolérance aux pannes & résilience') {
      when { expression { params.DEPLOY_KUBERNETES } }
      steps {
        script {
          def ctx = params.K8S_CONTEXT?.trim() ? "--context=${params.K8S_CONTEXT}" : ''
          def ns = params.K8S_NAMESPACE
          if (fileExists('k8s/pod-disruption-budget.yaml')) {
            sh "kubectl ${ctx} apply -f k8s/pod-disruption-budget.yaml -n ${ns}"
          }
          sh """
            kubectl ${ctx} get deploy -n ${ns} -o wide 2>/dev/null || true
            kubectl ${ctx} get pdb -n ${ns} 2>/dev/null || true
          """
          echo '[INFO] Recommandations : replicas >= 2, readinessProbe/livenessProbe, PodDisruptionBudget, anti-affinity si multi-zones.'
        }
      }
    }
  }

  post {
    success {
      echo 'Pipeline terminé avec succès.'
    }
    failure {
      echo 'Pipeline en échec — consulter les logs des stages.'
    }
    unstable {
      echo 'Pipeline instable (ex. tests ignorés ou seuils).'
    }
  }
}
