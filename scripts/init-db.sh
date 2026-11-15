#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if ! command -v java >/dev/null 2>&1; then source scripts/local-env.sh; fi
if [[ -f .env.local ]]; then
    set -a
    source .env.local
    set +a
fi
: "${DB_URL:?Set DB_URL}"
: "${DB_USER:?Set DB_USER}"
: "${DB_PASSWORD:?Set DB_PASSWORD}"
: "${ADMIN_PASSWORD:?Set ADMIN_PASSWORD (at least 12 characters)}"
exec java -cp "target/classes:$(cat target/classpath.txt)" com.study.launcher.DatabaseSetup
