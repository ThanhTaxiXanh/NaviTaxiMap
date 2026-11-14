"""Tests for owner-invoked local private backup; no network or original PMTiles copied."""
import hashlib
import json
from pathlib import Path
import stat
import tempfile
import unittest
from unittest import mock
import zipfile

from copy_private_originals import (
    PROVINCE_NAME, NATIONAL_NAME, RECEIPT_NAME,
    safe_private_directory, verified_private_copy, save_receipt
)
from verify_private_preservation import HEADER


class PrivateOriginalCopyTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        self.dest = self.root / "private"
        self.dest.mkdir(mode=0o700)
        self.bytes = HEADER + (b"Test region geometry. " * 40)
        self.row = [{
            "code": "24", "file": "24-bac-ninh.pmtiles",
            "size_bytes": str(len(self.bytes)),
            "sha256": hashlib.sha256(self.bytes).hexdigest(),
            "tile_count": "7",
        }]
        self.source = self.root / "source.zip"
        with zipfile.ZipFile(self.source, "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("artifact/24-bac-ninh.pmtiles", self.bytes)

    def tearDown(self):
        self.tmp.cleanup()

    def test_copy_is_byte_identical_private_and_idempotent(self):
        first = verified_private_copy(self.source, self.dest, self.row, PROVINCE_NAME)
        self.assertFalse(first["existingVerified"])
        target = self.dest / PROVINCE_NAME
        self.assertEqual(self.source.read_bytes(), target.read_bytes())
        self.assertEqual(0, stat.S_IMODE(target.stat().st_mode) & 0o077)
        again = verified_private_copy(self.source, self.dest, self.row, PROVINCE_NAME)
        self.assertTrue(again["existingVerified"])
        self.assertEqual(first["zipSha256"], again["zipSha256"])
        self.assertFalse(list(self.dest.glob("*.partial")))

    def test_existing_wrong_bytes_are_never_overwritten(self):
        target = self.dest / PROVINCE_NAME
        target.write_bytes(b"Important existing private contents")
        target.chmod(0o600)
        with self.assertRaises(ValueError):
            verified_private_copy(self.source, self.dest, self.row, PROVINCE_NAME)
        self.assertEqual(b"Important existing private contents", target.read_bytes())

    def test_source_corruption_refused_before_private_write(self):
        with zipfile.ZipFile(self.source, "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("24-bac-ninh.pmtiles", HEADER + b"tampered")
        with self.assertRaises(ValueError):
            verified_private_copy(self.source, self.dest, self.row, PROVINCE_NAME)
        self.assertEqual([], list(self.dest.iterdir()))

    def test_symlinked_and_world_readable_private_directory_rejected(self):
        shortcut = self.root / "private-link"
        shortcut.symlink_to(self.dest, target_is_directory=True)
        with self.assertRaises(ValueError):
            safe_private_directory(shortcut)
        self.dest.chmod(0o755)
        with self.assertRaises(ValueError):
            verified_private_copy(self.source, self.dest, self.row, PROVINCE_NAME)

    def test_wrong_destination_filename_and_symlinked_target_rejected(self):
        with self.assertRaises(ValueError):
            verified_private_copy(self.source, self.dest, self.row, "../oops.zip")
        target = self.dest / PROVINCE_NAME
        target.symlink_to(self.source)
        with self.assertRaises(ValueError):
            verified_private_copy(self.source, self.dest, self.row, PROVINCE_NAME)
        self.assertEqual(0, len(list(self.dest.glob("*.partial"))))

    def test_metadata_receipt_cannot_overwrite_different_existing_receipt(self):
        payload = {"schema": "private-test", "zipCopies": [PROVINCE_NAME]}
        save_receipt(self.dest, payload)
        receipt = self.dest / RECEIPT_NAME
        self.assertEqual(payload, json.loads(receipt.read_text()))
        self.assertEqual(0, stat.S_IMODE(receipt.stat().st_mode) & 0o077)
        save_receipt(self.dest, payload)
        with self.assertRaises(ValueError):
            save_receipt(self.dest, {"schema": "different"})
        self.assertEqual(payload, json.loads(receipt.read_text()))

    def test_insufficient_disk_never_starts_copy(self):
        from copy_private_originals import shutil
        with mock.patch.object(shutil, "disk_usage") as disk:
            disk.return_value = type("DiskUsage", (), {"free": 0})()
            with self.assertRaises(ValueError):
                verified_private_copy(self.source, self.dest, self.row, NATIONAL_NAME)
        self.assertFalse((self.dest / NATIONAL_NAME).exists())
