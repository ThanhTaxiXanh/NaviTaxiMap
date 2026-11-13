#!/usr/bin/env python3
"""Exercise the real Android ACTION_OPEN_DOCUMENT UI with a local PMTiles file.

No UiAutomator test-library dependency or broad storage permissions are used.
Stores all picker screenshots/XML dumps as GitHub Actions evidence.
"""
import hashlib
import os
from pathlib import Path
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

OUT = Path(sys.argv[1] if len(sys.argv) > 1 else "smoke-evidence")
OUT.mkdir(parents=True, exist_ok=True)
EXPECTED_NAME = os.environ.get("NAVITAXI_SAF_NAME", "navitaxi-saf-test.pmtiles")
EXPECTED_HASH = os.environ.get("NAVITAXI_SAF_SHA256", "860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407")
EXPECTED_BYTES = int(os.environ.get("NAVITAXI_SAF_BYTES", "1268"))
UUID_FILE = re.compile(r"^[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}\.pmtiles$")

def adb(*args, binary=False, check=True):
    cmd = ["adb", *args]
    p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=35)
    if check and p.returncode:
        raise RuntimeError(f"{' '.join(cmd)} exited {p.returncode}: {p.stderr.decode(errors='replace')}")
    return p.stdout if binary else p.stdout.decode(errors="replace").replace("\r", "").strip()

def snapshot(stage):
    # UIAutomator writes a hierarchy accessible through adb pull even without root.
    remote = "/sdcard/navitaxi-picker.xml"
    last = None
    for _ in range(3):
        try:
            adb("shell", "uiautomator", "dump", remote)
            xml = adb("exec-out", "cat", remote, binary=True)
            if not xml.startswith(b"<?xml"):
                raise ValueError("Invalid uiautomator XML")
            (OUT / f"saf-{stage}.xml").write_bytes(xml)
            return ET.fromstring(xml)
        except Exception as e:
            last = e
            time.sleep(1)
    raise RuntimeError(f"Failed reading screen {stage}: {last}")

def screenshot(stage):
    raw = adb("exec-out", "screencap", "-p", binary=True)
    (OUT / f"saf-{stage}.png").write_bytes(raw)

def nodes(root):
    return list(root.iter("node"))

def candidate(node, labels, exact=False):
    values = [node.get("text", ""), node.get("content-desc", "")]
    for value in values:
        norm = value.strip().casefold()
        for label in labels:
            needle = label.casefold()
            if (norm == needle if exact else needle in norm):
                return True
    return False

def tap_node(node):
    match = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.get("bounds", ""))
    if not match:
        raise ValueError(f"Missing clickable bounds: {node.attrib}")
    x1, y1, x2, y2 = map(int, match.groups())
    if x2 <= x1 or y2 <= y1:
        raise ValueError(f"Empty clickable bounds: {node.attrib}")
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))

def select_text(stage, labels, timeout=15, exact=False):
    last_screen = None
    until = time.monotonic() + timeout
    while time.monotonic() < until:
        root = snapshot(stage)
        last_screen = root
        matches = [node for node in nodes(root) if candidate(node, labels, exact=exact)]
        if matches:
            # Most specific matching node is normally the actual text/view.
            tap_node(matches[-1])
            time.sleep(1.2)
            return
        time.sleep(1)
    screenshot(stage + "-missing")
    text_nodes = [(n.get("text"), n.get("content-desc")) for n in nodes(last_screen)] if last_screen is not None else []
    raise RuntimeError(f"Cannot find {labels} at {stage}; visible: {text_nodes[:90]}")

def installed_archives():
    result = adb("shell", "run-as", "vn.navitaximap", "ls", "files/offline-packs")
    return {f for f in result.splitlines() if UUID_FILE.fullmatch(f)}

def open_picker():
    select_text("app", ["Dữ liệu bản đồ"])
    select_text("menu", ["Nhập tệp PMTiles từ thiết bị"])
    screenshot("picker-start")
    snapshot("picker-start")

def find_pilot_file():
    until = time.monotonic() + 15
    while time.monotonic() < until:
        root = snapshot("picker-recent")
        # DocumentsUI exposes both a file title and a separate preview control.
        # The preview icon has content-desc "Preview the file ...", and selecting
        # that icon does NOT return the document URI to the app.
        hits = [n for n in nodes(root)
                if n.get("text", "").strip() == EXPECTED_NAME
                and n.get("resource-id", "").endswith(":id/title")]
        if hits:
            tap_node(hits[0])
            return
        time.sleep(1)

    # Recent files may omit files copied by adb. Navigate DocumentsUI's roots.
    root = snapshot("picker-before-downloads")
    shortcuts = ["Downloads", "Tệp tải xuống", "Tải xuống"]
    matches = [n for n in nodes(root) if candidate(n, shortcuts)]
    if not matches:
        select_text("picker-roots", ["Show roots", "Hiển thị thư mục", "Open navigation drawer"], timeout=5)
        root = snapshot("picker-root-menu")
        matches = [n for n in nodes(root) if candidate(n, shortcuts)]
    if not matches:
        raise RuntimeError("DocumentsUI did not expose a Downloads root")
    tap_node(matches[-1])
    time.sleep(1.5)
    # In the Downloads root, always select the title, not the preview icon.
    until = time.monotonic() + 20
    while time.monotonic() < until:
        root = snapshot("picker-downloads")
        titles = [n for n in nodes(root)
                  if n.get("text", "").strip() == EXPECTED_NAME
                  and n.get("resource-id", "").endswith(":id/title")]
        if titles:
            tap_node(titles[0])
            return
        time.sleep(1)
    raise RuntimeError("PMTiles document was not visible in Downloads")

def verify_ui():
    until = time.monotonic() + 30
    while time.monotonic() < until:
        root = snapshot("result")
        text = " ".join((n.get("text", "") or "") for n in nodes(root))
        if "Đã lưu PMTiles" in text and EXPECTED_NAME in text:
            screenshot("success")
            return
        if "Nhập không thành công" in text:
            screenshot("app-error")
            raise RuntimeError(f"Android UI reported import failed: {text}")
        time.sleep(1)
    screenshot("result-timeout")
    raise RuntimeError("Import success dialog not shown")

def verify_persisted(before):
    after = installed_archives()
    new = after - before
    if len(new) != 1:
        raise RuntimeError(f"Expected exactly one new SAF archive; before={before}, after={after}")
    ident = next(iter(new)).removesuffix(".pmtiles")
    archive = adb("exec-out", "run-as", "vn.navitaximap",
                  "cat", f"files/offline-packs/{ident}.pmtiles", binary=True)
    index = adb("exec-out", "run-as", "vn.navitaximap",
                "cat", f"files/offline-packs/{ident}.properties", binary=True)
    actual_hash = hashlib.sha256(archive).hexdigest()
    if len(archive) != EXPECTED_BYTES:
        raise AssertionError(f"SAF byte count mismatch: {len(archive)}")
    if actual_hash != EXPECTED_HASH:
        raise AssertionError(f"SAF-imported bytes mismatch: {actual_hash}")
    properties = index.decode("ISO-8859-1")
    for item in ["name=" + EXPECTED_NAME, "sha256=" + EXPECTED_HASH,
                 "sourceApproval=UNVERIFIED", "size=" + str(EXPECTED_BYTES)]:
        if item not in properties:
            raise AssertionError(f"Index missing {item}: {properties}")
    # Keep legacy evidence-bytes default. Bulk 34-province CI retains hashes,
    # metadata and screenshots instead of republishing hundreds of MB of PMTiles.
    if os.environ.get("NAVITAXI_SAF_PRESERVE_ARCHIVE", "1") == "1":
        (OUT / "saf-imported-pack.pmtiles").write_bytes(archive)
    (OUT / "saf-imported-pack.properties").write_bytes(index)
    print(f"SAF picker PASS: selected {EXPECTED_NAME} in system UI, stored {len(archive)} bytes, SHA-256={actual_hash}")

def main():
    before = installed_archives()
    try:
        open_picker()
        find_pilot_file()
        verify_ui()
        verify_persisted(before)
    except Exception as exc:
        print(f"SAF PICKER FAILED: {exc}", file=sys.stderr)
        try:
            screenshot("failure")
            root = snapshot("failure")
            print("Visible nodes:", [(n.get("text"), n.get("content-desc"))
                                     for n in nodes(root)][:100], file=sys.stderr)
        except Exception as evidence_error:
            print(f"Could not capture failure: {evidence_error}", file=sys.stderr)
        raise

if __name__ == "__main__":
    main()
