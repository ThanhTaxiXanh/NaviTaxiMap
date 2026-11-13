# NaviTaxiMap architecture

## Current implementation
```
MainActivity (online MapLibre UI + permission-gated GPS)
  |-- MapLibre Native (OpenFreeMap Liberty/Dark remote style)
  |-- DemoOfflinePack (synthetic PMTiles fixture -> private file -> MapLibre)
  |-- LocalPackStore (user selected content:// -> staged private PMTiles)
       |-- SHA-256 streaming copy and re-verification
       |-- PmtilesHeader (v3 structural checks, MVT only)
       |-- private .properties index with UNVERIFIED provenance
       |-- list / verify / delete
```

App modules currently live in `app`; the future module boundaries below are architectural plans, not implemented Android modules.

## Future components
`Map Renderer -> MapData/PMTiles -> Offline Package Manager -> licensed region packs`
`GPS -> Navigation -> Routing -> offline road graph`
`Search/POI -> SQLite`
`Road Intelligence/Traffic -> approved dated data sources`
`Vietnam Geographic Engine -> provenance + legal archive`

## Design constraints
- Offline tiles and offline routing graphs are different resources; rendering PMTiles alone is not navigation.
- MapLibre Native supports `pmtiles://file://` but **does not provide bulk offline pack downloads/caching**. The application must own package lifecycle.
- Android SAF `ACTION_OPEN_DOCUMENT` returns access to a selected URI, not blanket filesystem permissions.
- SHA-256 alone is integrity against a known hash, not source attribution, sovereignty certification or licensing.
- Imported files are **never automatically promoted to approved road data**.
- App-private storage and streaming avoid large memory buffers. Per-pack storage transactions and interrupted-download recovery still need engineering.
- No Android root operations, paid dependencies, fabricated traffic/speed limits or speculative maritime lines.

## Verified baseline
- CI 43359838414: offline synthetic MVT geometry rendered in airplane mode.
- Phase 4 import JUnit and Android APK assemble PASSED on GitHub; full current emulator QA pending.
- Physical Xiaomi debug unavailable due to absent device connection.

## Phase 4 crash-recovery layer (CI 43364225600 verified)
- `LocalPackStore.recoverInterruptedImports()` runs on startup and before streamed import on its single app executor; cleans only matching private UUID-generated partial files and unpaired archives/indexes.
- A package is visible only when index/PMTiles pair is present with matching recorded file length. `verifyPack` re-hashes SHA-256 on demand.
- Streamed free-space preflight preserves default 8 MiB reserve. Current in-process `@Synchronized` methods are not a multi-process lock, and directory fsync and production atomic updates are not implemented.
- Emulator QA injected four interrupted-import files, force-stopped and restarted app, confirmed cleanup and unchanged valid PMTiles SHA-256.

## Real road-only pilot (development-only, not routing)
`OSM XML snapshot -> normalized highway ways (frozen JSON.gz) -> deterministic Python MVT/PMTiles builder -> bundled hanoi_real_roads.pmtiles -> app-private checksum-validated file -> MapLibre pmtiles://file:// source -> 3 local vector road line layers`.
- Source snapshot and PMTiles SHA-256 are pinned; converter tested with no third-party Python packages. The 21-tile archive contains actual OSM way geometry cropped into map tile viewports.
- No remote style, sprite or glyph dependencies for this preview. Attribution and safety warning appear in the status display.
- This is a tiny road-only sample, not a local route graph, offline search index, traffic feed or any legally reviewed geographic boundary overlay.

## Phase 4 pinned catalog and transfer manager
`APK PilotCatalog (id, version, HTTPS immutable commit URL, SHA-256, size, OSM source SHA, ODbL)`
→ **explicit user consent** → `PinnedHttpsPackDownloader` (TLS, no redirects, timeout)
→ `CatalogPackStore.install` (staged streamed copy, SHA/size/header, quota margin, fsync, atomic data promotion, index commit)
→ app-private `catalog-packs` (versioned archive + properties, `UNREVIEWED_VISUAL_PREVIEW`)
→ MapLibre native local `pmtiles://file://` renderer with attribution, **no navigation**.
- Local asset install uses the exact same catalog store/checks and works in airplane mode.
- Prior metadata/PMTiles version persists through failed update tests; no background scheduler or remote manifest. One catalog item now; no signed distribution channel.
- CI 43374393332: 23 JVM tests, real HTTPS to emulator, on-device SHA readback, switch to airplane mode, real-road colored pixels and catalog UI passed.

## Large-area 35-pack generation pipeline
1. Decision 19/2025/QD-TTg names/codes in `tools/vietnam34/provinces_34.json`.
2. Geofabrik latest OSM PBF -> osmium admin_level=4 relation geometry audit (33 polygons); separately OSM API relation 1887959 full geometry for Khánh Hòa (34th). Both sources pinned by SHA256.
3. Geofabrik experimental Shortbread 1.0 MBTiles (686.7MB) -> `tools/vietnam34/build_national_and_provinces.py` -> use Shapely STRtree to select preexisting, real MVT tile bytes at z8–14 intersecting each technical OSM province polygon.
4. Pin go-pmtiles CLI v1.31.2 to convert MBTiles into 34 province PMTiles and full nationwide original MBTiles into country PMTiles z0–14. Preserve original MVT layer schema; no per-feature boundary clipping or republishing of external raster tiles.
5. Output `manifest.json` containing 35 files, full SHA-256, source pointers, tile counts, license and safety flags. Independent CI 43379440781 rehashes each artifact and validates PMTiles v3 MVT headers.
6. **No Android 35-pack runtime renderer or distributed regional catalog implemented.** Shortbread-compatible offline style and download manager remain separate future work.


## 2026-11-13 stronger Shortbread offline 35-pack import
- Frozen 35-entry checksum and exact-byte ledger in Vietnam35Catalog + PACK_BYTES_VERIFIED.tsv, sourced from independent 43379440781 rehash evidence. Renderer only activates recognized/manual SAF-imported PMTiles after fresh full-file hash and PMTiles header validation.
- For recognized files, initial DocumentsUI name/size hint and device usable capacity preflight, then LocalPackStore streaming expected-size cap and exact SHA+length before committing index; unrecognized user imports still explicitly UNVERIFIED.
- Android visual evidence currently **3/35** (Bắc Ninh, Hà Nội, Hồ Chí Minh); no nationwide QA, approved online data endpoint, routing, legal boundaries, or production release.
