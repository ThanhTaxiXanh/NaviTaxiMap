"""Source approval and label layer builder, adapted from owner-supplied test engine."""
import hashlib
import json
import math
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
RULE = json.loads((ROOT / "rules/policy.json").read_text(encoding="utf8"))
LABELS = RULE["labels"]


def read(path):
    return json.loads(Path(path).read_bytes())


def require(condition, reason):
    if not condition:
        raise ValueError(reason)


def approved(manifest, source):
    meta = read(manifest)
    raw = Path(source).read_bytes()
    for key in ("publisher", "source_url", "edition", "license", "approved_by", "scope", "legal_basis"):
        require(str(meta.get(key) or "").strip(), key)
    require(meta.get("viewpoint") == "VN" and
            hashlib.sha256(raw).hexdigest() == meta.get("sha256"), "approval/hash")
    collection = json.loads(raw)
    require(collection.get("type") == "FeatureCollection", "collection type")
    result = []
    for key, label in LABELS.items():
        matches = [feature for feature in collection["features"] if feature.get("id") == key]
        require(len(matches) == 1, key)
        feature = matches[0]
        properties = feature["properties"]
        require(feature["type"] == "Feature" and properties["name_vi"] == label, label)
        require(key == "bien-dong" or properties.get("viewpoint") == "VN", key + " viewpoint")
        geom = feature["geometry"]
        require(geom["type"] == "Point" and len(geom["coordinates"]) == 2, key + " geometry")
        x, y = geom["coordinates"]
        require(all(type(v) in (int, float) and math.isfinite(v) for v in (x, y)) and
                abs(x) <= 180 and abs(y) <= 90, key + " geometry")
        result.append(feature)
    return {"type": "FeatureCollection", "features": result}


if __name__ == "__main__":
    try:
        action, *args = sys.argv[1:]
        require(action == "build" and len(args) == 3, "expected: build approval.json reviewed.geojson output.geojson")
        manifest, source, output = args
        Path(output).write_text(json.dumps(approved(manifest, source), ensure_ascii=False), encoding="utf8")
    except Exception as exc:
        print(json.dumps({"status": "FAIL", "reason": str(exc)}, ensure_ascii=False))
        sys.exit(1)
