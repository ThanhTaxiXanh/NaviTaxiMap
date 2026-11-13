# Shortbread 1.0 Android integration checkpoint

**Status: code committed; new CI pending.** This is NOT a navigation or legally approved map.

The SHA-pinned list of 35 OSM/Geofabrik Shortbread v1.0 research PMTiles is in app/src/main/java/vn/navitaximap/data/Vietnam35Catalog.kt. A catalog entry contains the exact official filename, code, zooms and SHA-256 from tools/vietnam34/SHA256SUMS.txt. No source URL is invented: the old GitHub Actions data artifacts are temporary and the project has no approved durable regional download host.

Android user flow: import an original PMTiles archive with Data menu -> import from device (SAF). The new Data menu -> 35 Shortbread packs visualizes recognized files only after comparing filename+zoom+SHA and computing the whole-file SHA again before rendering. The style in app/src/main/assets/shortbread_offline_style.json uses only the app-private pmtiles://file:// archive, paints streets/land/water/buildings, no sprites, glyphs, labels or legal boundary overlays. It displays OSM attribution and a no-navigation warning. The separate 54KB Hanoi road-only pilot is unchanged.

CI: Android debug workflow runs Kotlin unit tests and Python static schema/catalog tests. Dedicated android-shortbread-35-smoke workflow retrieves the previously generated 34-province artifact, checks actual Bắc Ninh PMTiles SHA-256, drives Android DocumentsUI SAF import and on-device SHA readback, switches to airplane mode, renders map and checks screenshot road-color pixel count, attribution and no-navigation warning. Only report PASS after CI 43385141310 and 43385141410 conclude successfully with evidence.

Unfinished: durable approved storage/download URLs, exact size-pinned resumable download, legality/ODbL audit, physical Xiaomi, nationwide coverage pixel tests, POI labels and routing. Artifacts expire 2026-11-27. Main/PR/merge/release remain prohibited without owner's approval.


## Verified limited milestone
Run 43385141410 completed SUCCESS. Artifact: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410/artifacts/14017389861. The real 24-bac-ninh.pmtiles has exactly 11,049,420 bytes and sha256 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f. Android SAF imported it, offline style drew 266,196 road-color pixels, OSM attribution and no-navigation warning were exposed while airplane mode was enabled. This proves one regional renderer, not 35/35 maps or routing readiness.


## Build and regression completion
Source feature commit: 4a55a8734dff9f75b451cdb29414414c92a64104.
Standard Android CI 43385141310: all jobs SUCCESS, existing Hanoi real-road sample regression preserved (58,015 screenshot colored pixels).
Dedicated Bắc Ninh Shortbread Android CI 43385141410: all jobs SUCCESS, 11,049,420-byte SHA-pinned real archive via SAF DocumentsUI, airplane-mode 266,196 street pixels, OpenStreetMap ODbL attribution and no-navigation warning. Evidence expires 2026-11-27: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410/artifacts/14017389861.
Not a demonstration of 35 region files rendered on device; 35 pins/catalog tested, visual rendering verified for 1 only.


## 2026-11-13 catalog byte pin and multi-region tests
- New `VietnamRegionSpec.sizeBytes` pins 35 exact archives from independent verifier log 43379440781, ledger `tools/vietnam34/PACK_BYTES_VERIFIED.tsv`; all archive names, zooms, lengths and SHA hashes are checksummed during import, no remote catalog.
- SAF import of recognized file performs Android DocumentsUI size preflight if available, free-space check, then bounded streamed SHA/length validation before promotion; unrecognized imports remain untrusted.
- Real screenshots: 01 Hà Nội 157,112 road pixels and 79 Ho Chi Minh City 183,085, CI https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43387058322 success, evidence artifact 14019316787. With 24 Bắc Ninh old run, 3/35 verified. Source NOT approved for navigation/legal maritime lines.
- A fresh Bắc Ninh CI encountered emulator storage race before import (43387058086 FAIL). A separate retry helper was committed (a3e53366), new CI pending.
