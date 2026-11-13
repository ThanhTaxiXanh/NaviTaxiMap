# PROGRESS
2026-11-13 | Phase 4 import prototype
- Confirmed prior offline PMTiles renderer CI 43359838414 PASS.
- Added `PmtilesHeader`: defensive PMTiles v3 preflight for MVT, ranges, zooms, coordinates and compression flags.
- Added `LocalPackStore`: streaming copy, optional expected SHA-256, app-private staging, atomic per-file promotion, .properties index, listing, re-hash and delete.
- Added JUnit tests for valid import, corruption, oversize, wrong hash and unsafe IDs.
- Added Android SAF document picker and menu for import, display, validation and deletion.
- Added DEBUG-only fixture import for emulator, runs while airplane mode enabled.
- GitHub build / testDebugUnitTest PASS for Phase 4 source.
- CI 43361851396: metadata and archive exist on emulator but smoke falsely failed due to empty tag-filtered logcat output; revised smoke to read and hash actual app-private archive.
- Latest byte-level smoke CI 43362607683: PASS. 1268-byte PMTiles archive re-read with Android run-as, SHA-256 matched and index sourceApproval=UNVERIFIED. Emulator and Android/Python test jobs PASS.
- All changes on develop branch, no main merge.

## Phase 4 reliability, 2026-11-13
- Added recovery of stale .partial files, orphan data archives and orphan metadata indexes on app start and before import. File matching limited to private generated UUIDs.
- Added free-space safety margin (default 8 MiB), enforced on each streamed chunk; added capacity accounting and UI usage summary.
- Added JVM tests for interrupted stream, insufficient free storage, restart cleanup and preservation of committed packs (LocalPackStoreTest now 9 tests).
- Added emulator regression that injects four incomplete files into app-private storage, force-stops and restarts app, then verifies cleanup plus byte-identical committed pack.
- CI 43364225600: build/tests/emulator online/offline/import/recovery ALL PASS.
- Researched official Geofabrik Vietnam OSM PBF candidate and tilemaker PMTiles build path; documented candidate without downloading or approving real road data in REAL_OSM_SOURCE_PLAN.md.
- Kept main untouched and no release.

## Phase 4 Android file-picker proof, 2026-11-13
- Added `ci/saf-picker-smoke.py`: UIAutomator XML-driven tap of app Data menu, Android DocumentsUI file chooser, selection of downloaded local synthetic fixture and on-device readback.
- First CI 43367038303: build and previous emulator tests PASS, but UI automation clicked the preview icon instead of file title; result did not select the document. Diagnosed from CI screenshot, XML bounds and node resource IDs.
- Fixed UI test to select the exact `android:id/title` node for the filename; rerun 43367761361: **ALL JOBS PASS**.
- On Android 34 emulator in airplane mode, selected PMTiles through actual system picker, app imported file, UI confirmation displayed, same 1268 bytes re-read, SHA-256 matched, index remains UNVERIFIED.
- Artifact persisted with screenshots/XML and file hashes. Main unchanged, no release.
- Real OSM regional dataset still not present: candidate Geofabrik URL is documented, but no reviewed snapshot was obtained during this development session.

## Phase 4 tiny real Hanoi OSM road preview, 2026-11-13
- Official OSM API v0.6 one-time tiny bbox download completed in GitHub runner and pinned by XML SHA-256 (run 43369521518, source artifact).
- Parsed 17,828 source nodes, 354 OSM highway ways, 1,691 road geometry references; kept a normalized and compressed road source in git without contributor usernames.
- Wrote pure-Python deterministic OSM-to-MVT PMTiles v3 converter. Generated 21 vector tiles at zoom 14–17 (53,956 bytes), plus reproducible manifest. Generator/unit tests and one-time build workflow 43370147765 PASS.
- Added separate MapLibre Native real-road offline pilot (line categories road_major, road_local, road_pedestrian) with visible © OpenStreetMap contributors (ODbL) attribution and KHÔNG dẫn đường label. Existing synthetic diagnostic remains separate.
- Android emulator screenshot run 43370534933 confirmed real OSM roads are visible offline in airplane mode. Test script failed due missing Pillow in CI, not due map rendering; corrected with existing stdlib PNG decoder, latest retry 43371354137 **ALL JOBS PASS**, including 58,015 locally rendered road-colored pixels verified by stdlib PNG checker and on-screen ODbL / no-navigation warning.
- No citywide data, accurate driving navigation or licensed full 34-province package yet.

## Phase 4 versioned catalog + HTTPS verified, 2026-11-13
- Added APK-pinned `CatalogPackSpec` with strict HTTPS GitHub raw allowlist requiring immutable 40-digit commit SHA (not mutable `develop` or `main`), checksum, size, zoom range, OSM source SHA, ODbL and attribution. Only one tiny Hanoi road-only preview entry is currently present.
- Added `CatalogPackStore`: streamed input with 8 MiB free-space safety margin, exact pinned size/SHA/MVT header checks, atomic same-filesystem data/index commit, previous-version rollback on failures, generated-orphan cleanup and deletion without changing external documents.
- Added `PinnedHttpsPackDownloader`: explicit network GET with redirect denial, 15s connect and 20s read timeouts; no remote JSON trust, no background synchronization. Added cancellation/progress.
- Refactored `HanoiRoadPilot` to use the same catalog for APK-local or pinned-HTTPS installation; real offline renderer continues to work after airplane-mode transition.
- Added **12 JVM tests** for URL restrictions, catalog versions, tampering, update rollback, failure/cancel cleanup, storage pressure, source checks and injected downloader.
- Added Android catalog manager UI: information, verify SHA-256, repair from bundled APK, manual HTTPS transfer with confirmation and cancel, delete app-private copy.
- CI 43374393332 ALL JOBS PASS, including actual HTTPS download, SHA readback from emulator, ODbL/non-navigation metadata, offline colored roads (58,015 pixels), catalog dialog UI, SAF importer and simulated interruption cleanup.
- Kept `main` untouched. Real Hanoi pilot remains a very small **unreviewed visual sample**; NOT navigation-ready, NOT 34 provinces.

## 35 OSM real Vietnam PMTiles packages: produced and independently verified 2026-11-13
- Loaded official 34 province/city code/name list from Decision 19/2025/QD-TTg; archived in `tools/vietnam34/provinces_34.json`.
- Captured Geofabrik OSM PBF with SHA-256 and built 33 admin4 multipolygons. First PBF polygon export omitted Khánh Hòa; corrected by fetching a single full OSM relation 1887959 from the OSM official API, hashed separately. All 34 polygon inputs validated, but are **not legal authority for boundaries**.
- Downloaded full Geofabrik experimental Shortbread MVT MBTiles (686,698,496 B). Split into 34 packs using whole-tile intersection with OSM administrative polygons; converted full-country MBTiles to one more PMTiles. No synthetic roads or placeholder empty provinces.
- 35 PMTiles built: nationwide 556,229,998 bytes/567,706 tiles; 34 provinces 287,504,805 bytes. All have source provenance, ODbL attribution and unreviewed/non-navigation status in manifest.
- CI 43378133931 build PASS. CI 43379440781 independent rehash and structural PMTiles validation **35/35 PASS**. Artifacts and full file inventory in `VIETNAM35_PACKS.md`.
- **Still needs** user-facing 35-pack catalog, Shortbread local MapLibre style, streamed transfer with resumability, independent spatial/license/sovereignty review, mobile hardware/performance tests.
