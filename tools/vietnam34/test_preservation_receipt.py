"""Offline-only preservation verification: no network, uploads, extraction, or big RAM."""
import hashlib
import json
import subprocess
import sys
from pathlib import Path
import stat
import tempfile
import unittest
import zipfile

from verify_private_preservation import (
    HEADER, pinned_ledger, verify_source, receipt, write_receipt
)

class OriginalArchivePreservationTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.root = Path(self.tmp.name)
        self.packets = {
            "24-bac-ninh.pmtiles": HEADER + b"A" * 2048,
            "00-viet-nam-all.pmtiles": HEADER + b"N" * 4096,
        }
        self.rows = [
            {
                "code": code, "file": name, "size_bytes": str(len(data)),
                "tile_count": str(3 if code == "VN" else 2),
                "sha256": hashlib.sha256(data).hexdigest(),
            }
            for code, name, data in [
                ("VN", "00-viet-nam-all.pmtiles",
                 self.packets["00-viet-nam-all.pmtiles"]),
                ("24", "24-bac-ninh.pmtiles",
                 self.packets["24-bac-ninh.pmtiles"]),
            ]
        ]

    def tearDown(self):
        self.tmp.cleanup()

    def write_zip(self, name, contents):
        path = self.root / name
        with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as z:
            for filename, data in contents.items():
                z.writestr(filename, data)
        return path

    def test_actual_repo_35_metadata_pins_are_stable_and_consistent(self):
        ledger = pinned_ledger()
        self.assertEqual(35, len(ledger))
        self.assertEqual(843_734_803,
                         sum(int(r["size_bytes"]) for r in ledger))
        self.assertEqual(34, sum(r["code"] != "VN" for r in ledger))
        self.assertEqual(1, sum(r["code"] == "VN" for r in ledger))

    def test_metadata_pin_and_android_catalog_must_match(self):
        """The SHA manifest and ledger must agree with pinned Android code."""
        import shutil
        repo_root = Path(__file__).resolve().parents[2]
        mirrored = self.root / "tools" / "vietnam34"
        mirrored.mkdir(parents=True)
        for name in ("PACK_BYTES_VERIFIED.tsv", "SHA256SUMS.txt"):
            shutil.copyfile(
                repo_root / "tools" / "vietnam34" / name, mirrored / name
            )
        catalog = self.root / "app/src/main/java/vn/navitaximap/data"
        catalog.mkdir(parents=True)
        shutil.copyfile(
            repo_root / "app/src/main/java/vn/navitaximap/data/Vietnam35Catalog.kt",
            catalog / "Vietnam35Catalog.kt",
        )
        self.assertEqual(35, len(pinned_ledger(mirrored)))
        # Change one hash in BOTH text manifests: their mutual equality is
        # insufficient. The independent Android catalog must reject the drift.
        ledger = mirrored / "PACK_BYTES_VERIFIED.tsv"
        sums = mirrored / "SHA256SUMS.txt"
        original = "6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f"
        replacement = "0" * 64
        self.assertNotEqual(original, replacement)
        ledger.write_text(ledger.read_text().replace(original, replacement))
        sums.write_text(sums.read_text().replace(original, replacement))
        with self.assertRaisesRegex(ValueError, "Android catalog differs"):
            pinned_ledger(mirrored)

    def test_nested_original_zip_streams_and_metadata_only_receipt(self):
        archive = self.write_zip("province.zip", {
            "artifact/source/24-bac-ninh.pmtiles":
                self.packets["24-bac-ninh.pmtiles"],
            "artifact/readme.txt": b"UNREVIEWED",
        })
        self.assertEqual((1, len(self.packets["24-bac-ninh.pmtiles"])),
                         verify_source(archive, [self.rows[1]]))
        output = self.root / "receipt.json"
        payload = receipt(self.rows)
        write_receipt(output, payload)
        manifest = json.loads(output.read_text(encoding="utf-8"))
        self.assertEqual(2, manifest["totalFiles"])
        self.assertFalse(manifest["distributionApproved"])
        self.assertFalse(manifest["navigationApproved"])
        self.assertFalse(manifest["localBackupCreatedByVerifier"])
        self.assertNotIn(str(archive), output.read_text(encoding="utf-8"))
        self.assertNotIn("http", output.read_text(encoding="utf-8"))
        self.assertFalse(any(p.suffix == ".pmtiles" for p in self.root.iterdir()))

    def test_extracted_original_folder_is_verified_with_no_copy(self):
        src = self.root / "archive"
        (src / "unpacked").mkdir(parents=True)
        file = src / "unpacked" / "00-viet-nam-all.pmtiles"
        file.write_bytes(self.packets[file.name])
        self.assertEqual((1, len(self.packets[file.name])),
                         verify_source(src, [self.rows[0]]))
        self.assertEqual(self.packets[file.name], file.read_bytes())

    def test_wrong_sha_wrong_length_and_wrong_magic_fail(self):
        for name, replacement in [
            ("checksum", HEADER + b"B" * 2048),
            ("size", self.packets["24-bac-ninh.pmtiles"][:-1]),
            ("magic", b"NOTA-PM!" + b"A" * 2048),
        ]:
            archive = self.write_zip(name + ".zip", {
                "24-bac-ninh.pmtiles": replacement
            })
            with self.subTest(case=name):
                with self.assertRaises(ValueError):
                    verify_source(archive, [self.rows[1]])

    def test_duplicate_extra_and_missing_files_fail_without_receipt(self):
        target = self.packets["24-bac-ninh.pmtiles"]
        files = [
            {"nested/a/24-bac-ninh.pmtiles": target,
             "nested/b/24-bac-ninh.pmtiles": target},
            {"24-bac-ninh.pmtiles": target,
             "01-ha-noi.pmtiles": HEADER + b"x" * 100},
            {"readme.txt": b"nothing"},
        ]
        for i, contents in enumerate(files):
            with self.subTest(case=i):
                archive = self.write_zip(f"invalid{i}.zip", contents)
                with self.assertRaises(ValueError):
                    verify_source(archive, [self.rows[1]])

    def test_zip_path_traversal_is_refused_even_if_pmtiles_hash_matches(self):
        artifact = self.write_zip("traversal.zip", {
            "../../24-bac-ninh.pmtiles": self.packets["24-bac-ninh.pmtiles"]
        })
        with self.assertRaises(ValueError):
            verify_source(artifact, [self.rows[1]])

    def test_symlink_inside_extracted_folder_is_refused(self):
        src = self.root / "extracted"
        src.mkdir()
        existing = self.root / "24-bac-ninh.pmtiles"
        existing.write_bytes(self.packets[existing.name])
        (src / existing.name).symlink_to(existing)
        with self.assertRaises(ValueError):
            verify_source(src, [self.rows[1]])

    def test_python_optimized_mode_cannot_bypass_integrity_checks(self):
        """python -O strips assert statements; checks must stay mandatory."""
        source_dir = Path(__file__).resolve().parent
        cases = [
            ("bad magic", "b'NOT-PMT!' + b'Q' * 128",
             "136", "'0' * 64"),
            ("bad SHA", f"{HEADER!r} + b'A' * 128",
             "136", "'f' * 64"),
        ]
        for label, payload, byte_count, digest in cases:
            with self.subTest(label=label):
                code = (
                    "import io,sys\n"
                    "from verify_private_preservation import checked_stream\n"
                    f"payload = {payload}\n"
                    "try:\n"
                    f"    checked_stream(io.BytesIO(payload), size={byte_count}, sha256={digest})\n"
                    "except ValueError:\n"
                    "    sys.exit(0)\n"
                    "sys.exit(91)\n"
                )
                result = subprocess.run(
                    [sys.executable, "-O", "-c", code],
                    cwd=source_dir, capture_output=True, text=True, check=False
                )
                self.assertEqual(0, result.returncode, result.stderr + result.stdout)
