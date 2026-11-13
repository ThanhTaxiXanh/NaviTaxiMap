# Hanoi Old Quarter real-road PMTiles pilot

**Status: LIMITED REAL-DATA VISUAL PREVIEW, NOT FOR NAVIGATION.** This document describes the downloaded and pinned data, not an approved city map, legal boundary or driving feature.

## Data snapshot and provenance

- Source: [OpenStreetMap API v0.6 map by bounding box](https://api.openstreetmap.org/api/0.6/map?bbox=105.849,21.031,105.858,21.037)
- Experimental bbox: west 105.849°, south 21.031°, east 105.858°, north 21.037°. The bounding box is **not** an administrative boundary.
- Fetched 2026-11-13 05:04:02 UTC by the development-only workflow [43369521518](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43369521518).
- Raw XML SHA-256: `db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697`.
- Source XML: 6,321,243 bytes, 17,828 nodes. Normalized source: 354 OSM `way` elements with `highway` tags, 1,691 referenced road geometry points. Ways may extend past the fetch bbox; MVT output is clipped to its tile squares.
- Original raw OSM XML retained in the workflow artifact `Hanoi-OSM-pilot-source-UNREVIEWED`, along with `roads.json` and `source-manifest.json`. Git stores a compressed, normalized copy at `tools/osm_hanoi_pilot/snapshot/roads.json.gz`, omitting contributor username metadata.
- Source license: [ODbL 1.0](https://www.openstreetmap.org/copyright), **© OpenStreetMap contributors**. Downstream data and produced-work obligations require review before any full public distribution.
- The API is for lightweight small-area access; the workflow runs only on the creation/change of `.github/workflows/osm-pilot-once.yml`. **Do not make repeated or bulk source requests to the editing API**, and never scrape public tile providers.

## Deterministic tile transformation

- Generator: `tools/osm_hanoi_pilot/build_pmtiles.py` uses only Python's standard library.
- Cached normalized snapshot: `tools/osm_hanoi_pilot/snapshot/roads.json.gz`.
- Generated 21 MVT vector tiles spanning zoom 14 to 17. Source layers: `road_major`, `road_local`, `road_pedestrian`. Roads are visual polylines only; no turn rules, direction or route graph.
- Packed into PMTiles v3 with uncompressed directory, gzip-compressed tile payloads, correct Hilbert tile ordering, embedded ODbL metadata and local source-layer names.
- Asset: `app/src/main/assets/hanoi_real_roads.pmtiles` (53,956 bytes).
- Asset SHA-256: `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d`.
- Full build manifest: `tools/osm_hanoi_pilot/snapshot/build-manifest.json`.
- Generator and source checks: `tools/osm_hanoi_pilot/tests/test_pilot.py` and `tools/osm_hanoi_pilot/verify_embedded_pilot.py`. These regenerate bytes from the frozen source and compare exact hashes.
- Initial one-time build CI: [43370147765](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43370147765), where all jobs PASS. Artifact `Hanoi-real-OSM-road-preview-UNREVIEWED`.

## Android preview

Menu: **Dữ liệu bản đồ** → **Xem đường OSM Hà Nội offline (mẫu nhỏ)**.

- Android verifies the asset SHA-256, stages the PMTiles file in app-private storage and invokes MapLibre via `pmtiles://file://`.
- Offline-only style references no Internet glyphs, sprites or tiles. It draws the three road categories using distinct colors.
- User-facing UI includes attribution and **KHÔNG dẫn đường**. Any GPS recentering is suppressed while the preview is active.
- Real-data rendering must be verified by Android emulator screenshot with airplane mode and the unique road-colored pixel assertion in `ci/check-osm-pilot-screenshot.py`. Do **not** claim runtime success before that CI passes.

## Important limitations

- No complete Hà Nội road network or offline 34-province coverage. This is a tiny road-only excerpt for renderer smoke testing.
- Some untrusted OSM highways may include nonmotorized paths. Tags alone do not verify safe road access, speed limits, turn restrictions, public right of way or one-way traffic.
- There are no real legal/administrative boundaries, historical maritime claims or traffic estimates in the pilot. Do not infer map accuracy, national territory or safe navigation from OSM tags.
- Does **not** replace original source provenance review, ODbL audit, full tile directory validation, style/fonts/labels review, Xiaomi hardware testing or signed distribution.
- This asset is only a debug/technical preview, not a product release.
