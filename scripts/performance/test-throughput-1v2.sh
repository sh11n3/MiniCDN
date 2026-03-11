#!/bin/bash
set -euo pipefail

# NFA-C1 Throughput benchmark:
# Compare router throughput with 1 edge instance vs. 2 edge instances.
# Pass condition: rps_two / rps_one >= 1.5

TOKEN="${MINICDN_ADMIN_TOKEN:-secret-token}"
ROUTER_BASE="${ROUTER_BASE:-http://localhost:8082}"
ORIGIN_BASE="${ORIGIN_BASE:-http://localhost:8080}"
TEST_REGION="${TEST_REGION:-EU}"
TEST_FILE="${TEST_FILE:-nfa-c1-throughput.txt}"
DURATION_SEC="${DURATION_SEC:-20}"
CONCURRENCY="${CONCURRENCY:-40}"
WARMUP_REQUESTS="${WARMUP_REQUESTS:-200}"

EDGE_JAR="edge/target/edge-1.0-SNAPSHOT-exec.jar"

EXTRA_EDGE_INSTANCE_ID=""

cleanup() {
  if [ -n "${EXTRA_EDGE_INSTANCE_ID}" ]; then
    curl -sf -X DELETE -H "X-Admin-Token: ${TOKEN}" \
      "${ROUTER_BASE}/api/cdn/admin/edges/${EXTRA_EDGE_INSTANCE_ID}?deregister=true" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

ensure_services() {
  echo "[1/8] Ensure services are running..."
  if ! curl -sf -H "X-Admin-Token: ${TOKEN}" "${ROUTER_BASE}/api/cdn/health" >/dev/null 2>&1; then
    echo "  router unavailable -> start services"
    bash ./startup-service.sh >/dev/null
  fi
}

ensure_edge_jar() {
  echo "[2/8] Ensure edge executable JAR exists for lifecycle auto-start..."
  if [ ! -f "${EDGE_JAR}" ]; then
    echo "  missing ${EDGE_JAR} -> building edge module"
    (cd edge && mvn -q -DskipTests package)
  fi
}

ensure_single_edge_setup() {
  echo "[3/8] Ensure baseline routing with exactly one managed baseline edge..."
  # ensure baseline edge is registered (idempotent because add endpoint ignores duplicates)
  curl -sf -X POST -H "X-Admin-Token: ${TOKEN}" \
    "${ROUTER_BASE}/api/cdn/routing?region=${TEST_REGION}&url=http://localhost:8081" >/dev/null || true
}

upload_test_file() {
  echo "[4/8] Upload benchmark file to origin..."
  curl -sf -X PUT -H "X-Admin-Token: ${TOKEN}" -H "Content-Type: application/octet-stream" \
    --data "nfa-c1 benchmark payload" \
    "${ORIGIN_BASE}/api/origin/admin/files/${TEST_FILE}" >/dev/null
}

warmup_router() {
  local clients_prefix="$1"
  echo "  warmup (${WARMUP_REQUESTS} requests)..."
  for i in $(seq 1 "${WARMUP_REQUESTS}"); do
    curl -s -o /dev/null -w "%{http_code}" \
      "${ROUTER_BASE}/api/cdn/files/${TEST_FILE}?region=${TEST_REGION}&clientId=${clients_prefix}-warmup-${i}" \
      | grep -q "307"
  done
}

run_load_test() {
  local label="$1"
  local client_prefix="$2"

  local end_epoch
  end_epoch=$(( $(date +%s) + DURATION_SEC ))

  local result_dir
  result_dir="$(mktemp -d)"

  local started_at_ns
  started_at_ns="$(date +%s%N)"

  for worker in $(seq 1 "${CONCURRENCY}"); do
    (
      local ok=0
      local attempts=0
      while [ "$(date +%s)" -lt "${end_epoch}" ]; do
        attempts=$((attempts + 1))
        code="$(curl -s -o /dev/null -w "%{http_code}" \
          "${ROUTER_BASE}/api/cdn/files/${TEST_FILE}?region=${TEST_REGION}&clientId=${client_prefix}-w${worker}-r${attempts}")"
        if [ "${code}" = "307" ]; then
          ok=$((ok + 1))
        fi
      done
      printf "%s\n" "${ok}" > "${result_dir}/${worker}.ok"
    ) &
  done
  wait

  local ended_at_ns
  ended_at_ns="$(date +%s%N)"

  local success_total
  success_total="$(awk '{s+=$1} END {print s+0}' "${result_dir}"/*.ok)"

  local elapsed_sec
  elapsed_sec="$(python3 - <<PY
start_ns=${started_at_ns}
end_ns=${ended_at_ns}
print((end_ns-start_ns)/1_000_000_000)
PY
)"

  local rps
  rps="$(python3 - <<PY
success=${success_total}
elapsed=${elapsed_sec}
print(0 if elapsed <= 0 else success/elapsed)
PY
)"

  rm -rf "${result_dir}"

  echo "${label}|${success_total}|${elapsed_sec}|${rps}"
}

start_second_edge() {
  echo "[6/8] Start one additional edge via lifecycle adapter..."
  local response_file
  response_file="$(mktemp)"

  curl -sf -X POST -H "X-Admin-Token: ${TOKEN}" -H "Content-Type: application/json" \
    -d "{\"region\":\"${TEST_REGION}\",\"count\":1,\"originBaseUrl\":\"${ORIGIN_BASE}\",\"autoRegister\":true,\"waitUntilReady\":true}" \
    "${ROUTER_BASE}/api/cdn/admin/edges/start/auto" > "${response_file}"

  EXTRA_EDGE_INSTANCE_ID="$(python3 - <<PY
import json
from pathlib import Path
obj = json.loads(Path('${response_file}').read_text())
print(obj['edges'][0]['instanceId'])
PY
)"

  rm -f "${response_file}"
  echo "  started extra edge: ${EXTRA_EDGE_INSTANCE_ID}"
}

print_report_and_assert() {
  local line_one="$1"
  local line_two="$2"

  local one_rps
  local two_rps
  one_rps="$(echo "${line_one}" | awk -F'|' '{print $4}')"
  two_rps="$(echo "${line_two}" | awk -F'|' '{print $4}')"

  local ratio
  ratio="$(python3 - <<PY
one=${one_rps}
two=${two_rps}
print(0 if one <= 0 else two/one)
PY
)"

  echo
  echo "=== NFA-C1 Throughput Report ==="
  printf "%-14s | %-10s | %-12s | %-12s\n" "Scenario" "Success" "Elapsed(s)" "RPS"
  printf "%-14s-+-%-10s-+-%-12s-+-%-12s\n" "--------------" "----------" "------------" "------------"
  echo "${line_one}" | awk -F'|' '{printf "%-14s | %-10s | %-12s | %-12s\n", $1, $2, $3, $4}'
  echo "${line_two}" | awk -F'|' '{printf "%-14s | %-10s | %-12s | %-12s\n", $1, $2, $3, $4}'
  echo "ratio(two/one)=${ratio}"
  echo "required>=1.5"

  python3 - <<PY
ratio=${ratio}
import sys
if ratio >= 1.5:
    sys.exit(0)
sys.exit(1)
PY
}

echo "NFA-C1 benchmark configuration: duration=${DURATION_SEC}s concurrency=${CONCURRENCY} warmup=${WARMUP_REQUESTS}"

ensure_services
ensure_edge_jar
ensure_single_edge_setup
upload_test_file

echo "[5/8] Measure throughput with 1 edge instance..."
warmup_router "run1"
ONE_LINE="$(run_load_test "one-edge" "run1")"

echo "[6/8] Prepare throughput measurement with 2 edge instances..."
start_second_edge

echo "[7/8] Measure throughput with 2 edge instances..."
warmup_router "run2"
TWO_LINE="$(run_load_test "two-edges" "run2")"

echo "[8/8] Evaluate acceptance criterion..."
if print_report_and_assert "${ONE_LINE}" "${TWO_LINE}"; then
  echo "PASS: NFA-C1 fulfilled (throughput factor >= 1.5)."
else
  echo "FAIL: NFA-C1 not fulfilled (throughput factor < 1.5)."
  exit 1
fi
