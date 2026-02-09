#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

docker compose up -d

SPRING_PID_FILE=app/build/app.pid \
SPRING_PID_FAIL_ON_WRITE_ERROR=true \
./gradlew :app:bootRun > app/build/bootRun.log 2>&1 &

echo "bootRun started (logs: app/build/bootRun.log)"

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
