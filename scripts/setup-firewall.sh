#!/usr/bin/env bash
# Run once on a fresh Ubuntu/Debian VPS before going live.
# Allows SSH (22), HTTP (80), HTTPS (443) only.

set -euo pipefail

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run as root: sudo $0"
  exit 1
fi

ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp comment 'SSH'
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'
ufw --force enable
ufw status verbose

echo ""
echo "Firewall enabled. Postgres and internal API ports are NOT exposed."
echo "Deploy with: docker compose -f docker-compose.yml -f docker-compose.prod.yml --profile production up -d"
