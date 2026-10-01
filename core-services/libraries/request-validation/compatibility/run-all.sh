#!/usr/bin/env bash
# Builds request-validation once (JDK 17) and tests the same jar on every supported stack.
# Override the JDK locations with JAVA8_HOME / JAVA17_HOME.
set -euo pipefail

JAVA8_HOME="${JAVA8_HOME:-/usr/lib/jvm/java-8-openjdk-amd64}"
JAVA17_HOME="${JAVA17_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
LIBRARY="$(cd "$(dirname "$0")/.." && pwd)"

echo "== build and install request-validation (JDK 17)"
JAVA_HOME="$JAVA17_HOME" mvn -q -f "$LIBRARY/pom.xml" clean install

run() { # profile, JDK home
  echo "== $1 ($2)"
  JAVA_HOME="$2" mvn -q -f "$LIBRARY/compatibility/pom.xml" "-P$1" clean test
}
run boot-1.5 "$JAVA8_HOME"
run boot-2.2 "$JAVA8_HOME"
run boot-3.2 "$JAVA17_HOME"
run boot-3.4 "$JAVA17_HOME"
echo "== all stacks passed"
