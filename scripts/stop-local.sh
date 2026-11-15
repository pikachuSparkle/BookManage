#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/local-env.sh
if [[ -f .runtime/app.pid ]]; then
    pid="$(cat .runtime/app.pid)"
    # Do not signal an unrelated process if a stale PID was reused.
    if [[ -r "/proc/$pid/cmdline" ]] && tr '\0' ' ' <"/proc/$pid/cmdline" | grep -q 'com.study.launcher.LocalServer'; then
        kill "$pid"
    fi
    rm .runtime/app.pid
fi
if [[ -S "$BOOKMANAGE_RUNTIME/mysql.sock" ]]; then
    "$BOOKMANAGE_RUNTIME/local/usr/bin/mariadb" --no-defaults \
        --socket="$BOOKMANAGE_RUNTIME/mysql.sock" --user="$(id -un)" -e SHUTDOWN
fi
echo 'Local application and database stopped; data preserved.'
