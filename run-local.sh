#!/usr/bin/env sh
set -eu
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$BASE_DIR"
if [ -z "${JWT_SECRET_BASE64:-}" ]; then
  if ! command -v openssl >/dev/null 2>&1; then
    echo 'ERROR: install openssl or export JWT_SECRET_BASE64 (Base64 of 32 random bytes).' >&2
    exit 1
  fi
  JWT_SECRET_BASE64=$(openssl rand -base64 32 | tr -d '\n')
  export JWT_SECRET_BASE64
  echo 'Generated temporary JWT secret for local use.'
fi
exec ./mvnw spring-boot:run
