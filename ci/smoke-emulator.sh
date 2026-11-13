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


# Simulate power loss at different points of the two-file import transaction.
# Keep the previously committed PMTiles + metadata pair intact.
STAGING_ID=11111111-1111-1111-1111-111111111111
ORPHAN_ID=22222222-2222-2222-2222-222222222222
INDEX_ID=33333333-3333-3333-3333-333333333333
adb shell run-as vn.navitaximap touch "files/offline-packs/$STAGING_ID.partial"
adb shell run-as vn.navitaximap touch "files/offline-packs/$STAGING_ID.properties.partial"
adb shell run-as vn.navitaximap touch "files/offline-packs/$ORPHAN_ID.pmtiles"
adb shell run-as vn.navitaximap touch "files/offline-packs/$INDEX_ID.properties"
adb shell am force-stop vn.navitaximap
adb shell am start -W -n vn.navitaximap/.MainActivity --ez vn.navitaximap.OFFLINE_DEMO true | tee smoke-evidence/recovery-launch.txt
sleep 5
adb shell run-as vn.navitaximap ls files/offline-packs | tee smoke-evidence/recovered-pack-files.txt
python3 - <<'PY'
from pathlib import Path
before = Path("smoke-evidence/imported-pack-files.txt").read_text().splitlines()
after = Path("smoke-evidence/recovered-pack-files.txt").read_text().splitlines()
assert sorted(before) == sorted(after), f"Recovery changed valid packs or retained orphans: {after}"
assert len([s for s in after if s.endswith(".pmtiles")]) == 1
assert len([s for s in after if s.endswith(".properties")]) == 1
print("Crash recovery PASS: 4 simulated incomplete files cleared, original pair unchanged")
PY
adb exec-out run-as vn.navitaximap cat "files/offline-packs/$PACK_ID.pmtiles" > smoke-evidence/recovered-pack.pmtiles
cmp smoke-evidence/imported-pack.pmtiles smoke-evidence/recovered-pack.pmtiles
echo "Crash recovery PASS: committed PMTiles content is byte-identical after app restart."


# Real Android system picker flow. Push just one local fixture, never download OSM
# or public map tiles in CI. Keep the emulator in airplane mode.
python3 - <<'PY'
import base64
from pathlib import Path
data = base64.b64decode(Path("app/src/main/assets/offline_demo.pmtiles.b64").read_text().strip())
Path("smoke-evidence/navitaxi-saf-test.pmtiles").write_bytes(data)
PY
adb push smoke-evidence/navitaxi-saf-test.pmtiles /sdcard/Download/navitaxi-saf-test.pmtiles
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/navitaxi-saf-test.pmtiles > smoke-evidence/saf-scanner.txt || true
adb shell am force-stop vn.navitaximap
adb shell am start -W -n vn.navitaximap/.MainActivity | tee smoke-evidence/saf-launch.txt
sleep 6
python3 ci/saf-picker-smoke.py smoke-evidence | tee smoke-evidence/saf-result.txt


# Third map rendering mode: a tiny REAL OSM road-only archive from a pinned,
# attributed snapshot. No network access, no navigation or legal map claims.
adb shell am force-stop vn.navitaximap
adb logcat -c
adb shell am start -W -n vn.navitaximap/.MainActivity --ez vn.navitaximap.REAL_OSM_PILOT true | tee smoke-evidence/osm-pilot-launch.txt
sleep 15
adb shell screencap -p /sdcard/navitaxi-real-osm-roads.png
adb pull /sdcard/navitaxi-real-osm-roads.png smoke-evidence/osm-pilot-screenshot.png
adb shell uiautomator dump /sdcard/navitaxi-osm-pilot.xml > /dev/null
adb pull /sdcard/navitaxi-osm-pilot.xml smoke-evidence/osm-pilot-ui.xml
adb logcat -d -s AndroidRuntime:E > smoke-evidence/osm-pilot-runtime-errors.txt || true
adb logcat -d -v brief > smoke-evidence/osm-pilot-full-logcat.txt || true
python3 ci/check-osm-pilot-screenshot.py smoke-evidence/osm-pilot-screenshot.png
if ! grep -q 'OpenStreetMap' smoke-evidence/osm-pilot-ui.xml; then
    echo "Missing user-visible OpenStreetMap attribution in offline pilot UI" >&2
    exit 1
fi
if ! grep -q 'KHÔNG dẫn đường' smoke-evidence/osm-pilot-ui.xml; then
    echo "Missing prominent non-navigation warning for OSM sample" >&2
    exit 1
fi
if ! adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'; then
    echo "OSM road-preview process did not survive" >&2
    exit 1
fi
if grep -q 'FATAL EXCEPTION' smoke-evidence/osm-pilot-runtime-errors.txt; then
    echo "OSM road-preview crashed" >&2
    cat smoke-evidence/osm-pilot-runtime-errors.txt >&2
    exit 1
fi
echo "Real OSM road-only pilot PASS: offline geometry visible, ODbL attribution displayed."
