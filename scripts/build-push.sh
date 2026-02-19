#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

IMAGE_REPO="manani83/hexagonal-app"
IMAGE_TAG="dev-$(date +%Y%m%d%H%M%S)"
IMAGE_NAME="${IMAGE_REPO}:${IMAGE_TAG}"

./gradlew :app:bootJar

docker build -t "${IMAGE_NAME}" -f app/Dockerfile app
docker push "${IMAGE_NAME}"

kubectl set image -n hexagonal deployment/hexagonal-app app="${IMAGE_NAME}"
kubectl rollout status -n hexagonal deployment/hexagonal-app

echo "Pushed and deployed ${IMAGE_NAME}"
