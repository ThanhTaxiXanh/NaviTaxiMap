#!/usr/bin/env bash
# Four parallel CI emulator shards cover every one of the 34 real province files.
# The nationwide 556MB PMTiles is deliberately a separate capacity/QA task.
set -euo pipefail
APK="${1:?Debug APK}" PACK_DIR="${2:?Original artifacts}" SHARD="${3:?Shard 0-3}"
[[ "$SHARD" =~ ^[0-3]$ ]]
test -s "$APK"
test -d "$PACK_DIR"
LEDGER="tools/vietnam34/PACK_BYTES_VERIFIED.tsv"
test -s "$LEDGER"
ROOT="coverage-evidence/shard-$SHARD"
mkdir -p "$ROOT"
adb install -r "$APK" | tee "$ROOT/install.txt"
all=0
selected=0
passed=0
failed=0
while IFS=$'\t' read -r CODE NAME SIZE TILES SHA; do
  [[ "$CODE" == "code" || -z "$CODE" || "$CODE" == "VN" ]] && continue
  [[ "$CODE" =~ ^[0-9]{2}$ && "$NAME" =~ ^[0-9]{2}-[a-z0-9-]+\.pmtiles$ ]] || exit 2
  [[ "$SHA" =~ ^[a-f0-9]{64}$ && "$SIZE" =~ ^[0-9]+$ ]] || exit 2
  all=$((all + 1))
  (( (all - 1) % 4 == SHARD )) || continue
  selected=$((selected + 1))
  OUT="$ROOT/$CODE"
  mkdir -p "$OUT"
  SOURCE="$(find "$PACK_DIR" -type f -name "$NAME" -print -quit)"
  echo "PROVINCE_START code=$CODE archive=$NAME bytes=$SIZE tiles=$TILES" | tee -a "$ROOT/coverage.log"
  if [[ -n "$SOURCE" ]] && \
    bash ci/shortbread-coverage-one.sh "$APK" "$SOURCE" "$CODE" "$NAME" "$SIZE" "$SHA" "$OUT" \
      > "$OUT/run.log" 2>&1; then
    cat "$OUT/run.log" | grep -E 'SAF picker PASS|SHORTBREAD_ROAD_PIXELS|SHORTBREAD_PROVINCE_OFFLINE_PASS' || true
    echo "PROVINCE_PASS code=$CODE $NAME" | tee -a "$ROOT/coverage.log"
    passed=$((passed + 1))
  else
    echo "PROVINCE_FAIL code=$CODE $NAME" | tee -a "$ROOT/coverage.log"
    tail -50 "$OUT/run.log" 2>/dev/null || true
    failed=$((failed + 1))
  fi
done < "$LEDGER"
echo "COVERAGE_SHARD_END shard=$SHARD totalProvinces=$all selected=$selected passed=$passed failed=$failed" | tee -a "$ROOT/coverage.log"
test "$all" -eq 34
test "$selected" -ge 8
test "$failed" -eq 0
