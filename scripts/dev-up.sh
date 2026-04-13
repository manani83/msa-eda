#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

IMAGE_NAME="hexagonal-app:dev"
IMAGE_TAR_DIR="app/docker-images"
IMAGE_TAR_PATH="${IMAGE_TAR_DIR}/hexagonal-app_dev.tar"
COMPOSE_FILE_ARGS=(-f docker-compose.mysql.yml -f docker-compose.yml)

./gradlew :app:bootJar

docker build -t "${IMAGE_NAME}" -f app/Dockerfile app

mkdir -p "${IMAGE_TAR_DIR}"
docker save "${IMAGE_NAME}" -o "${IMAGE_TAR_PATH}"

docker load -i "${IMAGE_TAR_PATH}"

docker compose "${COMPOSE_FILE_ARGS[@]}" up -d mysql

echo "Waiting for MySQL to become healthy..."
for _ in $(seq 1 60); do
  mysql_container_id="$(docker compose "${COMPOSE_FILE_ARGS[@]}" ps -q mysql)"
  if [ -n "${mysql_container_id}" ]; then
    mysql_health_status="$(docker inspect -f '{{.State.Health.Status}}' "${mysql_container_id}" 2>/dev/null || true)"
    if [ "${mysql_health_status}" = "healthy" ]; then
      echo "MySQL is healthy."
      break
    fi
  fi
  sleep 1
done

if [ "${mysql_health_status:-}" != "healthy" ]; then
  echo "MySQL did not become healthy in time. Check logs with: docker compose -f docker-compose.mysql.yml logs -f mysql"
  exit 1
fi

docker compose "${COMPOSE_FILE_ARGS[@]}" up -d app

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
