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
