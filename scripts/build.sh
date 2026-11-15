#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if ! command -v mvn >/dev/null 2>&1; then source scripts/local-env.sh; fi
mvn -B -ntp verify dependency:build-classpath -DincludeScope=compile -Dmdep.outputFile=target/classpath.txt
