# Vietnam real-road offline data: source-to-PMTiles acceptance plan

**Status (2026-11-13): RESEARCH / SOURCE CANDIDATE ONLY.** No real road extract has been downloaded, processed or approved for inclusion in NaviTaxiMap. This document is NOT a certification of current maps or borders.

## Potential lawful open-data input

- **Geofabrik Vietnam OSM extract:** https://download.geofabrik.de/asia/vietnam.html
  - Offers `vietnam-latest.osm.pbf` (OpenStreetMap raw extract), typically updated daily.
  - Geofabrik description and license: https://www.geofabrik.de/data/download.html
  - OSM-derived data on this service is under **Open Database License 1.0 (ODbL)**. Attribution to OpenStreetMap contributors, database share-alike requirements and any published derived database obligations must be reviewed before distribution.
- **Vector conversion option:** `tilemaker` supports `--input ...osm.pbf --output ...pmtiles --config ...json --process ...lua`.
  - Reference: https://github.com/systemed/tilemaker/blob/master/docs/RUNNING.md
  - Tile schema and style compatibility depend on the exact config and Lua process; **not** automatically solved by changing the output file extension.
  - Do not use public map tile servers for mass downloading or unapproved offline redistribution.

## Proposed reproducible pipeline (NOT executed)

1. Record source page, precise fetch timestamp, extract's OSM data timestamp, provenance, ODbL notice, license version and exact checksum. Keep this in a reviewed manifest.
2. Pin a `vietnam-latest.osm.pbf` download to an immutable, locally hashed snapshot; reject unexpected source changes. For testing, extract only a small *verified* bounding box using Osmium; a box is a practical crop, **not** a legal province boundary.
3. Generate an MVT vector tile archive with a pinned, locally tested `tilemaker` version and reviewed Lua/config layer schema; store resulting `.pmtiles` separately from the Android APK. Never bake large-country data into the app before storage/performance testing.
4. Check full PMTiles header and directory indexing, validate tile coordinate coverage, sample real OSM road geometry, style/vector layer matches, and verify SHA-256. Use real-world coordinates and offline emulator screenshots instead of synthetic lines.
5. Bundle a **fully offline** MapLibre style including reviewed local glyphs, sprites, fonts and attribution. Do not use any remote URL in offline mode.
6. Check OSM attribution and ODbL obligations in the UI and pack metadata; document tile availability, geographic age, size, generation tools, SHA-256 and usage limitations.
7. Only after legal/geographic/source review, publish a small pilot pack to the app's **approved** manifest/catalog. Do not bypass `sourceApproval=UNVERIFIED` for user-imported files.

## Sovereignty and administrative boundaries

- This OSM input is for road basemap processing and does not by itself verify official boundaries, names, disputed maritime features or legal claims.
- Do **not** infer the 34-province canonical geometry from an extract's `.poly` footprint, bounding box or incidental OSM tag.
- Keep basemap road data and provenance-validated administrative/maritime overlays separate. The project's uploaded geographic policy engine is necessary but not sufficient to approve any individual claim.
- No routing graph, GPS guidance, traffic source or speed data is supplied by an MVT visual tile archive.

## Current blockers

No Geofabrik dataset snapshot obtained; no available local `tilemaker` build tested; no reviewed small-region boundary, styles, scripts or independent offline real-road screenshot; no completed ODbL distribution review. This must remain IN PROGRESS.
