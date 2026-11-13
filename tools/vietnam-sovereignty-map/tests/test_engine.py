import hashlib
import json
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "scripts"))
from engine import approved, LABELS


class TestApproval(unittest.TestCase):
    def setUp(self):
        temp = tempfile.TemporaryDirectory()
        self.addCleanup(temp.cleanup)
        self.data = Path(temp.name) / "data"
        self.meta = Path(temp.name) / "meta"
        self.features = [
            dict(type="Feature", id=k, properties=dict(name_vi=v, viewpoint="VN"),
                 geometry=dict(type="Point", coordinates=[0, 0]))
            for k, v in LABELS.items()
        ]

    def prepare(self):
        self.data.write_text(json.dumps(dict(type="FeatureCollection", features=self.features)))
        m = dict.fromkeys("publisher source_url edition license approved_by scope legal_basis".split(), "test fixture")
        self.meta.write_text(json.dumps(dict(m, viewpoint="VN",
                          sha256=hashlib.sha256(self.data.read_bytes()).hexdigest())))

    def test_valid(self):
        self.prepare()
        self.assertEqual(approved(self.meta, self.data)["features"], self.features)

    def test_duplicate(self):
        self.features.append(self.features[0])
        self.prepare()
        with self.assertRaises(ValueError):
            approved(self.meta, self.data)

    def test_changed(self):
        self.prepare()
        self.data.write_text("{}")
        with self.assertRaises(ValueError):
            approved(self.meta, self.data)

    def test_attribution(self):
        self.features[0]["properties"]["viewpoint"] = "other"
        self.prepare()
        with self.assertRaises(ValueError):
            approved(self.meta, self.data)
