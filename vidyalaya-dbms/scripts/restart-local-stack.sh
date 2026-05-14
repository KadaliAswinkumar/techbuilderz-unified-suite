#!/usr/bin/env bash
set -euo pipefail
# From repo root: stop dev servers, ensure Postgres, optionally clear demo tenant rows, start API + UI, smoke-check.
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT}"

echo "== 1) Stop dev servers =="
bash "${ROOT}/scripts/stop-local-dev.sh"

echo "== 2) Postgres (Docker or Podman) =="
if docker compose -f "${ROOT}/docker-compose.yml" up -d 2>/dev/null; then
  echo "docker compose up OK"
elif podman compose -f "${ROOT}/docker-compose.yml" up -d 2>/dev/null; then
  echo "podman compose up OK"
else
  echo "WARN: could not start compose — ensure Postgres is on localhost:5433"
fi
echo "Waiting for Postgres on 5433..."
export PGPASSWORD="${MASTER_DB_PASSWORD:-vidyalaya}"
for i in $(seq 1 30); do
  if psql -h 127.0.0.1 -p 5433 -U "${MASTER_DB_USER:-vidyalaya}" -d vidyalaya_master -c "select 1" >/dev/null 2>&1; then
    echo "Postgres is accepting connections."
    break
  fi
  sleep 1
  if [[ "$i" -eq 30 ]]; then
    echo "FAIL: Postgres not ready on 127.0.0.1:5433 (start compose and ensure psql is installed)."
    exit 1
  fi
done

echo "== 3) Reset demo tenant data (if tenant DB exists) =="
export PGPASSWORD="${MASTER_DB_PASSWORD:-vidyalaya}"
DBNAME=$(psql -h 127.0.0.1 -p 5433 -U "${MASTER_DB_USER:-vidyalaya}" -d vidyalaya_master -tAc "SELECT db_name FROM tenants WHERE slug='demo' LIMIT 1" 2>/dev/null | tr -d '[:space:]' || true)
if [[ -n "${DBNAME}" ]]; then
  echo "Truncating business data in tenant DB: ${DBNAME}"
  psql -h 127.0.0.1 -p 5433 -U "${MASTER_DB_USER:-vidyalaya}" -d "${DBNAME}" -f "${ROOT}/scripts/reset-demo-tenant-data.sql"
else
  echo "No demo tenant row yet — backend will provision + seed on first start (profile dev)."
fi

echo "== 4) Start API (dev) =="
cd "${ROOT}/backend/vidyalaya-server"
export MASTER_DB_URL="${MASTER_DB_URL:-jdbc:postgresql://127.0.0.1:5433/vidyalaya_master}"
export MASTER_DB_USER="${MASTER_DB_USER:-vidyalaya}"
export MASTER_DB_PASSWORD="${MASTER_DB_PASSWORD:-vidyalaya}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
: > /tmp/vidyalaya-backend.log
nohup mvn -q spring-boot:run >> /tmp/vidyalaya-backend.log 2>&1 &
echo "API starting (logs: /tmp/vidyalaya-backend.log)"
for i in $(seq 1 90); do
  if grep -q "Started .*Application in" /tmp/vidyalaya-backend.log 2>/dev/null; then
    echo "Spring Boot started after ~$((i * 2))s"
    break
  fi
  if grep -q "Application run failed" /tmp/vidyalaya-backend.log 2>/dev/null; then
    echo "FAIL: Spring Boot exited during startup:"
    tail -60 /tmp/vidyalaya-backend.log
    exit 1
  fi
  sleep 2
  if [[ "$i" -eq 90 ]]; then
    echo "FAIL: API did not start; tail log:"
    tail -60 /tmp/vidyalaya-backend.log
    exit 1
  fi
done
if ! curl -sf http://127.0.0.1:8080/api/auth/dev-token >/dev/null; then
  echo "FAIL: dev-token not reachable"
  exit 1
fi

echo "== 5) Start frontend =="
cd "${ROOT}/frontend"
nohup npm run dev > /tmp/vidyalaya-frontend.log 2>&1 &
echo "Vite starting (logs: /tmp/vidyalaya-frontend.log)"
sleep 4
grep -E "Local:|http://localhost" /tmp/vidyalaya-frontend.log | tail -3 || true

echo "== 6) Verify demo data =="
API=http://127.0.0.1:8080 bash "${ROOT}/scripts/verify-demo-data.sh"

echo ""
echo "Open the Local URL printed above (Vite). API: http://127.0.0.1:8080"
