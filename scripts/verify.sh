#!/usr/bin/env bash
# Vérifications post-déploiement (kubectl rollout status, pods)
set -euo pipefail
NS="${1:-freelance-monitoring}"
CTX="${KUBE_CONTEXT:-}"
K=(kubectl)
[[ -n "$CTX" ]] && K+=(--context="$CTX")

echo "=== Namespace: $NS ==="
"${K[@]}" get ns "$NS" -o wide 2>/dev/null || true
echo "=== Pods ==="
"${K[@]}" get pods -n "$NS" -o wide 2>/dev/null || true
echo "=== Deployments (rollout) ==="
for d in $("${K[@]}" get deploy -n "$NS" -o jsonpath='{.items[*].metadata.name}' 2>/dev/null); do
  [[ -z "$d" ]] && continue
  "${K[@]}" rollout status "deployment/$d" -n "$NS" --timeout=120s || true
done
echo "=== OK verify ==="
