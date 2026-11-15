#!/usr/bin/env bash
# Rootless installation for Ubuntu 24.04 amd64. Does not initialize or modify any database.
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/local-env.sh
if [[ "$(dpkg --print-architecture)" != amd64 ]]; then
    echo 'This helper requires Ubuntu 24.04 amd64; use your own JDK 17/Maven/database elsewhere.' >&2; exit 1
fi
if [[ -x "$JAVA_HOME/bin/java" && -x "$BOOKMANAGE_RUNTIME/apache-maven-3.9.11/bin/mvn" ]]; then
    echo "Runtime already exists at $BOOKMANAGE_RUNTIME; leaving it unchanged."; exit 0
fi
mkdir -p "$BOOKMANAGE_RUNTIME/downloads" "$BOOKMANAGE_RUNTIME/local"
cd "$BOOKMANAGE_RUNTIME/downloads"
apt-get download openjdk-17-jdk-headless openjdk-17-jre-headless mariadb-server-core \
    mariadb-client-core mariadb-server libaio1t64 liburing2 libpcre2-8-0 libnuma1 libsnappy1v5
for package in ./*.deb; do dpkg-deb -x "$package" "$BOOKMANAGE_RUNTIME/local"; done
maven_url=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.tar.gz
curl -fL --retry 2 "$maven_url" -o maven.tar.gz
curl -fL --retry 2 "$maven_url.sha512" -o maven.sha512
echo "$(cat maven.sha512)  maven.tar.gz" | sha512sum -c -
tar -xzf maven.tar.gz -C "$BOOKMANAGE_RUNTIME"
python3 - <<'PY'
from pathlib import Path
import os
root = Path(os.environ['BOOKMANAGE_RUNTIME'])
jdk = Path(os.environ['JAVA_HOME'])
for path in jdk.rglob('*'):
    if path.is_symlink():
        target = os.readlink(path)
        if target.startswith('/etc/java-17-openjdk/'):
            path.unlink()
            path.symlink_to(root / 'local' / target.lstrip('/'))
trust = jdk / 'lib/security/cacerts'
if trust.is_symlink():
    trust.unlink()
PY
# Debian packages normally generate the Java trust store during system installation.
# Build it from the host's CA bundle instead, without writing to /etc.
cat >BuildTrustStore.java <<'JAVA'
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.*;
class BuildTrustStore {
    public static void main(String[] args) throws Exception {
        KeyStore store = KeyStore.getInstance("JKS"); store.load(null, null);
        int i = 0;
        try (InputStream input = Files.newInputStream(Path.of(args[0]))) {
            for (var cert : CertificateFactory.getInstance("X.509").generateCertificates(input))
                store.setCertificateEntry("system-ca-" + i++, cert);
        }
        try (OutputStream output = Files.newOutputStream(Path.of(args[1]))) {
            store.store(output, "changeit".toCharArray());
        }
    }
}
JAVA
java BuildTrustStore.java /etc/ssl/certs/ca-certificates.crt "$JAVA_HOME/lib/security/cacerts"
java -version
mvn -version
echo "Runtime installed at $BOOKMANAGE_RUNTIME. No system services or database data were changed."
