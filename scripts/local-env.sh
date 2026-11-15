#!/usr/bin/env bash
# Source this file; all software and data live under the current user's home directory.
export BOOKMANAGE_RUNTIME="${BOOKMANAGE_RUNTIME:-$HOME/.local/share/bookmanage-runtime}"
export JAVA_HOME="$BOOKMANAGE_RUNTIME/local/usr/lib/jvm/java-17-openjdk-amd64"
export PATH="$JAVA_HOME/bin:$BOOKMANAGE_RUNTIME/apache-maven-3.9.11/bin:$PATH"
export LD_LIBRARY_PATH="$BOOKMANAGE_RUNTIME/local/usr/lib/x86_64-linux-gnu${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"
