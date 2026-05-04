#!/bin/bash
# Resolves the Git repo root and runs Gradle to build the KMP iOS framework (simulator, arm64).
# Xcode run-script env: https://help.apple.com/xcode/#/dev745c52c4c
set -euo pipefail
if [[ -n "${PROJECT_DIR:-}" ]]; then
  # Directory that contains the .xcodeproj (e.g. …/iosApp/MockcatSample)
  REPO_ROOT="$(cd "$PROJECT_DIR/../.." && pwd)"
elif [[ -n "${SRCROOT:-}" ]]; then
  REPO_ROOT="$(cd "$SRCROOT/../.." && pwd)"
else
  SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"
  REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
fi
cd "$REPO_ROOT"
if [[ -x ./gradlew ]]; then
  exec ./gradlew --no-daemon \
    :mockcat-logger-ui:linkDebugFrameworkIosSimulatorArm64 \
    :mockcat-logger-urlsession:linkDebugFrameworkIosSimulatorArm64
else
  echo "error: expected ./gradlew at $REPO_ROOT" >&2
  exit 1
fi
