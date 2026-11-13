#!/usr/bin/env bash
set -euo pipefail
APK="${1:?Debug APK}"
HANOI="${2:?Hanoi archive}"
HCM="${3:?Ho Chi Minh City archive}"
mkdir -p multi-region-evidence
adb install -r "$APK" | tee multi-region-evidence/install.txt
for record in "01 01-ha-noi.pmtiles $HANOI" "79 79-ho-chi-minh.pmtiles $HCM"; do
    read -r CODE NAME FILE <<< "$record"
    OUT="multi-region-evidence/$CODE"
    mkdir -p "$OUT"
    test -s "$FILE"
    case "$CODE" in
        01) SHA="ad6f2a78d2d7ca3d5763028965cc914195e61ec40ec4ad0b46db5d766c484139"; SIZE=17562631 ;;
        79) SHA="8a8634f1414f9953ca1fe30ce9e52cb5c8e897e3211a4637392f30094b6c214d"; SIZE=18217716 ;;
    esac
    test "$(stat -c %s "$FILE")" = "$SIZE"
    echo "$SHA  $FILE" | sha256sum -c -
    bash ci/push-emulator-download.sh "$FILE" "$NAME" | tee "$OUT/push.txt"
    # A successful adb push/length check is not sufficient: confirm the exact
    # SHA of bytes visible through Android emulated shared storage before SAF.
    DEVICE_SHA="$(adb shell sha256sum "/sdcard/Download/$NAME" | awk '{print $1}' | tr -d '\r')"
    test "$DEVICE_SHA" = "$SHA"
    echo "ANDROID_DOWNLOAD_SHA256_OK $NAME $DEVICE_SHA" | tee "$OUT/device-sha.txt"
    sleep 4
    adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d "file:///sdcard/Download/$NAME" > "$OUT/media-scan.txt" || true
    adb shell am force-stop vn.navitaximap || true
    adb shell am start -W -n vn.navitaximap/.MainActivity | tee "$OUT/import-launch.txt"
    sleep 6
    region_saf_once() {
        local evidence_log="${1:?SAF log}"
        NAVITAXI_SAF_NAME="$NAME" NAVITAXI_SAF_SHA256="$SHA" NAVITAXI_SAF_BYTES="$SIZE" \
            python3 ci/saf-picker-smoke.py "$OUT" > "$evidence_log" 2>&1
    }
    if region_saf_once "$OUT/import-first.log"; then
        echo "MULTI_REGION_SAF_FIRST_PASS code=$CODE"
    elif grep -q "Pixel Launcher isn't responding" "$OUT/import-first.log"; then
        # Retry exactly once after system Launcher ANR, not on corruption,
        # checksum failure, invalid PMTiles or missing offline roads.
        echo "MULTI_REGION_LAUNCHER_ANR_RETRY code=$CODE"
        adb shell input keyevent 4 || true
        adb shell am force-stop com.google.android.apps.nexuslauncher || true
        adb shell am force-stop vn.navitaximap || true
        adb shell am start -W -n vn.navitaximap/.MainActivity | tee "$OUT/retry-launch.txt"
        sleep 10
        if region_saf_once "$OUT/import-retry.log"; then
            echo "MULTI_REGION_SAF_RETRY_PASS code=$CODE"
        else
            tail -75 "$OUT/import-retry.log" >&2
            exit 1
        fi
    else
        tail -75 "$OUT/import-first.log" >&2
        exit 1
    fi
    (cat "$OUT/import-first.log"; test ! -f "$OUT/import-retry.log" || cat "$OUT/import-retry.log") |
        grep -E "SAF picker PASS|SAF STREAM|SAF_STREAM_SHA256_OK" | tee "$OUT/import-verified.txt"
    grep -q "SAF picker PASS: selected $NAME" "$OUT/import-verified.txt"
    adb shell am force-stop vn.navitaximap
    adb shell cmd connectivity airplane-mode enable
    adb shell svc wifi disable
    adb shell svc data disable
    adb logcat -c
    adb shell am start -W -n vn.navitaximap/.MainActivity \
        --ez vn.navitaximap.SHORTBREAD_PREVIEW true \
        --es vn.navitaximap.SHORTBREAD_REGION "$CODE" | tee "$OUT/offline-launch.txt"
    sleep 20
    adb shell screencap -p "/sdcard/shortbread-$CODE.png"
    adb pull "/sdcard/shortbread-$CODE.png" "$OUT/shortbread.png"
    adb shell uiautomator dump "/sdcard/shortbread-$CODE.xml" >/dev/null
    adb pull "/sdcard/shortbread-$CODE.xml" "$OUT/shortbread.xml"
    adb logcat -d -v brief > "$OUT/logcat.txt"
    python3 ci/check-shortbread-screenshot.py "$OUT/shortbread.png" | tee "$OUT/pixels.txt"
    grep -q "Shortbread 1.0" "$OUT/shortbread.xml"
    grep -q "OpenStreetMap" "$OUT/shortbread.xml"
    grep -q "KHÔNG dẫn đường" "$OUT/shortbread.xml"
    grep -q "SHORTBREAD_STYLE_LOADED $NAME" "$OUT/logcat.txt"
    ! grep -q "FATAL EXCEPTION" "$OUT/logcat.txt"
    adb shell pidof vn.navitaximap | tr -d '\r' | grep -Eq '^[0-9]+'
    echo "SHORTBREAD_MULTI_REGION_PASS code=$CODE file=$NAME bytes=$SIZE"
done
