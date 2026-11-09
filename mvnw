#!/usr/bin/env sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$BASE_DIR"
JAR=.mvn/wrapper/maven-wrapper.jar
URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar
if [ ! -f "$JAR" ]; then
  echo 'Downloading official Apache Maven Wrapper...'
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$JAR"
  else
    echo 'ERROR: curl or wget is needed for initial Maven Wrapper setup.' >&2
    exit 1
  fi
fi
exec java "-Dmaven.multiModuleProjectDirectory=$BASE_DIR" -cp "$JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
