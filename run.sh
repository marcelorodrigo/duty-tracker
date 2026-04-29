#!/usr/bin/env bash
set -euo pipefail

echo "==> Stopping containers and removing volumes..."
docker compose down -v

echo "==> Building images..."
docker compose build

echo "==> Starting project..."
docker compose up
