#!/usr/bin/env bash
set -euo pipefail
APK="${1:?APK file}"
PACK="${2:?Bac Ninh PMTiles}"
SHA="6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f"
mkdir -p shortbread-evidence
test -s "$APK"
test -s "$PACK"
echo "$SHA  $PACK" | sha256sum -c -
SIZE="$(stat -c %s "$PACK")"
adb install -r "$APK" | tee shortbread-evidence/install.txt
adb push "$PACK" /sdcard/Download/24-bac-ninh.pmtiles
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/24-bac-ninh.pmtiles > shortbread-evidence/media-scan.txt || true
adb shell am start -W -n vn.navitaximap/.MainActivity
sleep 7
NAVITAXI_SAF_NAME="24-bac-ninh.pmtiles" NAVITAXI_SAF_SHA256="$SHA" NAVITAXI_SAF_BYTES="$SIZE" \
 python3 ci/saf-picker-smoke.py shortbread-evidence | tee shortbread-evidence/saf-result.txt
adb shell am force-stop vn.navitaximap
adb shell cmd connectivity airplane-mode enable
adb shell svc wifi disable
adb shell svc data disable
adb logcat -c
adb shell am start -W -n vn.navitaximap/.MainActivity --ez vn.navitaximap.SHORTBREAD_PREVIEW true | tee shortbread-evidence/offline-start.txt
sleep 20
adb shell screencap -p /sdcard/shortbread-bacninh.png
adb pull /sdcard/shortbread-bacninh.png shortbread-evidence/shortbread-bacninh.png
adb shell uiautomator dump /sdcard/shortbread.xml >/dev/null
adb pull /sdcard/shortbread.xml shortbread-evidence/shortbread.xml
adb logcat -d -v brief > shortbread-evidence/logcat.txt
python3 ci/check-shortbread-screenshot.py shortbread-evidence/shortbread-bacninh.png
grep -q "Shortbread 1.0" shortbread-evidence/shortbread.xml
grep -q "OpenStreetMap" shortbread-evidence/shortbread.xml
grep -q "KHÔNG dẫn đường" shortbread-evidence/shortbread.xml
grep -q "SHORTBREAD_STYLE_LOADED" shortbread-evidence/logcat.txt
! grep -q "FATAL EXCEPTION" shortbread-evidence/logcat.txt
adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'
echo "SHORTBREAD_BAC_NINH_OFFLINE_RENDER_PASS"
