#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
API="${API:-http://127.0.0.1:8080}"

echo "== Dev token =="
tok_json=$(curl -sf "${API}/api/auth/dev-token") || { echo "FAIL: no API at ${API}"; exit 1; }
TOKEN=$(echo "${tok_json}" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "OK (token length ${#TOKEN})"

echo "== Students (tenant=demo) =="
code=$(curl -s -o /tmp/students.json -w "%{http_code}" "${API}/api/students" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "X-Tenant-Slug: demo")
echo "HTTP ${code}"
python3 <<'PY'
import json, sys
try:
    with open("/tmp/students.json") as f:
        d = json.load(f)
    if isinstance(d, list):
        n = len(d)
        print(f"student_count={n}")
        if n < 20:
            print("FAIL: expected demo seed (~50 students); check backend profile dev and logs.")
            sys.exit(1)
    else:
        print("response:", d)
        sys.exit(1)
except Exception as e:
    print("parse error", e)
    sys.exit(1)
PY

echo "== Dashboard admin (tenant=demo) =="
code=$(curl -s -o /tmp/dash.json -w "%{http_code}" "${API}/api/dashboard/admin?from=2025-01-01&to=2026-12-31" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "X-Tenant-Slug: demo")
echo "HTTP ${code}"
python3 <<'PY'
import json
try:
    raw = open("/tmp/dash.json").read().strip()
    if not raw:
        print("(empty body)")
    else:
        d = json.loads(raw)
        if isinstance(d, dict) and "students" in d:
            print(
                "students",
                d.get("students"),
                "teachers",
                d.get("teachers"),
                "parents",
                d.get("parents"),
                "monthlySeries_len",
                len(d.get("monthlySeries") or []),
            )
        else:
            print(d)
except Exception as e:
    print("parse error", e)
PY
