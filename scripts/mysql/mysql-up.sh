#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

COMPOSE_FILE="docker-compose.mysql.yml"

docker compose -f "${COMPOSE_FILE}" up -d

echo "MySQL is starting. Waiting for health check..."
for _ in $(seq 1 60); do
  container_id="$(docker compose -f "${COMPOSE_FILE}" ps -q mysql)"
  if [ -n "${container_id}" ]; then
    health_status="$(docker inspect -f '{{.State.Health.Status}}' "${container_id}" 2>/dev/null || true)"
    if [ "${health_status}" = "healthy" ]; then
      echo "MySQL is healthy."
      exit 0
    fi
  fi
  sleep 1
done

echo "MySQL did not become healthy in time. Check logs with: docker compose -f ${COMPOSE_FILE} logs -f mysql"
exit 1
