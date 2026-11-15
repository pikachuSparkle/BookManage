#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if ! command -v java >/dev/null 2>&1; then source scripts/local-env.sh; fi
if [[ -f .env.local ]]; then
    set -a
    source .env.local
    set +a
fi
: "${DB_PASSWORD:?Set DB_PASSWORD or create .env.local first}"
[[ -f target/classpath.txt ]] || { echo "Run bash scripts/build.sh first" >&2; exit 1; }
exec java -cp "target/classes:$(cat target/classpath.txt)" com.study.launcher.LocalServer
