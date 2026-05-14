#!/usr/bin/env bash
# Quick smoke check: Postgres (optional), API OpenAPI, UI dev server (optional).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

fail() { echo "FAIL: $*" >&2; exit 1; }

echo "== Vidyalaya local verify =="

if command -v podman >/dev/null 2>&1; then
  if podman compose -f docker-compose.yml ps 2>/dev/null | grep -qi postgres; then
    echo "OK: Postgres service present in podman compose ps"
  else
    echo "WARN: Postgres not running — start with: podman compose -f docker-compose.yml up -d"
  fi
else
  echo "WARN: podman not in PATH (skip compose check)"
fi

if curl -sf --max-time 3 "http://127.0.0.1:8080/v3/api-docs" >/dev/null; then
  echo "OK: API OpenAPI reachable at http://127.0.0.1:8080"
elif curl -sf --max-time 3 "http://127.0.0.1:8080/api-docs" >/dev/null; then
  echo "OK: API docs reachable at http://127.0.0.1:8080/api-docs"
else
  fail "API not responding on 8080 — run ./scripts/run-backend.sh (and Postgres on 5433)"
fi

if curl -sf --max-time 2 "http://127.0.0.1:5173/" >/dev/null 2>&1; then
  echo "OK: Vite dev server responding on 5173"
else
  echo "WARN: Nothing on 5173 — run: cd frontend && npm run dev"
fi

echo "Done."
