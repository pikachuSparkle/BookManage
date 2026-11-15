#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/local-env.sh
mkdir -p .runtime
if [[ ! -d "$BOOKMANAGE_RUNTIME/data/mysql" ]]; then
    echo 'Database is not initialized. See README.md.' >&2
    exit 1
fi
client="$BOOKMANAGE_RUNTIME/local/usr/bin/mariadb"
socket="$BOOKMANAGE_RUNTIME/mysql.sock"
if ! "$client" --no-defaults --socket="$socket" --user="$(id -un)" -e 'SELECT 1' >/dev/null 2>&1; then
    nohup "$BOOKMANAGE_RUNTIME/local/usr/sbin/mariadbd" --no-defaults \
        --basedir="$BOOKMANAGE_RUNTIME/local/usr" --datadir="$BOOKMANAGE_RUNTIME/data" \
        --socket="$socket" --pid-file="$BOOKMANAGE_RUNTIME/mysql.pid" \
        --bind-address=127.0.0.1 --port=3307 --skip-name-resolve \
        --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci \
        --log-error="$BOOKMANAGE_RUNTIME/mysql.log" >.runtime/database-console.log 2>&1 &
    ready=false
    for attempt in {1..50}; do
        if "$client" --no-defaults --socket="$socket" --user="$(id -un)" -e 'SELECT 1' >/dev/null 2>&1; then
            ready=true; break
        fi
        sleep 0.2
    done
    if [[ "$ready" != true ]]; then
        echo "Database did not start; inspect $BOOKMANAGE_RUNTIME/mysql.log" >&2; exit 1
    fi
fi
if [[ "${1:-}" == --database-only ]]; then
    echo 'Local database ready on 127.0.0.1:3307.'; exit 0
fi
if [[ -f .env.local ]]; then
    set -a; source .env.local; set +a
fi
: "${DB_PASSWORD:?Set DB_PASSWORD or create .env.local first}"
[[ -f target/classpath.txt ]] || { echo 'Run bash scripts/build.sh first' >&2; exit 1; }
if [[ -f .runtime/app.pid ]] && kill -0 "$(cat .runtime/app.pid)" 2>/dev/null; then
    echo "Application is already running (PID $(cat .runtime/app.pid))."; exit 0
fi
nohup bash scripts/run.sh >.runtime/application.log 2>&1 &
echo "$!" >.runtime/app.pid
url="http://127.0.0.1:${APP_PORT:-8080}${APP_CONTEXT_PATH:-}/login"
for attempt in {1..100}; do
    if curl -fsS "$url" >/dev/null 2>&1; then
        echo "Application ready: $url"; exit 0
    fi
    if ! kill -0 "$(cat .runtime/app.pid)" 2>/dev/null; then break; fi
    sleep 0.2
done
echo 'Application failed to become ready; inspect .runtime/application.log' >&2
exit 1
