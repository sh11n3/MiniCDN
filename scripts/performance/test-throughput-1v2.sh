#!/bin/bash
set -euo pipefail

# Compatibility wrapper for previous bash entrypoint.
# Main implementation lives in Python for cross-platform usage.

if command -v python3 >/dev/null 2>&1; then
  exec python3 "$(dirname "$0")/test_throughput_1v2.py" "$@"
fi

exec python "$(dirname "$0")/test_throughput_1v2.py" "$@"
