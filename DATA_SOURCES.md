# DATA_SOURCES
Updated: 2026-11-13

| Source | Use | Status |
|---|---|---|
| MapLibre Native Android OpenGL ES 13.5.4 | Renderer and native PMTiles file reading | Build/emulator with synthetic tiles VERIFIED |
| OpenFreeMap Liberty / Dark | Prototype online street map | Public service, no uptime SLA; terms: https://openfreemap.org/tos/ |
| OpenStreetMap | Online road geography via external tiles; candidate for legal self-produced regional packs | ODbL attribution and redistribution obligations not yet audited for offline release |
| Synthetic fixture `tools/pmtiles_fixture/generate.py` | MVT PMTiles z0/z1 test only | In-house synthetic geometry, checksum verified, no real streets |
| User-selected PMTiles documents | Local import via SAF | Source ownership and geographic accuracy UNKNOWN, stored with `sourceApproval=UNVERIFIED`; not used for navigation |
| Actual Vietnam provincial PMTiles | Future offline map | NOT STARTED, source and licenses require validation |
| Hoàng Sa / Trường Sa | Future historical/legal reference layers | NOT APPROVED, no speculative borders |
| Real-time traffic | Future optional module | BLOCKED; no stable authorized no-cost source established |

Do not use the public OpenFreeMap server for bulk scraping or unauthorized offline redistribution. Source licenses and style attribution must be reviewed before real packs are imported and displayed.

## Pilot source candidate (researched, NOT downloaded)
- **Geofabrik Vietnam**: https://download.geofabrik.de/asia/vietnam.html offers the OSM raw data PBF and publishes updates; Geofabrik states OSM-derived downloads use ODbL 1.0 and require attribution: https://www.geofabrik.de/data/download.html.
- **tilemaker** can convert OSM PBF to PMTiles with explicit config/process scripts: https://github.com/systemed/tilemaker/blob/master/docs/RUNNING.md.
- A separate tiny OSM API source snapshot and real-road PMTiles pilot are now integrated and pinned, but there is still **no real provincial dataset, independently reviewed geometry, or production style**. See `HANOI_OSM_PILOT.md` and `REAL_OSM_SOURCE_PLAN.md`.

## Verified small real-road pilot input (not full provincial coverage)
- **OpenStreetMap API bbox**: https://api.openstreetmap.org/api/0.6/map?bbox=105.849,21.031,105.858,21.037 . One small snapshot on 2026-11-13 05:04:02 UTC. Download XML 6,321,243 bytes, SHA-256 `db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697`, 17,828 nodes, 354 highway ways. Workflow evidence 43369521518.
- **Derived road-only PMTiles**: 21 tiles, zoom 14–17, 53,956 bytes, SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d`. Source, converter and manifest are pinned in git; generator CI 43370147765 PASS.
- **License**: OpenStreetMap ODbL 1.0, mandatory attribution **© OpenStreetMap contributors**. User-visible attribution displayed in dedicated preview status; full public distribution/share-alike audit remains OPEN.
- **Source review**: UNREVIEWED for turn restrictions, navigation, admin/maritime boundaries. Snapshot is a tiny rectangular crop, not a verified province. No Geofabrik full-country data was downloaded. See `HANOI_OSM_PILOT.md`.

## Single allowlisted HTTPS pack source (development only)
- Immutable source URL: `https://raw.githubusercontent.com/ThanhTaxiXanh/NaviTaxiMap/554cfe62013c80146842aea85cb3625e583f2ced/app/src/main/assets/hanoi_real_roads.pmtiles`.
- Android CI 43374393332 fetched this exact 53,956-byte byte sequence from HTTPS, verified SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d` and then rendered offline in airplane mode. Filename/URL is pinned in `PilotCatalog` and refuses branch-name substitutions.
- This is source-identical to the frozen OpenStreetMap API bbox snapshot, not a verified 34-province map. ODbL attribution remains mandatory; `UNREVIEWED_VISUAL_PREVIEW` is not geographic/legal approval.
