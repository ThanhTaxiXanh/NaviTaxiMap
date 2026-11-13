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
