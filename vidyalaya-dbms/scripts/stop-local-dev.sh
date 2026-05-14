#!/usr/bin/env bash
set -euo pipefail
# Stop Vite + Spring Boot dev servers bound to common local ports (macOS-friendly).
for port in 8080 5173 5174 5175 5176 5177 5178 5179 5180; do
  pids=$(lsof -ti ":$port" 2>/dev/null || true)
  if [[ -n "${pids}" ]]; then
    echo "Stopping port ${port}: ${pids}"
    kill -9 ${pids} 2>/dev/null || true
  fi
done
echo "Done. Ports 8080 and 5173–5180 should be free (verify with: lsof -i :8080)."
