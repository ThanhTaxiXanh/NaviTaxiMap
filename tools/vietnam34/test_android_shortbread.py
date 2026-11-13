"""Check that Android offline style conforms to v1.0 and catalog pins the 35 SHA lines."""
import json
import csv
from pathlib import Path
import re
import runpy
import tempfile
from unittest import mock
import sys
import unittest
ROOT = Path(__file__).resolve().parents[2]
STYLE = ROOT / "app/src/main/assets/shortbread_offline_style.json"
CAT = ROOT / "app/src/main/java/vn/navitaximap/data/Vietnam35Catalog.kt"

class OfflineShortbreadTest(unittest.TestCase):
    def test_offline_style_valid_layers(self):
        data = json.loads(STYLE.read_text())
        self.assertEqual(8, data["version"])
        self.assertEqual("__PMTILES_URL__", data["sources"]["shortbread"]["url"])
        self.assertNotIn("glyphs", data)
        self.assertNotIn("sprite", data)
        layers = {x["source-layer"] for x in data["layers"] if "source-layer" in x}
        self.assertTrue({"ocean","land","water_lines","water_polygons","streets","buildings"} <= layers)
        self.assertNotIn("boundaries", layers)
        self.assertNotIn("http", STYLE.read_text())
    def test_all_hashes_match_and_no_download_approval(self):
        content = CAT.read_text()
        entries = re.findall(r'VietnamRegionSpec\("([0-9]{2}|VN)", "([^"]+)", "([^"]+)", "([a-f0-9]{64})", ([0-9]+), 14, ([0-9]+)L\)', content)
        self.assertEqual(35, len(entries))
        sums = {file: h for h,file in (row.split() for row in (ROOT / "tools/vietnam34/SHA256SUMS.txt").read_text().splitlines() if row.strip())}
        self.assertEqual(sums, {file:sha for _,_,file,sha,_,_ in entries})
        with (ROOT / "tools/vietnam34/PACK_BYTES_VERIFIED.tsv").open() as stream:
            ledger = list(csv.DictReader(stream, delimiter="\t"))
        self.assertEqual(35, len(ledger))
        self.assertEqual({row["file"]: (row["sha256"], int(row["size_bytes"]))
                          for row in ledger},
                         {file: (sha, int(size)) for _,_,file,sha,_,size in entries})
        self.assertEqual(sum(int(row["size_bytes"]) for row in ledger if row["code"] != "VN"), 287_504_805)
        self.assertEqual(sum(int(row["size_bytes"]) for row in ledger if row["code"] == "VN"), 556_229_998)
        self.assertIn("const val navigationApproved = false", content)
        self.assertIn("const val distributionApproved = false", content)

class SafPickerInstrumentationIsolationTest(unittest.TestCase):
    """Regression: Android DocumentsUI must never see the QA XML dump as a file."""
    def test_snapshot_uses_shell_tmp_not_android_document_storage(self):
        with tempfile.TemporaryDirectory() as tempdir:
            picker_file = str(ROOT / "ci/saf-picker-smoke.py")
            with mock.patch.object(sys, "argv", [picker_file, tempdir]):
                module = runpy.run_path(picker_file)
            calls = []
            xml = b'<?xml version="1.0" encoding="UTF-8"?><hierarchy/>'

            def fake_adb(*args, binary=False, check=True):
                calls.append(args)
                if args[:2] == ("exec-out", "cat"):
                    return xml
                return ""

            module["snapshot"].__globals__["adb"] = fake_adb
            root = module["snapshot"]("offline-map-picker")
            self.assertEqual("hierarchy", root.tag)
            dumps = [args for args in calls if args[:3] == ("shell", "uiautomator", "dump")]
            self.assertEqual(1, len(dumps))
            self.assertEqual("/data/local/tmp/navitaxi-picker.xml", dumps[0][3])
            self.assertTrue((Path(tempdir) / "saf-offline-map-picker.xml").is_file())

    def test_app_rejects_non_pmtiles_uris_before_opening_content_stream(self):
        source = (ROOT / "app/src/main/java/vn/navitaximap/MainActivity.kt").read_text()
        section = source[source.index("private fun importUserFile("):
                         source.index("private fun showVietnam35Catalog(")]
        self.assertIn('require(name.endsWith(".pmtiles", ignoreCase = true))', section)
        self.assertLess(section.index('require(name.endsWith(".pmtiles"'),
                        section.index("contentResolver.openInputStream(uri)"))
        self.assertNotIn('"name=" + name', section)

