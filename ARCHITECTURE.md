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
