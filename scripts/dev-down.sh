#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

PID_FILE="app/build/app.pid"
if [ -f "$PID_FILE" ]; then
  PID="$(cat "$PID_FILE")"
  if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
    kill "$PID"
  fi
  rm -f "$PID_FILE"
else
  # Fallback: kill by main class if PID file is missing
  if command -v pgrep >/dev/null 2>&1; then
    PIDS="$(pgrep -f 'com.example.hexagonal.HexagonalApiApplication' || true)"
    if [ -n "$PIDS" ]; then
      kill $PIDS
    fi
  fi
fi

docker compose down
