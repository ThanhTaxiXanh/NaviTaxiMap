# DATA_SOURCES
Updated: 2026-11-13

| Source | Use | Verification and restrictions |
|---|---|---|
| MapLibre Native Android OpenGL ES 13.5.4 | Map renderer, native PMTiles file reader | GitHub build + emulator offline render verified |
| OpenFreeMap Liberty/Dark styles | Online map for prototype | OpenFreeMap public service; no uptime SLA; terms [14 Oct 2026](https://openfreemap.org/tos/) |
| OpenStreetMap | Road/background geography for online preview | OpenStreetMap ODbL, attribution required; no 34-province offline packages imported yet |
| Synthetic fixture from `tools/pmtiles_fixture/generate.py` | Vector tile MVT z0/z1 integration test | Self-generated geometry, zero geospatial accuracy; 5 tile entries, SHA-256 verifier, PASS in airplane mode |
| PMTiles data for Vietnam | Future offline basemap | NOT STARTED: source review, boundary segmentation, metadata and legal distribution |
| Hoàng Sa / Trường Sa legal/geographic sources | Future archive / labels | BLOCKED until independently approved references, attribution, legal boundaries and redistribution |
| Live traffic | Future optional data | BLOCKED: no authorized sustainable free provider |

Online tile provider terms restrict offline downloads to **up to 3,000 tiles or 50 MB per download**, no more than **4 parallel requests**, with a unique app User-Agent.
This prototype neither bulk-downloads tiles from OpenFreeMap nor stores online traffic.
No fabricated speeds, legal maritime boundaries, or roads are used in the local fixture.
