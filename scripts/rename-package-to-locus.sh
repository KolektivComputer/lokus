#!/usr/bin/env bash
# Mechanical package move: cat.mey.platform / cat.mey.ktx → capital.yuri.locus
# Run from repo root on branch docker-local-dev (or after merge).
set -euo pipefail

ROOT="backend/api/src/main/kotlin"
OLD_PLATFORM="$ROOT/cat/mey/platform"
OLD_KTX="$ROOT/cat/mey/ktx"
NEW_ROOT="$ROOT/capital/yuri/locus"

if [[ ! -d "$OLD_PLATFORM" ]]; then
  echo "Nothing to rename (already moved?)."
  exit 0
fi

mkdir -p "$NEW_ROOT"

# Move sources
shopt -s dotglob nullglob
cp -a "$OLD_PLATFORM"/. "$NEW_ROOT"/
if [[ -d "$OLD_KTX" ]]; then
  mkdir -p "$NEW_ROOT/ktx"
  cp -a "$OLD_KTX"/. "$NEW_ROOT/ktx"/
fi

# Rewrite package / import declarations
while IFS= read -r -d '' f; do
  sed -i \
    -e 's/cat\.mey\.platform/capital.yuri.locus/g' \
    -e 's/cat\.mey\.ktx/capital.yuri.locus.ktx/g' \
    -e 's/cat\.mey\.core/capital.yuri.locus.core/g' \
    "$f"
done < <(find "$NEW_ROOT" -type f \( -name '*.kt' -o -name '*.kts' \) -print0)

# Remove old tree
rm -rf "$ROOT/cat"

echo "Done. Review with: git status && ./gradlew :backend:api:compileKotlin"
