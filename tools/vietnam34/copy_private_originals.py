#!/usr/bin/env python3
"""Make a verified PRIVATE local copy of the TWO manually downloaded original ZIPs.

Explicit destination only; no network, cloud, automatic host selection, or
distribution. This helper is NOT invoked by GitHub Actions to copy map data.

Use an existing owner-approved PRIVATE directory (mode 0700 on POSIX):
  python3 tools/vietnam34/copy_private_originals.py \
    --provinces /private/downloads/34-original.zip \
    --national /private/downloads/vietnam-original.zip \
    --destination /private/approved-offline-backup
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import stat
import tempfile
import zipfile

from verify_private_preservation import pinned_ledger, require, verify_source

CHUNK = 1024 * 1024
EXTRA_SPACE = 32 * 1024 * 1024
MAX_ZIP_SIZE = 1_500_000_000
PROVINCE_NAME = "NaviTaxiMap-34-Provinces-PRIVATE-ORIGINAL.zip"
NATIONAL_NAME = "NaviTaxiMap-Vietnam-PRIVATE-ORIGINAL.zip"
RECEIPT_NAME = "NaviTaxiMap-private-backup-receipt.json"


def safe_private_directory(directory):
    """Reject symlinked paths and directories readable by unrelated users."""
    directory = Path(directory).absolute()
    require(os.name == "posix", "Verified copy currently supports POSIX file permissions only")
    require(directory.is_dir(), "Approved private backup directory must already exist")
    require(all(not ancestor.is_symlink() for ancestor in (directory, *directory.parents)),
            "Private backup directory path cannot contain symlinks")
    mode = stat.S_IMODE(directory.stat().st_mode)
    require(mode & 0o077 == 0,
            "Private backup directory must be inaccessible to group/others (chmod 700)")
    return directory


def sync_directory(directory):
    flags = os.O_RDONLY | getattr(os, "O_DIRECTORY", 0)
    fd = os.open(directory, flags)
    try:
        os.fsync(fd)
    finally:
        os.close(fd)


def sha256_stream(path):
    digest = hashlib.sha256()
    total = 0
    with Path(path).open("rb") as stream:
        while True:
            chunk = stream.read(CHUNK)
            if not chunk:
                break
            total += len(chunk)
            require(total <= MAX_ZIP_SIZE, "Original ZIP exceeds safe byte limit")
            digest.update(chunk)
    return total, digest.hexdigest()


def verify_local_zip(source, rows):
    source = Path(source)
    require(not source.is_symlink() and source.is_file() and zipfile.is_zipfile(source),
            "Original input must be an actual local ZIP file, not a directory/symlink")
    require(0 < source.stat().st_size <= MAX_ZIP_SIZE, "Original ZIP outside safe size range")
    verify_source(source, rows)
    return sha256_stream(source)


def verified_private_copy(source, directory, rows, fixed_name):
    """Safely copy, re-verify ZIP internals and never overwrite an existing file."""
    require(fixed_name in (PROVINCE_NAME, NATIONAL_NAME),
            "Unexpected destination filename")
    directory = safe_private_directory(directory)
    source = Path(source)
    zip_size, zip_sha = verify_local_zip(source, rows)
    target = directory / fixed_name
    require(not target.is_symlink(), "Symlinked destination filename forbidden")
    require(source.resolve() != target.resolve(), "Source and private copy must differ")

    if target.exists():
        require(target.is_file(), "Existing private destination is not a regular file")
        require(stat.S_IMODE(target.stat().st_mode) & 0o077 == 0,
                "Existing private backup has unsafe read permissions")
        require(sha256_stream(target) == (zip_size, zip_sha),
                "Existing copy differs; never overwrite private originals")
        verify_source(target, rows)
        return {"filename": fixed_name, "zipBytes": zip_size,
                "zipSha256": zip_sha, "existingVerified": True}

    require(shutil.disk_usage(directory).free >= zip_size + EXTRA_SPACE,
            "Insufficient private destination space; no original bytes copied")

    temp_path = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="wb", dir=directory, prefix=".navitaxi-original-", suffix=".partial",
            delete=False
        ) as temp:
            temp_path = Path(temp.name)
            os.chmod(temp_path, 0o600)
            digest = hashlib.sha256()
            total = 0
            with source.open("rb") as data:
                while True:
                    buf = data.read(CHUNK)
                    if not buf:
                        break
                    total += len(buf)
                    require(total <= zip_size, "Original source ZIP changed while copying")
                    digest.update(buf)
                    temp.write(buf)
            require((total, digest.hexdigest()) == (zip_size, zip_sha),
                    "Original source ZIP changed or was corrupted during copying")
            temp.flush()
            os.fsync(temp.fileno())

        # Re-verify the destination bytes BEFORE publication under its final
        # predictable name. No extracted raw PMTiles content is ever written.
        require(sha256_stream(temp_path) == (zip_size, zip_sha),
                "Copied original ZIP SHA-256 differs from source")
        verify_source(temp_path, rows)

        # Atomic no-overwrite publication on the SAME private filesystem.
        # os.replace() would overwrite a previous backup, so never use it here.
        os.link(temp_path, target, follow_symlinks=False)
        sync_directory(directory)
        return {"filename": fixed_name, "zipBytes": zip_size,
                "zipSha256": zip_sha, "existingVerified": False}
    finally:
        if temp_path is not None:
            temp_path.unlink(missing_ok=True)


def save_receipt(directory, data):
    """Private metadata only; an existing receipt is immutable unless identical."""
    directory = safe_private_directory(directory)
    target = directory / RECEIPT_NAME
    encoded = (json.dumps(data, sort_keys=True, indent=2) + "\n").encode("utf-8")
    require(not target.is_symlink(), "Symlinked receipt forbidden")
    if target.exists():
        require(target.is_file() and
                stat.S_IMODE(target.stat().st_mode) & 0o077 == 0 and
                target.read_bytes() == encoded, "Existing backup receipt differs")
        return
    temp_path = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="wb", dir=directory, prefix=".navitaxi-receipt-", delete=False
        ) as stream:
            temp_path = Path(stream.name)
            os.chmod(temp_path, 0o600)
            stream.write(encoded)
            stream.flush()
            os.fsync(stream.fileno())
        os.link(temp_path, target, follow_symlinks=False)
        sync_directory(directory)
    finally:
        if temp_path is not None:
            temp_path.unlink(missing_ok=True)


def make_private_backup(provinces, national, destination):
    """Explicit offline opt-in. Can be safely rerun after one valid ZIP was saved."""
    dest = safe_private_directory(destination)
    rows = pinned_ledger()
    province_rows = [r for r in rows if r["code"] != "VN"]
    national_rows = [r for r in rows if r["code"] == "VN"]

    # Validate BOTH source archives before writing even the first backup.
    verify_local_zip(provinces, province_rows)
    verify_local_zip(national, national_rows)
    items = [
        verified_private_copy(provinces, dest, province_rows, PROVINCE_NAME),
        verified_private_copy(national, dest, national_rows, NATIONAL_NAME),
    ]
    proof = {
        "schema": "navitaximap-private-original-copy/v1",
        "localPrivateCopyVerified": True,
        "distributionApproved": False,
        "navigationApproved": False,
        "sourceArtifactRun": 43378133931,
        "originalPMTilesCount": 35,
        "originalPMTilesBytes": 843_734_803,
        "zipCopies": [{k: v for k, v in item.items() if k != "existingVerified"}
                      for item in items],
    }
    save_receipt(dest, proof)
    print("PRIVATE_OFFLINE_BACKUP_COPY_PASS files=35 pmtiles_bytes=843734803")
    print("Stored only in the explicitly selected private directory; no upload or publishing.")
    return proof


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--provinces", type=Path, required=True,
                        help="Manually downloaded original 34-provincial ZIP")
    parser.add_argument("--national", type=Path, required=True,
                        help="Manually downloaded original nationwide ZIP")
    parser.add_argument("--destination", type=Path, required=True,
                        help="Already existing OWNER-APPROVED private directory (chmod 700)")
    args = parser.parse_args()
    make_private_backup(args.provinces, args.national, args.destination)


if __name__ == "__main__":
    main()
