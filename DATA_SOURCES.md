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
