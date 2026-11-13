#!/usr/bin/env python3
"""Create one nationwide + 34 province PMTiles using actual Geofabrik MVT tiles.

Inputs: a full-country Shortbread MBTiles snapshot and 34 independently audited
OSM admin_level=4 multipolygons. This algorithm ASSIGNS WHOLE MVT TILES whose
WebMercator footprints intersect the province polygon; it DOES NOT CLIP
individual MVT geometries at administrative boundaries. Users must expect a
small outside-province strip near tile edges. NEVER use as a legal border map.
No download side effects, no fake data, no empty placeholder files.
"""
import argparse
import hashlib
import json
import math
import sqlite3
import subprocess
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path

from shapely.geometry import box, shape
from shapely.strtree import STRtree

HERE = Path(__file__).resolve().parent
PROVINCES = json.loads((HERE / "provinces_34.json").read_text(encoding="utf8"))["provinces"]
OSM_SHA = "ddb9155f112af521d0674e4f40b87173f92954e5b8a3f71445d3008b506ef9ed"
SOURCE_PBF_BYTES = 332558874
KH_OSM_SHA = "2b94689adcc3bf8fbfffe83387655421598b08dae3b32a908791e1d995c595b1"


def sha256(path):
    h = hashlib.sha256()
    with Path(path).open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def norm(s):
    import unicodedata
    s = unicodedata.normalize("NFKD", str(s).casefold())
    s = "".join(c for c in s if not unicodedata.combining(c)).replace("đ","d")
    for token in ("thanh pho ","tinh ","province","city","municipality"):
        s = s.replace(token, " ")
    return "".join(c for c in s if c.isalnum())


def load_provinces(admin4_path, kh_path):
    all_features = json.loads(Path(admin4_path).read_text(encoding="utf8"))["features"]
    kh = json.loads(Path(kh_path).read_text(encoding="utf8"))["features"]
    assert len(kh) == 1 and norm(kh[0]["properties"]["name"]) == norm("Khánh Hòa")
    all_features += kh
    assert len(all_features) == 34, "Expected exactly 34 OSM admin4 features"
    geom = []
    features_by_name = defaultdict(list)
    for feat in all_features:
        props = feat.get("properties") or {}
        name = props.get("name:vi") or props.get("name")
        geom_type = (feat.get("geometry") or {}).get("type")
        assert geom_type in ("Polygon", "MultiPolygon"), f"Invalid OSM boundary {name}"
        g = shape(feat["geometry"])
        assert g.is_valid and not g.is_empty, f"Invalid geometry {name}"
        assert 8 < g.bounds[1] < 25 and 100 < g.bounds[0] < 120, name
        assert 8 < g.bounds[3] < 26 and 100 < g.bounds[2] < 120, name
        features_by_name[norm(name)].append((name, g))
    for p in PROVINCES:
        matches = features_by_name.get(norm(p["name"]), [])
        assert len(matches) == 1, f"Missing/ambiguous OSM admin4 polygon: {p['name']} ({len(matches)})"
        geom.append(matches[0][1])
    assert len(geom) == 34
    return geom


def tile_bounds(z, x, tms_row):
    n = 1 << z
    y = n - 1 - tms_row
    def lat(v):
        return math.degrees(math.atan(math.sinh(math.pi * (1 - 2 * v / n))))
    return box(x / n * 360 - 180, lat(y + 1),
               (x + 1) / n * 360 - 180, lat(y))


def make_db(path, source_meta, province, polygon):
    con = sqlite3.connect(str(path))
    con.execute("PRAGMA journal_mode=OFF")
    con.execute("PRAGMA synchronous=OFF")
    con.execute("PRAGMA cache_size=-10000")
    con.execute("CREATE TABLE metadata (name TEXT PRIMARY KEY, value TEXT)")
    con.execute("CREATE TABLE tiles (zoom_level INTEGER NOT NULL, tile_column INTEGER NOT NULL, tile_row INTEGER NOT NULL, tile_data BLOB NOT NULL, PRIMARY KEY (zoom_level,tile_column,tile_row))")
    data = dict(source_meta)
    bounds = polygon.bounds
    data.update({
        "name": f"NaviTaxiMap {province['name']} - OSM Shortbread (UNREVIEWED)",
        "description": ("Geofabrik OSM MVT, province region chosen by OSM admin4 tile intersection. "
                        "Tile edges extend outside polygon. Not a legal boundary or navigation dataset."),
        "minzoom": "8",
        "maxzoom": "14",
        "bounds": ",".join(f"{v:.7f}" for v in bounds),
        "center": f"{(bounds[0]+bounds[2])/2:.7f},{(bounds[1]+bounds[3])/2:.7f},10",
        "attribution": "© OpenStreetMap contributors (ODbL); tiles: Geofabrik Shortbread",
        "navitaximap_source_approval": "UNREVIEWED_ROAD_BASEMAP",
        "navitaximap_navigation_approved": "false",
        "navitaximap_province_code": province["code"],
        "navitaximap_province_clip": "WHOLE_TILE_INTERSECTION_NOT_EXACT_BOUNDARY"
    })
    con.executemany("INSERT INTO metadata(name,value) VALUES (?,?)", data.items())
    con.commit()
    return con


def source_tiles(conn):
    names = {r[0] for r in conn.execute("SELECT name FROM sqlite_master WHERE type IN ('table','view')")}
    if "tiles" in names:
        q = "SELECT zoom_level,tile_column,tile_row,tile_data FROM tiles"
    elif {"map","images"} <= names:
        q = ("SELECT m.zoom_level,m.tile_column,m.tile_row,i.tile_data "
             "FROM map m JOIN images i ON m.tile_id=i.tile_id")
    else:
        raise ValueError(f"Unsupported MBTiles schema: {sorted(names)}")
    return conn.execute(q)


def validate_pmtiles(file):
    with Path(file).open("rb") as f:
        header = f.read(127)
    assert header[:8] == b"PMTiles\x03", f"Bad PMTiles magic {file}"
    import struct
    values = struct.unpack_from("<11Q", header, 8)
    size = Path(file).stat().st_size
    assert size >= 127 and 0 < values[1] <= 16384
    assert values[6] + values[7] <= size
    assert header[99] == 1, "Only PMTiles MVT vector tiles accepted"
    assert header[100] <= header[101] <= 14
    return {"size_bytes":size, "sha256":sha256(file),
            "minzoom":header[100],"maxzoom":header[101],
            "tiles":values[8]}


def build(source, admin4, kh, outdir, pmtiles_cli="pmtiles"):
    outdir.mkdir(parents=True, exist_ok=True)
    polygons = load_provinces(admin4, kh)
    source_db = sqlite3.connect(f"file:{source}?mode=ro", uri=True)
    metadata = dict(source_db.execute("SELECT name,value FROM metadata"))
    assert metadata.get("format") in ("pbf","mvt"), metadata.get("format")
    source_maxzoom = int(metadata.get("maxzoom", "14"))
    assert source_maxzoom >= 14, f"Source needs road zoom 14, got {source_maxzoom}"
    assert int(metadata.get("minzoom", "0")) <= 8

    src_bytes = Path(source).stat().st_size
    assert 300_000_000 < src_bytes < 1_300_000_000, "Unexpected country source size"
    print("SHORTBREAD_SOURCE",json.dumps({"bytes":src_bytes,"sha256":sha256(source),
                                          "format":metadata.get("format"),
                                          "schema":metadata.get("json","")[:80]}),flush=True)

    root = outdir.parent / "work-mbtiles"
    root.mkdir(parents=True, exist_ok=True)
    regions = []
    conns = []
    for p, geom in zip(PROVINCES, polygons):
        db = root / f"{p['code']}-{p['slug']}.mbtiles"
        conns.append(make_db(db, metadata, p, geom))
        regions.append({"code":p["code"],"name":p["name"],"slug":p["slug"],
                        "geometry_bounds":[round(n,6) for n in geom.bounds],
                        "tile_count":0, "mbtiles":str(db)})
    tree = STRtree(polygons)
    pending = defaultdict(list)
    total_src_tiles = 0
    total_region_copies = 0
    zoom_count = defaultdict(int)

    def flush():
        nonlocal total_region_copies
        for i, items in pending.items():
            if not items: continue
            conns[i].executemany("INSERT OR IGNORE INTO tiles(zoom_level,tile_column,tile_row,tile_data) VALUES (?,?,?,?)",items)
            total_region_copies += len(items)
            items.clear()

    for z, x, row, tile_data in source_tiles(source_db):
        total_src_tiles += 1
        z, x, row = int(z), int(x), int(row)
        zoom_count[z] += 1
        if not 8 <= z <= 14:
            continue
        tile = tile_bounds(z, x, row)
        # Shapely 2 STRtree.query returns array of geometry indices.
        for i in tree.query(tile):
            idx = int(i)
            if polygons[idx].intersects(tile):
                pending[idx].append((z, x, row, tile_data))
                regions[idx]["tile_count"] += 1
        if total_src_tiles % 1500 == 0:
            flush()
    flush()
    for c in conns:
        c.commit()
        c.close()
    source_db.close()
    print("SHORTBREAD_SPLIT",json.dumps({"source_tiles":total_src_tiles,
          "province_tile_copies":total_region_copies,"zoom_counts":zoom_count,
          "per_province":{r["code"]:r["tile_count"] for r in regions}}),flush=True)
    assert total_src_tiles > 10_000, "Country input too sparse"
    assert sum(zoom_count[z] for z in range(12,15)) > 1000, "Missing street-level zoom tiles"
    assert all(r["tile_count"] >= 20 for r in regions), "Province lacks geographic tile coverage"

    national = outdir / "00-viet-nam-all.pmtiles"
    subprocess.run([pmtiles_cli,"convert",str(source),str(national)],
                   check=True,timeout=2400)
    results = [{"code":"VN","name":"Việt Nam (Geofabrik Vietnam extract, NOT official boundaries)",
                "slug":"viet-nam-all","tile_count_selected":total_src_tiles,
                "scope":"GEOGRAPHIC_GEOFABRIK_EXTRACT",
                "file":national.name, **validate_pmtiles(national)}]
    for r in regions:
        pm = outdir / f"{r['code']}-{r['slug']}.pmtiles"
        subprocess.run([pmtiles_cli,"convert",r["mbtiles"],str(pm)],
                       check=True,timeout=420)
        record = {k:v for k,v in r.items() if k!="mbtiles"}
        record.update({"scope":"OSM_ADMIN4_WHOLE_TILE_INTERSECTION",
                       "file":pm.name, **validate_pmtiles(pm)})
        assert record["maxzoom"] == 14, r["name"]
        results.append(record)
        print("VN34_PACK",json.dumps({k:record[k] for k in
              ("code","file","tiles","size_bytes","sha256")},ensure_ascii=False),flush=True)
        Path(r["mbtiles"]).unlink()
    manifest = {
        "schema":"navitaximap-vn34-geofabrik-shortbread-2026-v1",
        "created_utc":datetime.now(timezone.utc).isoformat(),
        "expected_files":35,"files_count":len(results),
        "source_mbtiles":"https://download.geofabrik.de/asia/vietnam-shortbread-1.0.mbtiles",
        "source_mbtiles_sha256":sha256(source),
        "osm_boundaries_snapshot_pbf_sha256":OSM_SHA,
        "osm_boundary_recovery_khanhhoa_full_relation_sha256":KH_OSM_SHA,
        "osm_source_license":"ODbL 1.0, © OpenStreetMap contributors",
        "tile_generator":"Geofabrik experimental Shortbread 1.0",
        "maximum_zoom":14,
        "rendering_notes":"Requires compatible Shortbread vector style. No GPS routing graph.",
        "province_selection":"OSM 2026 admin4 polygon intersects full tile envelope. Does not clip MVT features; edge tiles may show adjacent region/ocean.",
        "administrative_geometry_status":"UNREVIEWED / NOT a legal boundary or sovereignty overlay",
        "navigation_approved":False,
        "distribution_approved":False,
        "packages":results,
    }
    assert len(results)==35 and len({r["file"] for r in results})==35
    (outdir/"manifest.json").write_text(
        json.dumps(manifest,ensure_ascii=False,indent=2,sort_keys=True)+"\n",
        encoding="utf8")
    print("VIETNAM34_BUILD_SUCCESS",json.dumps({
        "count":len(results),"national_mb":round(national.stat().st_size/1e6,1),
        "province_total_mb":round(sum(r["size_bytes"] for r in results[1:])/1e6,1),
        "source_sha256":manifest["source_mbtiles_sha256"]}),flush=True)
    return manifest


if __name__=="__main__":
    ap=argparse.ArgumentParser()
    ap.add_argument("--mbtiles",type=Path,required=True)
    ap.add_argument("--admin4",type=Path,required=True)
    ap.add_argument("--khanhhoa",type=Path,required=True)
    ap.add_argument("--outdir",type=Path,required=True)
    ap.add_argument("--pmtiles-cli",default="pmtiles")
    args=ap.parse_args()
    build(args.mbtiles,args.admin4,args.khanhhoa,args.outdir,args.pmtiles_cli)
