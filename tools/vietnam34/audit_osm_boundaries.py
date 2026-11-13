#!/usr/bin/env python3
"""Audit province boundary relation candidates from a PINNED Geofabrik OSM snapshot.

A relation name, center or approximate bbox is NOT legally authoritative.
Strictly do not create fake province PMTiles when valid shapes are missing.
"""
import collections
import json
import sys
import unicodedata
from pathlib import Path

BASE = Path(__file__).resolve().parent
INDEX = json.loads((BASE / "provinces_34.json").read_text(encoding="utf8"))


def normalize(name):
    name = unicodedata.normalize("NFKD", str(name).casefold())
    name = "".join(c for c in name if not unicodedata.combining(c))
    name = name.replace("đ", "d")
    for token in ("thanh pho ", "tinh ", "tp. ", "province", "city", "municipality"):
        name = name.replace(token, " ")
    return "".join(c for c in name if c.isalnum())


def audit(path, outdir):
    outdir.mkdir(parents=True, exist_ok=True)
    features = []
    with path.open(encoding="utf8") as stream:
        for line in stream:
            line = line.lstrip("\x1e").strip()
            if not line:
                continue
            feature = json.loads(line)
            props = feature.get("properties") or {}
            if props.get("admin_level") != "4" or props.get("boundary") != "administrative":
                continue
            if (feature.get("geometry") or {}).get("type") not in ("Polygon", "MultiPolygon"):
                continue
            features.append(feature)
    names = collections.Counter()
    candidate = []
    for feature in features:
        props = feature["properties"]
        values = {key: props.get(key, "") for key in ("name", "name:vi", "official_name", "name:en")}
        names.update(v for v in values.values() if v)
        candidate.append({"id": props.get("@id"), **values})
    missing = []
    matching = {}
    for province in INDEX["provinces"]:
        target = {normalize(v) for v in province["components"]}
        matched = [f for f in features if normalize((f.get("properties") or {}).get("name:vi") or
                      (f.get("properties") or {}).get("name")) in target]
        matching[province["code"]] = len(matched)
        if not matched:
            missing.append(province["name"])
    result = {
        "boundary_source":"Geofabrik OSM admin_level=4 relations, unreviewed",
        "feature_count":len(features),"unique_names":len(names),
        "candidates":candidate,"matches_by_code":matching,
        "no_matching_component":missing,
        "legality":"UNREVIEWED; NOT official administrative boundaries"
    }
    (outdir / "boundary-audit.json").write_text(
        json.dumps(result,ensure_ascii=False,indent=2,sort_keys=True),encoding="utf8")
    # Save all original OSM-derived polygons. Source data license ODbL.
    (outdir / "osm-admin4-boundaries.geojson").write_text(json.dumps(
        {"type":"FeatureCollection","features":features},ensure_ascii=False,
        separators=(",",":")),encoding="utf8")
    print("PROVINCE_AUDIT_SUMMARY",json.dumps({k:result[k] for k in
          ("feature_count","unique_names","matches_by_code","no_matching_component")},
          ensure_ascii=False))
    assert len(features) >= 30, "Very few admin4 province boundaries; no usable province split"


if __name__ == "__main__":
    audit(Path(sys.argv[1]),Path(sys.argv[2]))
