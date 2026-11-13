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
- Phase 4: IN PROGRESS; LocalPackStore, PmtilesHeader and Android SAF import UI written, unit tested, APK assembled. The emulator test observed actual archive + metadata files; byte-level archive/index regression verified in CI 43362607683 and interruption-recovery emulator regression verified in CI 43364225600, both PASS. **Actual Android system DocumentsUI file-picker import** verified in CI 43367761361 (all jobs PASS, user selection of local synthetic .pmtiles and independent SHA-256 readback). Real regional map packs remain pending.
- Phase 5-18: NOT STARTED, apart from preparatory data-policy validators and simple location code.

## Current code / workflow
- Source code: `app/src/main/java/vn/navitaximap/`.
- Pack import: `data/LocalPackStore.kt`, `data/PmtilesHeader.kt`.
- Unit tests: `app/src/test/java/vn/navitaximap/data/`.
- Emulator CI script: `ci/smoke-emulator.sh`.
- Latest verified CI: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43367761361 (code commit a3f76d1636a30acca6e56a393fbf60264f3fb7f6, all jobs PASS including system picker).

## Not demonstrated
No real licensed Vietnamese province packs, PMTiles download manager, route graph, POI/search, navigation, traffic, speed signs, terrain/3D or approved legal maritime overlays.
Not installed or tested on the owner's Xiaomi. No production-ready release APK.
