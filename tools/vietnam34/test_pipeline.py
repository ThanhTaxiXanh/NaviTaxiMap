#!/usr/bin/env python3
"""No empty provinces, no fabricated boundaries, correct WebMercator/TMS logic."""
import unittest
import importlib.util
from pathlib import Path
import sys
BASE=Path(__file__).resolve().parent
sys.path.insert(0,str(BASE))
from build_national_and_provinces import tile_bounds, load_provinces, PROVINCES

class TestNationwidePipeline(unittest.TestCase):
    def test_exact_official_regions(self):
        self.assertEqual(len(PROVINCES),34)
        self.assertEqual(len({p["code"] for p in PROVINCES}),34)
        self.assertEqual(len({p["slug"] for p in PROVINCES}),34)
        self.assertEqual([p["code"] for p in PROVINCES][:4],["01","04","08","11"])
        self.assertEqual(PROVINCES[-1]["name"],"Cà Mau")
    def test_tms_tile_coordinate_conversion(self):
        b=tile_bounds(1,1,0).bounds
        self.assertAlmostEqual(b[0],0)
        self.assertAlmostEqual(b[2],180)
        self.assertLess(b[1],0)
        self.assertAlmostEqual(b[3],0)
    def test_missing_boundary_must_fail(self):
        import tempfile,json
        with tempfile.TemporaryDirectory() as d:
            a=Path(d)/"a.json"; b=Path(d)/"b.json"
            a.write_text(json.dumps({"type":"FeatureCollection","features":[]}))
            b.write_text(json.dumps({"type":"FeatureCollection","features":[]}))
            with self.assertRaises(AssertionError):
                load_provinces(a,b)
if __name__=="__main__":unittest.main()
