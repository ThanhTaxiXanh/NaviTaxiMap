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
# Never alter original data/ZIP: mutate one byte in a temporary runner copy,
# preserving PMTiles v3 header, filename and exact byte length.
TAMPER_DIR="$(mktemp -d)"
trap 'rm -rf "$TAMPER_DIR"' EXIT
TAMPERED="$TAMPER_DIR/24-bac-ninh.pmtiles"
python3 - "$PACK" "$TAMPERED" <<'PY'
import pathlib
import shutil
import sys
source, destination = map(pathlib.Path, sys.argv[1:])
shutil.copyfile(source, destination)
with destination.open("r+b") as f:
    f.seek(-4096, 2)
    original = f.read(1)
    assert len(original) == 1
    f.seek(-4096, 2)
    f.write(bytes([original[0] ^ 1]))
assert destination.stat().st_size == source.stat().st_size
assert destination.open("rb").read(127) == source.open("rb").read(127)
PY
TAMPERED_SHA="$(sha256sum "$TAMPERED" | cut -d' ' -f1)"
test "$TAMPERED_SHA" != "$SHA"
test "$(stat -c %s "$TAMPERED")" = "$SIZE"
cmp -n 127 "$PACK" "$TAMPERED"
echo "SHORTBREAD_TEST_COPY_TAMPERED bytes=$SIZE header-preserved SHA-differs"
adb install -r "$APK" | tee shortbread-evidence/install.txt
bash ci/push-emulator-download.sh "$TAMPERED" "24-bac-ninh.pmtiles"
# Misnamed 01 archive is deliberately a NEGATIVE-TEST ALIAS of Bac Ninh bytes.
# The app must refuse it before streaming, never promote it as Hanoi data.
bash ci/push-emulator-download.sh "$PACK" "01-ha-noi.pmtiles"
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/24-bac-ninh.pmtiles > shortbread-evidence/media-scan.txt || true
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/01-ha-noi.pmtiles >> shortbread-evidence/media-scan.txt || true
# Import via the 35-region picker with ALL network transports disabled.
# Android DocumentsUI uses the exact file already copied into Downloads.
adb shell cmd connectivity airplane-mode enable
adb shell svc wifi disable
adb shell svc data disable
adb shell am start -W -n vn.navitaximap/.MainActivity
sleep 7
python3 -m py_compile ci/shortbread-direct-picker.py
NAVITAXI_SAF_NAME="24-bac-ninh.pmtiles" NAVITAXI_SAF_SHA256="$SHA" NAVITAXI_SAF_BYTES="$SIZE" \
 NAVITAXI_TAMPERED_SHA256="$TAMPERED_SHA" NAVITAXI_BAC_NINH_ORIGINAL_PACK="$PACK" \
 NAVITAXI_SAF_PRESERVE_ARCHIVE=0 \
 python3 ci/shortbread-direct-picker.py shortbread-evidence | tee shortbread-evidence/saf-result.txt
adb shell am force-stop vn.navitaximap
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
# After successful genuine original rendering, independently prove that a
# subsequent one-byte mutation in the app-private imported COPY is refused.
# Do not change source artifact, user Downloads original or cloud backups.
python3 -m py_compile ci/shortbread-post-import-tamper.py
NAVITAXI_SAF_NAME="24-bac-ninh.pmtiles" NAVITAXI_SAF_SHA256="$SHA" NAVITAXI_SAF_BYTES="$SIZE" \
 python3 ci/shortbread-post-import-tamper.py shortbread-evidence | tee shortbread-evidence/private-post-import-result.txt
