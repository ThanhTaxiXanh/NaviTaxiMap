#!/usr/bin/env python3
"""Fetch one SMALL real OSM XML snapshot for an experimental Hanoi road preview.

Runs once in a branch-only GitHub Actions workflow, NOT in the normal build.
Never bulk-download online tile servers. Does not assert legal/admin boundaries.
"""
import hashlib
import json
from pathlib import Path
from datetime import datetime, timezone
from urllib.request import Request, urlopen
import xml.etree.ElementTree as ET

BBOX = (105.8490, 21.0310, 105.8580, 21.0370)  # test crop, not province border
URL = "https://api.openstreetmap.org/api/0.6/map?bbox=" + ",".join(map(str, BBOX))
USER_AGENT = "NaviTaxiMap-road-pilot/0.1 (+https://github.com/ThanhTaxiXanh/NaviTaxiMap)"
OUTPUT = Path("osm-pilot-snapshot")
MAX_BYTES = 8 * 1024 * 1024


def fetch():
    OUTPUT.mkdir(exist_ok=True)
    request = Request(URL, headers={"User-Agent": USER_AGENT, "Accept": "application/xml"})
    with urlopen(request, timeout=60) as resp:
        assert resp.status == 200, f"OSM source returned HTTP {resp.status}"
        raw = resp.read(MAX_BYTES + 1)
    assert 0 < len(raw) <= MAX_BYTES, "OSM snapshot too large or empty"
    assert b"<!DOCTYPE" not in raw.upper(), "OSM XML contains DTD"
    root = ET.fromstring(raw)
    assert root.tag == "osm" and root.get("version") == "0.6", "Unexpected OSM source"
    nodes = {int(n.get("id")): (float(n.get("lat")), float(n.get("lon")))
             for n in root.findall("node")}
    roads = []
    for way in root.findall("way"):
        highway = next((t.get("v") for t in way.findall("tag")
                        if t.get("k") == "highway"), None)
        if not highway:
            continue
        points = [nodes[int(n.get("ref"))] for n in way.findall("nd")
                  if int(n.get("ref")) in nodes]
        if len(points) < 2:
            continue
        roads.append({
            "osm_way_id": int(way.get("id")),
            "highway": highway,
            "points": points,
        })
    roads.sort(key=lambda road: road["osm_way_id"])
    assert len(nodes) >= 40, f"Too few OSM nodes ({len(nodes)})"
    assert len(roads) >= 6, f"Too few actual OSM highways ({len(roads)})"
    assert sum(len(w["points"]) for w in roads) >= 20, "Road geometry too sparse"
    snapshot_hash = hashlib.sha256(raw).hexdigest()
    normalized = {
        "schema": "navitaximap-osm-road-pilot-v1",
        "source": URL,
        "bbox": list(BBOX),
        "attribution": "© OpenStreetMap contributors",
        "license": "Open Database License 1.0 (ODbL)",
        "source_xml_sha256": snapshot_hash,
        "downloaded_at_utc": datetime.now(timezone.utc).isoformat(),
        "node_count": len(nodes),
        "road_count": len(roads),
        "roads": roads,
    }
    (OUTPUT / "osm-raw.xml").write_bytes(raw)
    (OUTPUT / "roads.json").write_text(
        json.dumps(normalized, ensure_ascii=False, separators=(",", ":"), sort_keys=True) + "\n",
        encoding="utf-8",
    )
    summary = {key: value for key, value in normalized.items() if key != "roads"}
    (OUTPUT / "source-manifest.json").write_text(
        json.dumps(summary, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    print(f"OSM pilot snapshot: {len(raw)} bytes, {len(nodes)} nodes, {len(roads)} road ways")
    print(f"Source URL: {URL}")
    print(f"Source SHA-256: {snapshot_hash}")


if __name__ == "__main__":
    fetch()
