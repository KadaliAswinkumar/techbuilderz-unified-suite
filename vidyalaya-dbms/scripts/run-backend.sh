#!/usr/bin/env bash
set -euo pipefail
# Run from repo root — avoids -pl mistakes; does not require being in backend/
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/backend/vidyalaya-server"
export MASTER_DB_URL="${MASTER_DB_URL:-jdbc:postgresql://localhost:5433/vidyalaya_master}"
export MASTER_DB_USER="${MASTER_DB_USER:-vidyalaya}"
export MASTER_DB_PASSWORD="${MASTER_DB_PASSWORD:-vidyalaya}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
exec mvn spring-boot:run "$@"
