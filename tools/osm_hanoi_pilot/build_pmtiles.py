#!/usr/bin/env python3
"""Reproducible MVT/PMTiles v3 preview from captured REAL OpenStreetMap ways.

Only renders visible road lines for a tiny pilot bbox. This is NOT an offline
routing graph, province map, traffic layer or legal geography source.
Input must be a previously frozen OSM-derived roads.json with source SHA-256.
"""
import argparse
import gzip
import hashlib
import json
import math
import struct
from pathlib import Path

MAJOR = {"motorway", "trunk", "primary", "secondary", "tertiary",
         "motorway_link", "trunk_link", "primary_link", "secondary_link", "tertiary_link"}
PEDESTRIAN = {"footway", "pedestrian", "path", "steps", "cycleway", "bridleway"}
LAYERS = ("road_local", "road_major", "road_pedestrian")
EXTENT = 4096
MINZOOM, MAXZOOM = 14, 17


def varint(n):
    assert n >= 0
    data = bytearray()
    while n >= 128:
        data.append((n & 127) | 128)
        n >>= 7
    data.append(n)
    return bytes(data)


def field(tag, wire, value):
    return varint((tag << 3) | wire) + value


def blob(tag, value):
    return field(tag, 2, varint(len(value)) + value)


def uint(tag, value):
    return field(tag, 0, varint(value))


def zigzag(n):
    return n * 2 if n >= 0 else -n * 2 - 1


def xy_to_hilbert(z, x, y):
    size = 1 << z
    d = 0
    scale = size >> 1
    while scale:
        rx = int(bool(x & scale))
        ry = int(bool(y & scale))
        d += scale * scale * ((3 * rx) ^ ry)
        if ry == 0:
            if rx == 1:
                x = size - 1 - x
                y = size - 1 - y
            x, y = y, x
        scale >>= 1
    return d


def tile_id(z, x, y):
    return ((4 ** z - 1) // 3) + xy_to_hilbert(z, x, y)


def coord(lat, lon, z):
    assert -85.051129 < lat < 85.051129
    scale = 1 << z
    x = (lon + 180.0) / 360 * scale
    y = (1 - math.asinh(math.tan(math.radians(lat))) / math.pi) / 2 * scale
    return x, y


def clip_segment(x0, y0, x1, y1):
    """Liang-Barsky clip to [0,1] square, in local tile coordinates."""
    dx, dy = x1 - x0, y1 - y0
    lo, hi = 0.0, 1.0
    for p, q in ((-dx, x0), (dx, 1 - x0), (-dy, y0), (dy, 1 - y0)):
        if abs(p) < 1e-12:
            if q < 0:
                return None
        else:
            t = q / p
            if p < 0:
                lo = max(lo, t)
            else:
                hi = min(hi, t)
            if lo > hi:
                return None
    return x0 + lo * dx, y0 + lo * dy, x0 + hi * dx, y0 + hi * dy


def line_feature(road_id, seg):
    coords = [max(0, min(EXTENT, round(n * EXTENT))) for n in seg]
    x0, y0, x1, y1 = coords
    if (x0, y0) == (x1, y1):
        return b""
    geometry = b"".join(varint(n) for n in (
        9, zigzag(x0), zigzag(y0), 10, zigzag(x1 - x0), zigzag(y1 - y0)
    ))
    return blob(2, uint(1, road_id) + uint(3, 2) + blob(4, geometry))


def vector_layer(name, features):
    body = blob(1, name.encode()) + b"".join(features)
    body += uint(5, EXTENT) + uint(15, 2)
    return blob(3, body)


def make_tile(roads, z, col, row):
    parts = {key: [] for key in LAYERS}
    for road in roads:
        label = road["highway"]
        layer = ("road_major" if label in MAJOR
                 else "road_pedestrian" if label in PEDESTRIAN
                 else "road_local")
        points = road["points"]
        for a, b in zip(points, points[1:]):
            ax, ay = coord(a[0], a[1], z)
            bx, by = coord(b[0], b[1], z)
            clipped = clip_segment(ax - col, ay - row, bx - col, by - row)
            if clipped:
                feature = line_feature(road["osm_way_id"], clipped)
                if feature:
                    parts[layer].append(feature)
    if not any(parts.values()):
        return None
    return b"".join(vector_layer(key, parts[key]) for key in LAYERS if parts[key])


def tiles_for_bbox(bbox, z):
    left, bottom, right, top = bbox
    x1, y1 = coord(top, left, z)
    x2, y2 = coord(bottom, right, z)
    for x in range(math.floor(x1), math.floor(x2) + 1):
        for y in range(math.floor(y1), math.floor(y2) + 1):
            yield x, y


def generate(data):
    assert data["schema"] == "navitaximap-osm-road-pilot-v1"
    assert data["license"].startswith("Open Database License")
    assert data["attribution"] == "© OpenStreetMap contributors"
    assert len(data["source_xml_sha256"]) == 64
    bbox, roads = data["bbox"], data["roads"]
    assert len(bbox) == 4 and bbox[0] < bbox[2] and bbox[1] < bbox[3]
    assert 10 <= len(roads) < 10000
    assert all(road["osm_way_id"] > 0 and len(road["points"]) >= 2 for road in roads)

    tiles = []
    for z in range(MINZOOM, MAXZOOM + 1):
        for x, y in tiles_for_bbox(bbox, z):
            raw = make_tile(roads, z, x, y)
            if raw:
                tiles.append((tile_id(z, x, y), gzip.compress(raw, mtime=0), z, x, y))
    tiles.sort(key=lambda item: item[0])
    assert len(tiles) >= 4 and len(tiles) < 100
    assert len({t[0] for t in tiles}) == len(tiles)
    assert all(t[0] >= 0 and t[1] for t in tiles)

    ids = [t[0] for t in tiles]
    sizes = [len(t[1]) for t in tiles]
    # PMTiles directory: count, tileID deltas, run lengths, tile lengths, tile offsets.
    directory = varint(len(tiles))
    prev = 0
    for tile in ids:
        directory += varint(tile - prev)
        prev = tile
    directory += b"".join(varint(1) for _ in tiles)
    directory += b"".join(varint(n) for n in sizes)
    directory += varint(1) + b"".join(varint(0) for _ in tiles[1:])

    metadata = json.dumps({
        "name": "NaviTaxiMap Hanoi Old Quarter REAL OSM roads pilot",
        "description": "Tiny visual sample from OSM; NOT navigation, routing, official boundaries or full Hanoi coverage",
        "format": "pbf", "type": "overlay", "minzoom": MINZOOM, "maxzoom": MAXZOOM,
        "bounds": ",".join(map(str, bbox)),
        "attribution": "© OpenStreetMap contributors · ODbL 1.0",
        "vector_layers": [{"id": key, "fields": {}, "minzoom": MINZOOM, "maxzoom": MAXZOOM}
                          for key in LAYERS],
        "osm_source_xml_sha256": data["source_xml_sha256"],
    }, ensure_ascii=False, separators=(",", ":"), sort_keys=True).encode("utf-8")
    head = bytearray(127)
    head[:8] = b"PMTiles\x03"
    root_off = 127
    meta_off = root_off + len(directory)
    tile_off = meta_off + len(metadata)
    payload = b"".join(t[1] for t in tiles)
    values = [root_off, len(directory), meta_off, len(metadata),
              tile_off, 0, tile_off, len(payload),
              len(tiles), len(tiles), len(tiles)]
    struct.pack_into("<11Q", head, 8, *values)
    head[96:102] = bytes([1, 1, 2, 1, MINZOOM, MAXZOOM])
    west, south, east, north = bbox
    struct.pack_into("<4i", head, 102, *[round(v * 1e7)
                    for v in (west, south, east, north)])
    head[118] = 16
    struct.pack_into("<2i", head, 119,
                     round((west + east) / 2 * 1e7),
                     round((south + north) / 2 * 1e7))
    binary = bytes(head) + directory + metadata + payload
    assert len(directory) <= 16000

    summary = {
        "kind": "small REAL OSM road-preview, NOT a navigable map",
        "bbox": bbox,
        "source": data["source"],
        "osm_source_xml_sha256": data["source_xml_sha256"],
        "snapshot_at_utc": data["downloaded_at_utc"],
        "source_node_count": data["node_count"],
        "source_road_count": data["road_count"],
        "tile_count": len(tiles),
        "layer_names": list(LAYERS),
        "zoom_range": [MINZOOM, MAXZOOM],
        "tiles": [{"z": z, "x": x, "y": y} for _, _, z, x, y in tiles],
        "pmtiles_sha256": hashlib.sha256(binary).hexdigest(),
        "pmtiles_bytes": len(binary),
        "license": data["license"],
        "attribution": data["attribution"],
        "source_review": "UNREVIEWED for navigation and administrative/legal data",
    }
    return binary, summary


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--manifest", required=True)
    args = parser.parse_args()
    source = json.loads(Path(args.input).read_text(encoding="utf-8"))
    output, manifest = generate(source)
    Path(args.output).parent.mkdir(parents=True, exist_ok=True)
    Path(args.output).write_bytes(output)
    Path(args.manifest).write_text(
        json.dumps(manifest, ensure_ascii=False, sort_keys=True, indent=2) + "\n",
        encoding="utf-8")
    print(json.dumps({key: value for key, value in manifest.items() if key != "tiles"},
                     ensure_ascii=False, sort_keys=True))


if __name__ == "__main__":
    main()
