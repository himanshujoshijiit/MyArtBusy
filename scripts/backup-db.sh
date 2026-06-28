#!/usr/bin/env bash
# Daily Postgres backup — run via cron on the VPS:
#   0 3 * * * /opt/makeupseven/scripts/backup-db.sh >> /var/log/makeupseven-backup.log 2>&1
#
# Requires: docker, pg_dump (via postgres container)
# Optional: set BACKUP_S3_BUCKET or BACKUP_DIR for off-server storage

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_DIR"

POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-mab-postgres-1}"
POSTGRES_USER="${POSTGRES_USER:-makeupseven}"
POSTGRES_DB="${POSTGRES_DB:-makeupseven}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETAIN_DAYS="${RETAIN_DAYS:-14}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
FILENAME="makeupseven_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date -Iseconds)] Starting backup → ${BACKUP_DIR}/${FILENAME}"

docker exec "$POSTGRES_CONTAINER" pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > "${BACKUP_DIR}/${FILENAME}"

echo "[$(date -Iseconds)] Backup complete ($(du -h "${BACKUP_DIR}/${FILENAME}" | cut -f1))"

# Optional: upload to S3 (install awscli and configure credentials on VPS)
if [[ -n "${BACKUP_S3_BUCKET:-}" ]]; then
  aws s3 cp "${BACKUP_DIR}/${FILENAME}" "s3://${BACKUP_S3_BUCKET}/db/${FILENAME}"
  echo "[$(date -Iseconds)] Uploaded to s3://${BACKUP_S3_BUCKET}/db/${FILENAME}"
fi

# Prune old local backups
find "$BACKUP_DIR" -name 'makeupseven_*.sql.gz' -mtime +"$RETAIN_DAYS" -delete 2>/dev/null || true
echo "[$(date -Iseconds)] Pruned backups older than ${RETAIN_DAYS} days"
