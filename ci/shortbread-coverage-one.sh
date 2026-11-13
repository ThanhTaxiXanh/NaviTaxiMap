#!/usr/bin/env bash
# Verify ONE genuine provincial PMTiles in an Android emulator, including SAF,
# end-to-end app-private SHA/byte count and a strict airplane-mode pixel proof.
set -euo pipefail
APK="${1:?APK}" PACK="${2:?Source province archive}"
CODE="${3:?Code}" NAME="${4:?File name}"
SIZE="${5:?Expected bytes}" SHA="${6:?Expected SHA256}"
OUT="${7:?Evidence directory}"
mkdir -p "$OUT"
[[ "$CODE" =~ ^[0-9]{2}$ && "$NAME" =~ ^[0-9]{2}-[a-z0-9-]+\.pmtiles$ ]]
[[ "$SHA" =~ ^[a-f0-9]{64}$ && "$SIZE" =~ ^[0-9]+$ ]]
test -s "$PACK"
test "$(stat -c %s "$PACK")" = "$SIZE"
echo "$SHA  $PACK" | sha256sum -c - | tee "$OUT/source-sha256.txt"
bash ci/push-emulator-download.sh "$PACK" "$NAME" | tee "$OUT/push.txt"
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE \
    -d "file:///sdcard/Download/$NAME" > "$OUT/media-scan.txt" || true
adb shell am force-stop vn.navitaximap || true
adb shell am start -W -n vn.navitaximap/.MainActivity | tee "$OUT/import-launch.txt"
sleep 5
NAVITAXI_SAF_NAME="$NAME" NAVITAXI_SAF_SHA256="$SHA" NAVITAXI_SAF_BYTES="$SIZE" \
  NAVITAXI_SAF_PRESERVE_ARCHIVE=0 \
  python3 ci/saf-picker-smoke.py "$OUT" | tee "$OUT/saf-result.txt"
adb shell am force-stop vn.navitaximap
adb shell cmd connectivity airplane-mode enable
adb shell svc wifi disable
adb shell svc data disable
adb logcat -c
adb shell am start -W -n vn.navitaximap/.MainActivity \
  --ez vn.navitaximap.SHORTBREAD_PREVIEW true \
  --es vn.navitaximap.SHORTBREAD_REGION "$CODE" | tee "$OUT/offline-launch.txt"
sleep 12
for attempt in $(seq 1 9); do
  sleep 5
  adb shell screencap -p "/sdcard/shortbread-$CODE.png"
  adb pull "/sdcard/shortbread-$CODE.png" "$OUT/shortbread.png" >/dev/null
  if python3 ci/check-shortbread-screenshot.py "$OUT/shortbread.png" \
       > "$OUT/pixel-probe.txt" 2>&1; then
    echo "SHORTBREAD_PROVINCE_PIXELS_READY code=$CODE attempt=$attempt"
    break
  fi
  echo "No real road pixels yet: $CODE attempt $attempt/9"
done
# The final assertion is mandatory even if every screenshot retry failed.
python3 ci/check-shortbread-screenshot.py "$OUT/shortbread.png" | tee "$OUT/pixels.txt"
adb shell uiautomator dump "/sdcard/shortbread-$CODE.xml" > /dev/null
adb pull "/sdcard/shortbread-$CODE.xml" "$OUT/shortbread.xml" >/dev/null
adb logcat -d -v brief > "$OUT/logcat.txt"
grep -q "Shortbread 1.0" "$OUT/shortbread.xml"
grep -q "OpenStreetMap" "$OUT/shortbread.xml"
grep -q "KHÔNG dẫn đường" "$OUT/shortbread.xml"
grep -q "SHORTBREAD_STYLE_LOADED $NAME" "$OUT/logcat.txt"
! grep -q "FATAL EXCEPTION" "$OUT/logcat.txt"
adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'
echo "SHORTBREAD_PROVINCE_OFFLINE_PASS code=$CODE file=$NAME bytes=$SIZE"
