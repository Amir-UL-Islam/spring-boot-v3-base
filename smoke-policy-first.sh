#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9988}"
SUPERADMIN_USER="${SUPERADMIN_USER:-superadmin}"
SUPERADMIN_PASS="${SUPERADMIN_PASS:-superadmin123!}"
BASIC_USER="${BASIC_USER:-user}"
BASIC_PASS="${BASIC_PASS:-user123!}"

step() {
  printf "\n[smoke] %s\n" "$1"
}

json_field() {
  local json="$1"
  local field="$2"
  python3 - "$json" "$field" <<'PY'
import json,sys
payload=sys.argv[1]
field=sys.argv[2]
try:
    data=json.loads(payload)
except Exception:
    print("")
    sys.exit(0)
value=data.get(field, "")
print(value if isinstance(value, str) else "")
PY
}

http_code() {
  local path="$1"
  local token="${2:-}"
  if [[ -n "$token" ]]; then
    curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL$path" -H "Authorization: Bearer $token"
  else
    curl -sS -o /dev/null -w "%{http_code}" "$BASE_URL$path"
  fi
}

login() {
  local username="$1"
  local password="$2"
  local body
  body=$(printf '{"username":"%s","password":"%s"}' "$username" "$password")
  curl -sS -X POST "$BASE_URL/authenticate" \
    -H "Content-Type: application/json" \
    -d "$body"
}

step "Login as superadmin"
SUPERADMIN_LOGIN_JSON="$(login "$SUPERADMIN_USER" "$SUPERADMIN_PASS")"
SUPERADMIN_TOKEN="$(json_field "$SUPERADMIN_LOGIN_JSON" "accessToken")"
if [[ -z "$SUPERADMIN_TOKEN" ]]; then
  echo "[smoke][FAIL] Superadmin login failed: $SUPERADMIN_LOGIN_JSON"
  exit 1
fi
echo "[smoke][OK] Superadmin login returned access token"

step "Verify matrix endpoint for superadmin"
ME_CODE="$(http_code "/api/me/permissions" "$SUPERADMIN_TOKEN")"
if [[ "$ME_CODE" != "200" ]]; then
  echo "[smoke][FAIL] Expected 200 from /api/me/permissions, got $ME_CODE"
  exit 1
fi
echo "[smoke][OK] /api/me/permissions returned 200"

step "Verify policy diagnostics endpoint for superadmin"
DRIFT_CODE="$(http_code "/api/policy/drift" "$SUPERADMIN_TOKEN")"
if [[ "$DRIFT_CODE" != "200" ]]; then
  echo "[smoke][FAIL] Expected 200 from /api/policy/drift, got $DRIFT_CODE"
  exit 1
fi
echo "[smoke][OK] /api/policy/drift returned 200"

step "Verify policy-first deny for unknown API route"
UNKNOWN_CODE="$(http_code "/api/does-not-exist" "$SUPERADMIN_TOKEN")"
if [[ "$UNKNOWN_CODE" != "403" && "$UNKNOWN_CODE" != "404" ]]; then
  echo "[smoke][FAIL] Expected 403/404 from unknown API route, got $UNKNOWN_CODE"
  exit 1
fi
echo "[smoke][OK] Unknown API route returned $UNKNOWN_CODE (denied or unresolved)"

step "Login as baseline user"
BASIC_LOGIN_JSON="$(login "$BASIC_USER" "$BASIC_PASS")"
BASIC_TOKEN="$(json_field "$BASIC_LOGIN_JSON" "accessToken")"
if [[ -z "$BASIC_TOKEN" ]]; then
  echo "[smoke][FAIL] Baseline user login failed: $BASIC_LOGIN_JSON"
  exit 1
fi
echo "[smoke][OK] Baseline user login returned access token"

step "Verify policy diagnostics endpoint is restricted"
BASIC_DRIFT_CODE="$(http_code "/api/policy/drift" "$BASIC_TOKEN")"
if [[ "$BASIC_DRIFT_CODE" != "403" ]]; then
  echo "[smoke][FAIL] Expected 403 from /api/policy/drift for baseline user, got $BASIC_DRIFT_CODE"
  exit 1
fi
echo "[smoke][OK] /api/policy/drift is restricted for non-policy-admin"

echo "\n[smoke][PASS] Policy-first matrix smoke checks completed successfully."

