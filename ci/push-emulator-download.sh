#!/usr/bin/env bash
# Android emulator can report sys.boot_completed before emulated storage is
# writable. Retry FUSE/Downloads readiness and then verify exact pushed bytes.
set -euo pipefail
SRC="${1:?Local archive}"
NAME="${2:?Safe PMTiles basename}"
[[ "$NAME" =~ ^[0-9]{2}-[a-z0-9-]+\.pmtiles$ ]] || {
  echo "Invalid PMTiles basename: $NAME" >&2
  exit 2
}
test -s "$SRC"
EXPECTED="$(stat -c %s "$SRC")"
REMOTE="/sdcard/Download/$NAME"
for attempt in $(seq 1 12); do
  adb wait-for-device
  if adb shell 'mkdir -p /sdcard/Download && echo ready > /sdcard/Download/.navitaxi-storage-ready && rm /sdcard/Download/.navitaxi-storage-ready' >/dev/null 2>&1; then
    if adb push "$SRC" "$REMOTE" >/dev/null 2>&1; then
      BYTES="$(adb shell stat -c %s "$REMOTE" 2>/dev/null | tr -d '\r' || true)"
      if [[ "$BYTES" == "$EXPECTED" ]]; then
        echo "ANDROID_STORAGE_PUSH_OK $NAME $BYTES bytes (attempt $attempt)"
        exit 0
      fi
      echo "Android pushed file size differs: $BYTES vs $EXPECTED" >&2
    fi
  fi
  echo "Android Download storage is not ready; retry $attempt/12" >&2
  sleep 5
done
echo "ANDROID_STORAGE_PUSH_FAIL $NAME: Download directory unavailable after retries" >&2
exit 1
