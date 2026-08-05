#!/usr/bin/env bash
# Build and start the local locus stack (Postgres + API).
# Passes git commit/tag into the image when available so version.json is useful
# even though the Alpine JDK build image has no git binary.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

BUILD_ARGS=()

if command -v git >/dev/null 2>&1 && git -C "$ROOT/.." rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  REPO="$(cd "$ROOT/.." && pwd)"
  COMMIT="$(git -C "$REPO" rev-parse HEAD 2>/dev/null || true)"
  TAG="$(git -C "$REPO" describe --tags --exact-match 2>/dev/null || true)"
  if [[ -n "${COMMIT}" ]]; then
    BUILD_ARGS+=(--build-arg "LOCUS_COMMIT=${COMMIT}")
  fi
  if [[ -n "${TAG}" ]]; then
    BUILD_ARGS+=(--build-arg "LOCUS_TAG=${TAG}")
  fi
fi

if [[ -n "${LOCUS_UPDATE_URL:-}" ]]; then
  BUILD_ARGS+=(--build-arg "LOCUS_UPDATE_URL=${LOCUS_UPDATE_URL}")
fi

echo "→ docker compose build ${BUILD_ARGS[*]:-}"
docker compose build "${BUILD_ARGS[@]}"

echo "→ docker compose up"
exec docker compose up "$@"
