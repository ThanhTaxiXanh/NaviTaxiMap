#!/usr/bin/env python3
"""Cold-start refusal for a previously verified but subsequently modified archive.

This runs only on a disposable Android emulator, with no user archives, cloud
backup objects, endpoints or original ZIPs modified. After successful offline
Shortbread screenshot QA, change one byte in the app-private *imported copy*.
A restarted app must fail SHA-256 verification BEFORE MapLibre opens that file.
"""
import importlib.util
import os
from pathlib import Path
import subprocess
import sys
import time

SPEC = importlib.util.spec_from_file_location(
    "navitaxi_saf_picker", Path(__file__).with_name("saf-picker-smoke.py")
)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("Native SAF test utilities unavailable")
picker = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(picker)

EXPECTED_NAME = "24-bac-ninh.pmtiles"
EXPECTED_HASH = "6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f"
EXPECTED_SIZE = 11049420
APP = "vn.navitaximap"


def private_file_sha(path):
    result = picker.adb("shell", "run-as", APP, "sha256sum", path)
    fields = result.split()
    if len(fields) < 2 or len(fields[0]) != 64:
        raise AssertionError("App-private SHA-256 command returned no digest")
    return fields[0].lower()


def private_size(path):
    return int(picker.adb("shell", "run-as", APP, "stat", "-c", "%s", path))


def main():
    # The primary smoke has already imported and rendered the real source.
    archives = picker.installed_archives()
    if len(archives) != 1:
        raise AssertionError(f"Expected one real original imported pack, got {len(archives)}")
    name = next(iter(archives))
    relative = "files/offline-packs/" + name
    archive_id = name.removesuffix(".pmtiles")
    index = picker.adb("exec-out", "run-as", APP,
                       "cat", f"files/offline-packs/{archive_id}.properties", binary=True)
    for item in [b"name=24-bac-ninh.pmtiles", b"sha256=" + EXPECTED_HASH.encode()]:
        if item not in index:
            raise AssertionError("App-private index does not match pinned original")
    if private_size(relative) != EXPECTED_SIZE:
        raise AssertionError("Original app-private archive byte count wrong")
    if private_file_sha(relative) != EXPECTED_HASH:
        raise AssertionError("Original app-private archive was not verified before mutation")

    # The external original in Downloads MUST remain completely unchanged.
    downloaded = picker.adb("shell", "sha256sum",
                            "/sdcard/Download/" + EXPECTED_NAME).split()[0].lower()
    if downloaded != EXPECTED_HASH:
        raise AssertionError("Android Downloads original changed unexpectedly")
    print("SHORTBREAD_PRE_TAMPER_PRIVATE_SHA_PASS")

    offset = EXPECTED_SIZE - 4096
    # Android toybox dd through 'adb exec-out run-as' is not a reliable
    # one-byte readback: it may return empty stdout even with a valid pack.
    # The integrity oracle is the entire pinned SHA-256, not a byte probe.
    # Try a deterministic zero byte first; on rare no-change, use random.
    for attempt in range(12):
        source = "/dev/zero" if attempt == 0 else "/dev/urandom"
        picker.adb("shell", "run-as", APP, "dd",
                   "if=" + source, "of=" + relative,
                   "bs=1", "seek=" + str(offset),
                   "count=1", "conv=notrunc")
        if private_file_sha(relative) != EXPECTED_HASH:
            break
    else:
        raise AssertionError("Emulator test did not change private archive SHA-256")
    if private_size(relative) != EXPECTED_SIZE:
        raise AssertionError("Tamper test accidentally resized private archive")
    if picker.installed_archives() != archives:
        raise AssertionError("Tamper test altered offline pack catalog/index")
    if picker.adb("shell", "sha256sum",
                  "/sdcard/Download/" + EXPECTED_NAME).split()[0].lower() != EXPECTED_HASH:
        raise AssertionError("Tamper test touched the original Downloads document")
    print("SHORTBREAD_POST_IMPORT_PRIVATE_COPY_TAMPERED_SHA_DIFFERS")

    # Force a cold start. The debug preview uses the EXACT same production
    # verifyPack -> showVerifiedShortbread path as the 35-entry catalog.
    picker.adb("shell", "am", "force-stop", APP)
    picker.adb("logcat", "-c")
    picker.adb("shell", "am", "start", "-W",
               "-n", APP + "/.MainActivity",
               "--ez", "vn.navitaximap.SHORTBREAD_PREVIEW", "true")
    result = None
    for attempt in range(1, 13):
        time.sleep(2)
        root = picker.snapshot("private-corrupt-cold-start")
        visible = " ".join((node.get("text", "") or "")
                           for node in picker.nodes(root))
        if "Không mở được gói Shortbread:" in visible:
            result = visible
            break
    if result is None:
        picker.screenshot("private-corrupt-no-refusal")
        raise AssertionError("Corrupt stored PMTiles cold-start did not report verification failure")
    if "SHA-256 / PMTiles bị thay đổi" not in result:
        raise AssertionError("Stored file was rejected for an unexpected reason: " + result)
    if "Shortbread 1.0 · Bắc Ninh" in result:
        raise AssertionError("Invalid stored region displayed as verified offline map")

    picker.screenshot("private-corrupt-refused")
    log = picker.adb("logcat", "-d", "-v", "brief")
    if "SHORTBREAD_VERIFY_FAIL" not in log:
        raise AssertionError("Missing production SHA verification failure marker")
    if "SHORTBREAD_STYLE_LOADED" in log:
        raise AssertionError("MapLibre received a style from corrupt stored PMTiles")
    if "FATAL EXCEPTION" in log:
        raise AssertionError("App crashed after stored file SHA mismatch")
    screenshot = picker.OUT / "saf-private-corrupt-refused.png"
    image_probe = subprocess.run(
        [sys.executable, "ci/check-shortbread-screenshot.py", str(screenshot)],
        capture_output=True, text=True, timeout=20,
    )
    if image_probe.returncode == 0:
        raise AssertionError("Map screenshot still has road pixels after cold-start refusal")

    if picker.installed_archives() != archives:
        raise AssertionError("Invalid-file refusal unexpectedly deleted or promoted packs")
    if private_size(relative) != EXPECTED_SIZE:
        raise AssertionError("Refusal unexpectedly altered stored file size")
    if private_file_sha(relative) == EXPECTED_HASH:
        raise AssertionError("Stored corruption disappeared before verification")
    print("SHORTBREAD_POST_IMPORT_TAMPER_COLD_START_REFUSED_PASS " +
          "no-corrupt-style no-offline-road-pixels original-download-intact")


if __name__ == "__main__":
    main()
