#!/usr/bin/env python3
"""Verify that the bundled real OSM road-preview PMTiles are reproducible offline.

Never fetch external OSM data in a normal build. Input is an immutable snapshot
in git with the captured upstream SHA and ODbL attribution.
"""
import gzip
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools" / "osm_hanoi_pilot"))
from build_pmtiles import generate

FROZEN = ROOT / "tools/osm_hanoi_pilot/snapshot/roads.json.gz"
PACKAGE = ROOT / "app/src/main/assets/hanoi_real_roads.pmtiles"
MANIFEST = ROOT / "tools/osm_hanoi_pilot/snapshot/build-manifest.json"
EXPECTED_SHA = "ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d"

def main():
    roads = json.loads(gzip.decompress(FROZEN.read_bytes()))
    expected = json.loads(MANIFEST.read_text())
    actual, data_manifest = generate(roads)
    binary = PACKAGE.read_bytes()
    assert binary == actual, "Bundled PMTiles differs from deterministic OSM conversion"
    assert hashlib.sha256(binary).hexdigest() == EXPECTED_SHA
    assert expected == data_manifest, "PMTiles metadata differs from pinned snapshot"
    assert expected["source_road_count"] == 354
    assert expected["source_node_count"] == 17828
    assert expected["tile_count"] == 21
    assert expected["osm_source_xml_sha256"] == "db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697"
    assert expected["source_review"] == "UNREVIEWED for navigation and administrative/legal data"
    assert expected["license"] == "Open Database License 1.0 (ODbL)"
    print("Real OSM preview fixture PASS: 354 source ways, 21 tiles, 53956 bytes, SHA256 " + EXPECTED_SHA)

if __name__ == "__main__":
    main()
