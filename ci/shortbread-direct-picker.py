#!/usr/bin/env python3
"""Android system DocumentsUI proof for exact regional Shortbread catalog selection.

Reuses the existing SAF import verifier. A successful test requires the regional
dialog, exact original filename, full app-private SHA/byte check, and road-colored
pixels in airplane mode. It never reaches an online distribution endpoint.
"""
import importlib.util
import os
import re
from pathlib import Path
import subprocess
import sys
import time

SCRIPT = Path(__file__).with_name("saf-picker-smoke.py")
spec = importlib.util.spec_from_file_location("navitaxi_saf_picker", SCRIPT)
if spec is None or spec.loader is None:
    raise RuntimeError("SAF test module missing")
picker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(picker)

WRONG_REGION_NAME = "01-ha-noi.pmtiles"  # negative-test alias of the original Bac Ninh bytes

if picker.EXPECTED_NAME != "24-bac-ninh.pmtiles":
    raise ValueError("Direct region selection QA must use pinned Bac Ninh filename")
if picker.EXPECTED_HASH != "6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f":
    raise ValueError("Unexpected Bac Ninh SHA-256 test pin")


def select_bac_ninh_in_scrollable_catalog():
    # The 35-item Android AlertDialog is scrollable and Bắc Ninh is the
    # twelfth entry, commonly below the first screen. Never assume fixed
    # screen coordinates or silently select a different province.
    for page in range(9):
        root = picker.snapshot(f"region-catalog-page-{page}")
        matches = [node for node in picker.nodes(root)
                   if picker.candidate(node, ["24 · Bắc Ninh"])]
        if matches:
            picker.tap_node(matches[-1])
            time.sleep(1.2)
            return
        scrollables = [node for node in picker.nodes(root)
                       if node.get("scrollable") == "true"]
        candidates = []
        for node in scrollables:
            match = re.fullmatch(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
                                 node.get("bounds", ""))
            if match:
                x1, y1, x2, y2 = map(int, match.groups())
                if x2 - x1 > 50 and y2 - y1 > 150:
                    candidates.append((y2 - y1, (x1+x2)//2, y1, y2))
        if not candidates:
            raise RuntimeError("No scrollable 35-region catalog entries exposed")
        _, x, y1, y2 = max(candidates)
        picker.adb("shell", "input", "swipe", str(x), str(y2-35),
                   str(x), str(y1+35), "450")
        time.sleep(0.8)
    raise RuntimeError("Region 24 missing after scrolling exact catalog")


def open_direct_region_picker():
    picker.adb("shell", "rm", "-f", "/sdcard/navitaxi-picker.xml", check=False)
    picker.select_text("app", ["Dữ liệu bản đồ"])
    picker.select_text("menu", ["35 gói Shortbread"])
    select_bac_ninh_in_scrollable_catalog()
    picker.select_text("region-confirm", ["Chọn tệp trên thiết bị"])
    picker.screenshot("direct-region-picker-open")
    picker.snapshot("direct-region-picker-open")


def verify_wrong_region_rejected_before_import():
    """Choose Hanoi filename while the pinned selection is Bac Ninh.

    This intentionally mislabeled test document contains original Bac Ninh bytes.
    It is never installed as Hanoi, uploaded or exposed as a catalog source.
    """
    before = picker.installed_archives()
    original_name = picker.EXPECTED_NAME
    try:
        picker.EXPECTED_NAME = WRONG_REGION_NAME
        open_direct_region_picker()
        picker.find_pilot_file()  # exact DocumentsUI title, not preview icon
    finally:
        picker.EXPECTED_NAME = original_name

    until = time.monotonic() + 35
    while time.monotonic() < until:
        root = picker.snapshot("wrong-region-result")
        visible = " ".join((n.get("text", "") or "") for n in picker.nodes(root))
        if "Nhập không thành công" in visible:
            if "Đã chọn sai tệp" not in visible or "24-bac-ninh.pmtiles" not in visible:
                raise AssertionError("Wrong region was rejected for an unexpected reason: " + visible)
            picker.screenshot("wrong-region-rejected")
            break
        if "Đã lưu PMTiles" in visible:
            raise AssertionError("Wrong region document was incorrectly installed")
        time.sleep(1)
    else:
        raise AssertionError("Wrong-region SAF selection did not show explicit filename rejection")

    after = picker.installed_archives()
    if before != after:
        raise AssertionError(f"Wrong-region picker changed imported archives: {before} -> {after}")
    # Do not merely trust UI text. The app-private index or staging files
    # must not have been written by the rejected selection.
    entries = picker.adb("shell", "run-as", "vn.navitaximap",
                         "ls", "-A", "files/offline-packs")
    if entries.strip():
        raise AssertionError("Rejected region left app-private offline data: " + entries)
    picker.select_text("dismiss-wrong-region", ["Đóng"], timeout=15)
    print("SHORTBREAD_WRONG_REGION_REJECTED_PASS wrong=" + WRONG_REGION_NAME +
          " expected=24-bac-ninh.pmtiles no-app-private-archive")


def main():
    picker.open_picker = open_direct_region_picker
    verify_wrong_region_rejected_before_import()
    picker.main()  # DocumentsUI selected exact pin, app-private full SHA and size
    picker.select_text("dismiss-import-confirmation", ["Đóng"], timeout=20)

    # The region picker automatically opens the verified local Shortbread style.
    # Verify it visually here, WITHOUT app restart or any network transport.
    screenshot = picker.OUT / "saf-direct-shortbread.png"
    last = ""
    for attempt in range(1, 11):
        time.sleep(5)
        picker.screenshot("direct-shortbread")
        snapshot = picker.OUT / "saf-direct-shortbread.png"
        result = subprocess.run(
            [sys.executable, "ci/check-shortbread-screenshot.py", str(snapshot)],
            capture_output=True, text=True, timeout=15,
        )
        if result.returncode == 0:
            last = result.stdout.strip()
            break
        last = result.stdout + result.stderr
        print(f"Direct regional preview not yet ready: {attempt}/10")
    else:
        raise AssertionError("Direct region import has no offline road pixels: " + last)

    root = picker.snapshot("direct-shortbread-success")
    visible = " ".join((node.get("text", "") or "") for node in picker.nodes(root))
    for label in ["Shortbread 1.0", "OpenStreetMap", "KHÔNG dẫn đường"]:
        if label not in visible:
            raise AssertionError(f"Missing offline style attribution/status: {label}")
    logs = picker.adb("logcat", "-d", "-v", "brief")
    if "SHORTBREAD_STYLE_LOADED 24-bac-ninh.pmtiles" not in logs:
        raise AssertionError("App did not load the pinned region's local style")
    if "FATAL EXCEPTION" in logs:
        raise AssertionError("Android app fatal exception")
    print("SHORTBREAD_DIRECT_REGION_PICKER_OFFLINE_PASS " + last)


if __name__ == "__main__":
    main()
