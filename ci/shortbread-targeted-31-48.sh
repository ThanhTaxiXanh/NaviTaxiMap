#!/usr/bin/env bash
# Confirm Hai Phong 31 and Da Nang 48 on real original offline archives.
# One failing emulator must never stall the whole job for 45 minutes.
set -euo pipefail
APK="${1:?Debug APK}"
SOURCE_DIR="${2:?Original 34-province artifact}"
test -s "$APK"
test -d "$SOURCE_DIR"
test -s tools/vietnam34/PACK_BYTES_VERIFIED.tsv
mkdir -p targeted-evidence
adb install -r "$APK" | tee targeted-evidence/install.txt
adb shell input keyevent 82 || true
sleep 12
passed=0
for CODE in 31 48; do
  ROW="$(awk -F '\t' -v code="$CODE" '$1 == code {print}' tools/vietnam34/PACK_BYTES_VERIFIED.tsv)"
  [[ -n "$ROW" ]] || { echo "Missing exact SHA ledger: $CODE" >&2; exit 1; }
  IFS=$'\t' read -r REGION NAME BYTES TILES SHA <<< "$ROW"
  [[ "$REGION" == "$CODE" && "$SHA" =~ ^[a-f0-9]{64}$ ]]
  FILE="$(find "$SOURCE_DIR" -type f -name "$NAME" -print -quit)"
  test -s "$FILE"
  OUT="targeted-evidence/$CODE"
  mkdir -p "$OUT"
  echo "TARGETED_PROVINCE_START code=$CODE name=$NAME bytes=$BYTES tiles=$TILES"
  RESULT=1
  if timeout -s TERM -k 5s 180s \
    bash ci/shortbread-coverage-one.sh "$APK" "$FILE" "$CODE" "$NAME" "$BYTES" "$SHA" "$OUT" \
      </dev/null >"$OUT/run.log" 2>&1; then
    RESULT=0
  else
    status=$?
    echo "TARGETED_ATTEMPT_FAILURE code=$CODE exit=$status" | tee "$OUT/initial-failure.txt"
    if grep -q "Pixel Launcher isn't responding" "$OUT/run.log"; then
      # Only one recovery if Android Launcher, NOT map/hash validation, hung.
      echo "TARGETED_LAUNCHER_ANR_RECOVERY code=$CODE"
      adb shell input keyevent 4 || true
      adb shell am force-stop com.google.android.apps.nexuslauncher || true
      adb shell am force-stop vn.navitaximap || true
      sleep 10
      if timeout -s TERM -k 5s 180s \
        bash ci/shortbread-coverage-one.sh "$APK" "$FILE" "$CODE" "$NAME" "$BYTES" "$SHA" "$OUT" \
          </dev/null >"$OUT/retry.log" 2>&1; then
        RESULT=0
      fi
    fi
  fi
  if (( RESULT == 0 )); then
    grep -E 'SAF picker PASS|SHORTBREAD_ROAD_PIXELS|SHORTBREAD_PROVINCE_OFFLINE_PASS' \
      "$OUT/run.log" "$OUT/retry.log" 2>/dev/null || true
    echo "TARGETED_PROVINCE_PASS code=$CODE name=$NAME" | tee -a targeted-evidence/coverage.log
    passed=$((passed + 1))
  else
    echo "TARGETED_PROVINCE_FAIL code=$CODE name=$NAME" | tee -a targeted-evidence/coverage.log
    tail -45 "$OUT/run.log" || true
    tail -45 "$OUT/retry.log" 2>/dev/null || true
  fi
done
echo "TARGETED_COVERAGE_RESULT passed=$passed expected=2" | tee -a targeted-evidence/coverage.log
test "$passed" -eq 2
