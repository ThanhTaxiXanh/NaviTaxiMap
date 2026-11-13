#!/usr/bin/env bash
set -euo pipefail

APK="${1:-smoke-apk/app-debug.apk}"
mkdir -p smoke-evidence
test -s "$APK" || { echo "Missing APK at $APK" >&2; exit 1; }

adb logcat -c
adb install -r "$APK" | tee smoke-evidence/install.txt
adb shell am start -W -n vn.navitaximap/.MainActivity | tee smoke-evidence/launch.txt
sleep 12

adb shell screencap -p /sdcard/navitaximap-smoke.png
adb pull /sdcard/navitaximap-smoke.png smoke-evidence/screenshot.png
adb logcat -d -s AndroidRuntime:E > smoke-evidence/runtime-errors.txt || true
adb shell dumpsys activity activities > smoke-evidence/activity.txt || true

if ! adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'; then
  echo "App process did not survive launch" >&2
  cat smoke-evidence/runtime-errors.txt >&2
  exit 1
fi
if grep -q "FATAL EXCEPTION" smoke-evidence/runtime-errors.txt; then
  echo "AndroidRuntime recorded a fatal exception" >&2
  cat smoke-evidence/runtime-errors.txt >&2
  exit 1
fi
echo "Smoke PASS: installed, launched, survived 12 seconds and captured screenshot."

# Second launch: verify the app's synthetic PMTiles reader without online map tiles.
adb shell am force-stop vn.navitaximap
adb shell cmd connectivity airplane-mode enable | tee smoke-evidence/airplane-mode.txt
adb shell svc wifi disable
adb shell svc data disable
adb logcat -c
adb shell am start -W -n vn.navitaximap/.MainActivity --ez vn.navitaximap.OFFLINE_DEMO true | tee smoke-evidence/offline-launch.txt
sleep 15
adb shell screencap -p /sdcard/navitaximap-offline.png
adb pull /sdcard/navitaximap-offline.png smoke-evidence/offline-screenshot.png
adb shell uiautomator dump /sdcard/navitaxi-window.xml > /dev/null
adb pull /sdcard/navitaxi-window.xml smoke-evidence/offline-window.xml
adb logcat -d -s AndroidRuntime:E > smoke-evidence/offline-runtime-errors.txt || true
adb logcat -d -v brief > smoke-evidence/offline-full-logcat.txt || true
python3 ci/check-offline-screenshot.py smoke-evidence/offline-screenshot.png

if ! grep -q 'PMTiles offline' smoke-evidence/offline-window.xml; then
    echo "Offline demo status was not exposed in UI" >&2
    exit 1
fi
if ! adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'; then
    echo "Offline demo process exited" >&2
    exit 1
fi
if grep -q 'FATAL EXCEPTION' smoke-evidence/offline-runtime-errors.txt; then
    echo "Offline PMTiles launch crashed" >&2
    exit 1
fi
echo "Offline smoke PASS: started in airplane mode, UI status verified, screenshot saved."

# Phase 4 import smoke: test the same streaming store used by Android's document picker.
# Controlled DEBUG-only fixture intent avoids requiring interactive system-file-picker automation.
adb shell am force-stop vn.navitaximap
adb logcat -c
adb shell am start -W -n vn.navitaximap/.MainActivity --ez vn.navitaximap.DEBUG_IMPORT_FIXTURE true | tee smoke-evidence/import-launch.txt
sleep 8
adb logcat -d -s NaviTaxiMapImport:I NaviTaxiMapImport:E > smoke-evidence/import-logcat.txt
adb shell run-as vn.navitaximap ls files/offline-packs | tee smoke-evidence/imported-pack-files.txt
# Android logcat tag filters can omit buffered messages. Verify bytes, not a text log.
PACK_ID="$(sed -nE 's/^([0-9a-f-]{36})\.pmtiles$/\1/p' smoke-evidence/imported-pack-files.txt | head -n1)"
if [[ -z "$PACK_ID" ]]; then
  echo "No imported app-private archive found" >&2
  exit 1
fi
adb exec-out run-as vn.navitaximap cat "files/offline-packs/$PACK_ID.pmtiles" > smoke-evidence/imported-pack.pmtiles
adb exec-out run-as vn.navitaximap cat "files/offline-packs/$PACK_ID.properties" > smoke-evidence/imported-pack.properties
python3 - <<'PY'
from pathlib import Path
import hashlib
binary = Path("smoke-evidence/imported-pack.pmtiles").read_bytes()
index = Path("smoke-evidence/imported-pack.properties").read_text()
expected = "860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407"
actual = hashlib.sha256(binary).hexdigest()
assert actual == expected, f"Unexpected imported archive SHA-256: {actual}"
assert binary[:8] == b"PMTiles\x03"
assert "sha256=" + expected in index, "Missing persisted checksum"
assert "sourceApproval=UNVERIFIED" in index, "Source approval incorrectly elevated"
print(f"Imported PMTiles verified by bytes: {len(binary)} bytes, SHA-256={actual}")
PY
echo "Offline pack import PASS: imported bytes and metadata match expected fixture in airplane mode."
