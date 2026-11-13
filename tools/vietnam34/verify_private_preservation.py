#!/usr/bin/env python3
"""Verify locally preserved ORIGINAL Shortbread archives without uploading or copying.

Supports the two private GitHub Actions artifact ZIP files, or folders extracted
from them. Hashes the full uncompressed bytes using bounded memory. A receipt
contains hashes and sizes, NOT a backup or an approval to distribute the maps.

Example after manually downloading the two original artifacts:
  python3 tools/vietnam34/verify_private_preservation.py \
    --provinces NaviTaxiMap-34-Provinces-OSM-UNREVIEWED.zip \
    --national NaviTaxiMap-Vietnam-Whole-Country-OSM-UNREVIEWED.zip \
    --receipt local-preservation-receipt.json

Do NOT commit, upload, publish or release the 35 original PMTiles files.
"""
import argparse
import csv
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import re
import stat
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parent
HEADER = b"PMTiles\x03"
NAME = re.compile(r"^(?:00|[0-9]{2})-[a-z0-9-]+\.pmtiles$")
HASH = re.compile(r"^[0-9a-f]{64}$")
CHUNK = 1024 * 1024

def require(condition, message="Original PMTiles verification failed"):
    """Fail closed even with Python -O, which strips all normal assert checks."""
    if not condition:
        raise ValueError(message)

def pinned_ledger(folder=ROOT):
    """Validate three repo records, not unchecked command-line checksums."""
    ledger = folder / "PACK_BYTES_VERIFIED.tsv"
    sums_file = folder / "SHA256SUMS.txt"
    with ledger.open(encoding="utf-8", newline="") as f:
        rows = list(csv.DictReader(f, delimiter="\t"))
    require(len(rows) == 35, "The authoritative ledger must have 35 pins")
    by_name = {}
    by_code = {}
    for row in rows:
        code, name, sha = row["code"], row["file"], row["sha256"]
        require(NAME.fullmatch(name) and HASH.fullmatch(sha), "Malformed pack pin")
        require((code == "VN" and name.startswith("00-")) or (
            re.fullmatch(r"[0-9]{2}", code) is not None
            and name.startswith(code + "-")
        ), "Pack file name/code mismatch")
        count, tiles = int(row["size_bytes"]), int(row["tile_count"])
        require(count >= 127 and tiles > 0, "Bad original size/tile count")
        require(name not in by_name and code not in by_code, "Duplicate pack identity")
        by_name[name], by_code[code] = row, row
    require(len(by_code) == 35 and "VN" in by_code, "Wrong number of pinned regions")
    require(sum(int(x["size_bytes"]) for x in rows if x["code"] != "VN") == 287_504_805,
            "Provincial original total bytes mismatch")
    require(int(by_code["VN"]["size_bytes"]) == 556_229_998,
            "National original total bytes mismatch")
    actual_sums = {}
    for line in sums_file.read_text(encoding="utf-8").splitlines():
        m = re.fullmatch(r"([0-9a-f]{64})  ([0-9]{2}-[a-z0-9-]+\.pmtiles)", line)
        require(m, "Malformed SHA256SUMS entry")
        require(m.group(2) not in actual_sums, "Duplicate SHA256SUMS name")
        actual_sums[m.group(2)] = m.group(1)
    require(actual_sums == {n: row["sha256"] for n, row in by_name.items()},
            "SHA256SUMS differs from authoritative byte ledger")

    # Third independent repository pin: the actual Android catalog. Two
    # human-editable text manifests must not silently agree on a wrong hash.
    android_catalog = folder.parent.parent / (
        "app/src/main/java/vn/navitaximap/data/Vietnam35Catalog.kt"
    )
    content = android_catalog.read_text(encoding="utf-8")
    specs = re.findall(
        r'VietnamRegionSpec\("([0-9]{2}|VN)", "([^"]+)", "([^"]+)", '
        r'"([0-9a-f]{64})", ([0-9]+), 14, ([0-9]+)L\)',
        content,
    )
    require(len(specs) == 35, "Android package catalog must contain 35 pinned specs")
    by_android = {
        file: (code, sha, int(size)) for code, _, file, sha, _, size in specs
    }
    require(len(by_android) == 35, "Duplicate Android package filename")
    require(by_android == {
        row["file"]: (row["code"], row["sha256"], int(row["size_bytes"]))
        for row in rows
    }, "Android catalog differs from original private archive pins")
    require("const val distributionApproved = false" in content,
            "Distribution approval must remain disabled for unreviewed archives")
    require("const val navigationApproved = false" in content,
            "Navigation approval must remain disabled for unreviewed archives")
    return rows

def checked_stream(stream, *, size, sha256):
    """Read the WHOLE file, enforce byte budget, PMTiles v3 magic and SHA-256."""
    digest = hashlib.sha256()
    first = stream.read(8)
    require(first == HEADER, "Not a PMTiles v3 archive")
    digest.update(first)
    count = len(first)
    while True:
        chunk = stream.read(CHUNK)
        if not chunk:
            break
        count += len(chunk)
        require(count <= size, "Archive exceeded exact pinned bytes")
        digest.update(chunk)
    require(count == size, f"Expected {size} bytes, got {count}")
    require(digest.hexdigest() == sha256, "Full SHA-256 mismatch")

def _check_unique_names(actual, expected):
    require(set(actual) == set(expected),
            f"Local archive set incorrect, missing={sorted(set(expected) - set(actual))}, "
            f"unexpected={sorted(set(actual) - set(expected))}")

def verify_source(source, rows):
    """Inspect nested local archive ZIP OR directory; never extract or copy bytes."""
    source = Path(source)
    expected = {r["file"]: r for r in rows}
    require(not source.is_symlink(), "Symlinked artifact root forbidden")
    if source.is_dir():
        found = {}
        for p in source.rglob("*"):
            # Walk no malicious symlink. Relative folders are never allowed to
            # redirect outside the user-supplied offline artifact root.
            require(not p.is_symlink(), "Symlink inside artifact root forbidden")
            if p.is_file() and p.name.endswith(".pmtiles"):
                require(NAME.fullmatch(p.name), "Unexpected .pmtiles filename")
                require(p.name not in found, "Duplicate PMTiles basename")
                found[p.name] = p
        _check_unique_names(found, expected)
        for name, p in sorted(found.items()):
            row = expected[name]
            size = int(row["size_bytes"])
            require(p.stat().st_size == size, "Archive differs from pinned size")
            with p.open("rb") as stream:
                checked_stream(stream, size=size, sha256=row["sha256"])
    else:
        require(source.is_file() and zipfile.is_zipfile(source),
                "Source must be a local ZIP artifact or extracted directory")
        with zipfile.ZipFile(source) as z:
            found = {}
            for info in z.infolist():
                path = PurePosixPath(info.filename)
                require(not path.is_absolute() and ".." not in path.parts,
                        "Unsafe relative path inside artifact ZIP")
                require("\\" not in info.filename, "Backslash ZIP path forbidden")
                if info.is_dir():
                    continue
                mode = info.external_attr >> 16
                require(not stat.S_ISLNK(mode), "ZIP symlink forbidden")
                if path.name.endswith(".pmtiles"):
                    require(NAME.fullmatch(path.name), "Unexpected .pmtiles filename")
                    require(path.name not in found, "Duplicate PMTiles basename")
                    found[path.name] = info
            _check_unique_names(found, expected)
            for name, info in sorted(found.items()):
                row = expected[name]
                size = int(row["size_bytes"])
                require(info.file_size == size, "ZIP declared size differs from pinned bytes")
                with z.open(info) as stream:
                    checked_stream(stream, size=size, sha256=row["sha256"])
    return len(expected), sum(int(r["size_bytes"]) for r in rows)

def receipt(rows):
    """Metadata ONLY. Do not include local paths, URLs, original pack bytes."""
    return {
        "schema": "navitaximap-local-preservation-receipt/v1",
        "status": "LOCAL_BYTES_VERIFIED",
        "localBackupCreatedByVerifier": False,
        "distributionApproved": False,
        "navigationApproved": False,
        "legalBoundaryApproved": False,
        "originalArtifactWorkflowRun": 43378133931,
        "originalArtifactsExpireDate": "2026-11-27",
        "totalFiles": len(rows),
        "totalBytes": sum(int(r["size_bytes"]) for r in rows),
        "files": [
            {"code": r["code"], "file": r["file"],
             "sizeBytes": int(r["size_bytes"]), "sha256": r["sha256"],
             "tileCount": int(r["tile_count"])}
            for r in rows
        ],
    }

def write_receipt(path, payload):
    """Explicit output only, atomic metadata-only file creation (private mode)."""
    path = Path(path)
    require(path.parent.is_dir() and not path.is_symlink(), "Bad receipt destination")
    temp_path = None
    try:
        with tempfile.NamedTemporaryFile(mode="w", encoding="utf-8",
                                         prefix=".navitaxi-receipt-",
                                         dir=path.parent, delete=False) as stream:
            temp_path = Path(stream.name)
            os.chmod(temp_path, 0o600)
            json.dump(payload, stream, ensure_ascii=False, sort_keys=True, indent=2)
            stream.write("\n")
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(temp_path, path)
    finally:
        if temp_path is not None:
            temp_path.unlink(missing_ok=True)

def main():
    cli = argparse.ArgumentParser(description=__doc__)
    cli.add_argument("--provinces", type=Path, required=True,
                     help="Original 34-province private ZIP artifact or extracted folder")
    cli.add_argument("--national", type=Path, required=True,
                     help="Original national private ZIP artifact or extracted folder")
    cli.add_argument("--receipt", type=Path,
                     help="Optional LOCAL JSON metadata receipt (no map data)")
    args = cli.parse_args()
    rows = pinned_ledger()
    province = [r for r in rows if r["code"] != "VN"]
    national = [r for r in rows if r["code"] == "VN"]
    p_count, p_bytes = verify_source(args.provinces, province)
    n_count, n_bytes = verify_source(args.national, national)
    require((p_count, n_count) == (34, 1), "Expected 34 provincial and 1 national original")
    require(p_bytes + n_bytes == 843_734_803, "Combined original bytes mismatch")
    result = receipt(rows)
    if args.receipt is not None:
        write_receipt(args.receipt, result)
    print(f"PRIVATE_PRESERVATION_VERIFY_PASS files=35 bytes={result['totalBytes']}")
    print("NOTE: Only local inputs were verified; no backup or redistribution was performed.")

if __name__ == "__main__":
    main()
