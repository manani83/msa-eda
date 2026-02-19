#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

docker compose up -d mysql

echo "MySQL is starting. Check logs with: docker compose logs -f mysql"
