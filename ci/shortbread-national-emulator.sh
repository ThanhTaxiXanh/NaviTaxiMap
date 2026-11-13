#!/usr/bin/env bash
# Real national 556MB PMTiles import: all SHA operations are streaming.
# No full-archive duplicate is stored in CI evidence or Python RAM.
set -euo pipefail
APK="${1:?APK}" SOURCE="${2:?Original national PMTiles}"
NAME="00-viet-nam-all.pmtiles"
SHA="678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55"
SIZE=556229998
OUT="national-evidence"
mkdir -p "$OUT"
test -s "$APK" && test -s "$SOURCE"
test "$(stat -c %s "$SOURCE")" = "$SIZE"
echo "$SHA  $SOURCE" | sha256sum -c - | tee "$OUT/source-sha256.txt"
adb install -r "$APK" | tee "$OUT/install.txt"
adb shell input keyevent 82 || true
sleep 12
bash ci/push-emulator-download.sh "$SOURCE" "$NAME" | tee "$OUT/push.txt"
REMOTE_SHA="$(adb shell sha256sum "/sdcard/Download/$NAME" | awk '{print $1}' | tr -d '\r')"
test "$REMOTE_SHA" = "$SHA"
echo "NATIONAL_ANDROID_DOWNLOAD_SHA256_OK bytes=$SIZE sha256=$REMOTE_SHA" | tee "$OUT/device-sha256.txt"
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE \
  -d "file:///sdcard/Download/$NAME" > "$OUT/media-scan.txt" || true
adb shell am start -W -n vn.navitaximap/.MainActivity | tee "$OUT/import-start.txt"
sleep 7
# The 556MB importer needs more time than the 30s normal-sized SAF fixture.
timeout -s TERM -k 5s 360s env NAVITAXI_SAF_NAME="$NAME" \
  NAVITAXI_SAF_SHA256="$SHA" NAVITAXI_SAF_BYTES="$SIZE" \
  NAVITAXI_SAF_VERIFY_STREAM=1 NAVITAXI_SAF_PRESERVE_ARCHIVE=0 \
  NAVITAXI_SAF_UI_TIMEOUT=180 \
  python3 ci/saf-picker-smoke.py "$OUT" | tee "$OUT/import-check.txt"
adb shell am force-stop vn.navitaximap
adb shell cmd connectivity airplane-mode enable
adb shell svc wifi disable
adb shell svc data disable
adb logcat -c
adb shell am start -W -n vn.navitaximap/.MainActivity \
  --ez vn.navitaximap.SHORTBREAD_PREVIEW true \
  --es vn.navitaximap.SHORTBREAD_REGION VN \
  --ez vn.navitaximap.SHORTBREAD_NATIONAL_DETAIL true | tee "$OUT/offline-start.txt"
sleep 16
for attempt in $(seq 1 24); do
  sleep 5
  adb shell screencap -p /sdcard/navitaxi-national-shortbread.png
  adb pull /sdcard/navitaxi-national-shortbread.png "$OUT/shortbread.png" >/dev/null
  if python3 ci/check-shortbread-screenshot.py "$OUT/shortbread.png" \
    > "$OUT/pixels-probe.txt" 2>&1; then
    echo "NATIONAL_OFFLINE_ROAD_PIXELS_READY attempt=$attempt"
    break
  fi
done
python3 ci/check-shortbread-screenshot.py "$OUT/shortbread.png" | tee "$OUT/pixels.txt"
adb shell uiautomator dump /sdcard/navitaxi-national.xml > /dev/null
adb pull /sdcard/navitaxi-national.xml "$OUT/screen.xml"
adb logcat -d -v brief > "$OUT/logcat.txt"
grep -q "Shortbread 1.0" "$OUT/screen.xml"
grep -q "OpenStreetMap" "$OUT/screen.xml"
grep -q "KHÔNG dẫn đường" "$OUT/screen.xml"
grep -q "SHORTBREAD_STYLE_LOADED $NAME" "$OUT/logcat.txt"
! grep -q "FATAL EXCEPTION" "$OUT/logcat.txt"
adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'
echo "NATIONAL_556MB_PM_TILES_OFFLINE_PASS bytes=$SIZE sha256=$SHA"
