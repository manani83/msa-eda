#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

IMAGE_NAME="hexagonal-app:dev"
IMAGE_TAR_DIR="app/docker-images"
IMAGE_TAR_PATH="${IMAGE_TAR_DIR}/hexagonal-app_dev.tar"

./gradlew :app:bootJar

docker build -t "${IMAGE_NAME}" -f app/Dockerfile app

mkdir -p "${IMAGE_TAR_DIR}"
docker save "${IMAGE_NAME}" -o "${IMAGE_TAR_PATH}"

docker load -i "${IMAGE_TAR_PATH}"

docker compose up -d

if command -v curl >/dev/null 2>&1; then
  echo "Waiting for app health check on http://localhost:8080/hello ..."
  for i in $(seq 1 60); do
    if curl -fsS http://localhost:8080/hello >/dev/null 2>&1; then
      echo "App is up."
      exit 0
    fi
    sleep 1
  done
  echo "Health check timed out. See app/build/bootRun.log"
  exit 1
else
  echo "curl not found; skipping health check."
fi
