#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/../.."

docker compose -f docker-compose.mysql.yml down

echo "MySQL stopped."
