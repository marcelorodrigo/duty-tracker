#!/usr/bin/env bash
set -euo pipefail

echo "==> Stopping containers..."
docker compose down 

echo "==> Building images..."
docker compose build

echo "==> Starting project..."
docker compose up
