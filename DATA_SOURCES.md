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
- No downloaded real OSM snapshot, validated province boundary or reviewed style has been integrated. See `REAL_OSM_SOURCE_PLAN.md`.
