"""Regression tests for Hilbert index, clipping and real-source MVT package construction."""
import importlib.util
import json
from pathlib import Path
import struct
import sys
import unittest

HERE = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(HERE))
from build_pmtiles import generate, tile_id, clip_segment, coord


class TestRoadPilot(unittest.TestCase):
    def test_hilbert_compatibility(self):
        self.assertEqual([tile_id(1, x, y) for x, y in
                          [(0, 0), (0, 1), (1, 1), (1, 0)]], [1, 2, 3, 4])
        self.assertEqual(tile_id(0, 0, 0), 0)

    def test_clip_segment(self):
        self.assertIsNone(clip_segment(-2, -2, -1, -1))
        self.assertEqual(clip_segment(-1, 0.5, 2, 0.5),
                         (0.0, 0.5, 1.0, 0.5))

    def test_real_format_with_synthetic_unit_ways(self):
        test_data = {
            "schema": "navitaximap-osm-road-pilot-v1",
            "source": "https://example.org/test.osm",
            "source_xml_sha256": "a" * 64,
            "downloaded_at_utc": "2026-11-13T00:00:00+00:00",
            "license": "Open Database License 1.0 (ODbL)",
            "attribution": "© OpenStreetMap contributors",
            "bbox": [105.849, 21.031, 105.858, 21.037],
            "node_count": 42, "road_count": 10,
            "roads": [
                {"osm_way_id": 100 + i,
                 "highway": ["primary", "residential", "footway"][i % 3],
                 "points": [[21.032 + i * 0.0003, 105.8491],
                            [21.036, 105.8578]]}
                for i in range(10)
            ],
        }
        data, meta = generate(test_data)
        self.assertEqual(data[:8], b"PMTiles\x03")
        self.assertGreater(meta["tile_count"], 3)
        self.assertEqual(meta["source_road_count"], 10)
        self.assertEqual(meta["layer_names"],
                         ["road_local", "road_major", "road_pedestrian"])
        offsets = struct.unpack_from("<11Q", data, 8)
        self.assertEqual(data[offsets[0]:offsets[0] + 1], bytes([meta["tile_count"]]))
        self.assertEqual(offsets[6] + offsets[7], len(data))
        self.assertEqual(data[98], 2)
        self.assertEqual(data[99], 1)


if __name__ == "__main__":
    unittest.main()
