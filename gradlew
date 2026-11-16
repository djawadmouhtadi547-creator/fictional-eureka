#!/usr/bin/env sh
# DARKNULL-06 Gradle bootstrap wrapper.
# Downloads the pinned Gradle distribution when it is not already cached.
set -eu

GRADLE_VERSION="8.6"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-${HOME}/.gradle}"
DIST_ROOT="${GRADLE_USER_HOME}/wrapper/dists/darknull-bootstrap-${GRADLE_VERSION}"
DIST_HOME="${DIST_ROOT}/gradle-${GRADLE_VERSION}"
GRADLE_BIN="${DIST_HOME}/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
    mkdir -p "$DIST_ROOT"
    ZIP_FILE="${DIST_ROOT}/gradle-${GRADLE_VERSION}-bin.zip"
    echo "DARKNULL-06: downloading Gradle ${GRADLE_VERSION} (first run only)"
    if command -v curl >/dev/null 2>&1; then
        curl -fL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o "$ZIP_FILE"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$ZIP_FILE" "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
    else
        echo "Install curl or wget to download Gradle ${GRADLE_VERSION}." >&2
        exit 1
    fi
    if command -v unzip >/dev/null 2>&1; then
        unzip -q -o "$ZIP_FILE" -d "$DIST_ROOT"
    else
        echo "Install unzip to extract Gradle ${GRADLE_VERSION}." >&2
        exit 1
    fi
fi

exec "$GRADLE_BIN" "$@"
