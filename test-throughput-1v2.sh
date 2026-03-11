#!/bin/bash
set -euo pipefail

# Compatibility wrapper to keep existing invocation path stable.
# Preferred script location is scripts/performance/test-throughput-1v2.sh

exec bash "$(dirname "$0")/scripts/performance/test-throughput-1v2.sh" "$@"
