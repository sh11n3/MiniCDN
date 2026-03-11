#!/bin/bash
set -euo pipefail

# Compatibility wrapper to keep the root invocation path stable.

exec bash "$(dirname "$0")/scripts/performance/test-throughput-1v2.sh" "$@"
