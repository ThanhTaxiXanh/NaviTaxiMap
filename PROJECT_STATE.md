# PROJECT_STATE
Updated: 2026-11-13, Asia/Bangkok
Repo: ThanhTaxiXanh/NaviTaxiMap
Working branch: develop/phase0-android-maplibre
main: protected; do not push, merge, create PR or release until owner explicitly approves.

## Phase status
- Phase 0: IN PROGRESS; GitHub/CI verified, Xiaomi Termux/Shizuku not connected.
- Phase 1: IN PROGRESS; Kotlin Android shell and UI, build/emulator smoke proven.
- Phase 2: IN PROGRESS; MapLibre + OpenFreeMap online roads rendered in emulator. Still not a production basemap.
- Phase 3: IN PROGRESS; native PMTiles v3 offline reader/render **proof verified only for synthetic MVT data** (CI 43359838414).
- Phase 4: IN PROGRESS; a **small true OSM Hanoi road-only pilot** (bbox 105.849–105.858 E, 21.031–21.037 N) has been fetched, pinned and built into 21 PMTiles vector tiles, 354 highway ways, SHA-256 ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d. Offline road geometry visibly rendered on Android emulator screenshot in run 43370534933; the run's new image-check step failed only because Pillow was not installed. Retest 43371354137 with standard-library PNG checker pending. No full Hanoi/34-province map, legal boundaries or routing graph. Previous import foundation: LocalPackStore, PmtilesHeader and Android SAF import UI written, unit tested, APK assembled. The emulator test observed actual archive + metadata files; byte-level archive/index regression verified in CI 43362607683 and interruption-recovery emulator regression verified in CI 43364225600, both PASS. **Actual Android system DocumentsUI file-picker import** verified in CI 43367761361 (all jobs PASS, user selection of local synthetic .pmtiles and independent SHA-256 readback). Real regional map packs remain pending.
- Phase 5-18: NOT STARTED, apart from preparatory data-policy validators and simple location code.

## Current code / workflow
- Source code: `app/src/main/java/vn/navitaximap/`.
- Pack import: `data/LocalPackStore.kt`, `data/PmtilesHeader.kt`.
- Unit tests: `app/src/test/java/vn/navitaximap/data/`.
- Emulator CI script: `ci/smoke-emulator.sh`.
- Latest verified CI: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332 (tested code 43a70acc220235fd1f6f13fdf5332eb37f286c59, all jobs PASS: Android build/23 JVM tests, pinned real HTTPS transfer, SAF, crash recovery, real OSM offline map and catalog UI).

## Not demonstrated
No complete province/city PMTiles packs (only a tiny real OSM sample), PMTiles download manager, route graph, POI/search, navigation, traffic, speed signs, terrain/3D or approved legal maritime overlays.
Not installed or tested on the owner's Xiaomi. No production-ready release APK.

## Next verification
Real OSM pilot source snapshot: workflow https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43369521518 (17,828 nodes, 354 road ways); deterministic build: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43370147765 (21 tiles, 53,956 bytes). Full regression CI https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43371354137 **ALL JOBS PASS**: 58,015 real OSM local-road-colored screenshot pixels in central map viewport, source/license warning exposed, no FATAL EXCEPTION. See HANOI_OSM_PILOT.md.

## Phase 4: versioned preview pack catalog, verified 2026-11-13
- **Implemented, limited pilot only:** `PilotCatalog`, `CatalogPackStore`, `PinnedHttpsPackDownloader`. One user-initiated download option, immutable 40-hex GitHub source commit URL, pin to exact 53,956 bytes and SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d`.
- Catalog pack installation is private, streamed and staged; archive is validated before atomically promoting index. Failed updates preserve prior installed version in tested scenarios; recovery removes generated orphan files. This is NOT a cross-process/durable database transaction.
- Local catalog UI supports checking installed version/hash, reinstalling from APK, confirmed delete and explicitly confirmed HTTPS transfer with cancellation hook.
- CI [43374393332](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332) ALL JOBS PASS. Emulator online received actual file through `PinnedHttpsPackDownloader` (origin=PINNED_HTTPS), re-read 53,956 bytes SHA exact, then airplane mode displayed roads and 58,015 colored pixels. Native DocumentsUI picker, simulated interrupted imports and provenance dialog PASS.
- **sourceApproval=UNREVIEWED_VISUAL_PREVIEW; navigationApproved=false**. Full 34-region catalog, signed external manifests, pause/resume downloads, usable navigation and physical Xiaomi QA remain NOT STARTED/IN PROGRESS as appropriate.
