# PROJECT_STATE
As of: 2026-11-13 (Asia/Bangkok)
Repo: ThanhTaxiXanh/NaviTaxiMap
Working branch: develop/phase0-android-maplibre
Protected main: DO NOT merge, create PR, or push until explicitly authorized.

## Phase status, based on real evidence
- PHASE 0: IN PROGRESS (GitHub/CI verified; Xiaomi Termux/Shizuku environment not connected).
- PHASE 1: IN PROGRESS (Kotlin app/MapView/UI built and launches Android emulator).
- PHASE 2: IN PROGRESS (MapLibre + OpenFreeMap street style rendered on emulator, but no production basemap).
- PHASE 3: IN PROGRESS (synthetic PMTiles v3 fixture + native file protocol integrated; emulator smoke and screenshot verification pending on latest source).
- PHASE 4-18: NOT STARTED, except early data-policy validation tool.
- Real-device QA: BLOCKED, no remote Android/ADB access.
- Release APK: NOT STARTED.

## Verified baseline
- GitHub run 43353698195: PASS Kotlin build, unit tests, Android emulator install/launch.
- Screenshot of that run: OSM street lines shown online in emulator.
- Original ZIP unit tests: 4/4 PASS. Synthetic fixture validation: 3/3 PASS in local Python (must re-run on CI).

## Not done
Offline Vietnam PMTiles, POI, route graph, navigation, traffic, speed signs,
34 provinces, 3D, approved geopolitical layers and production data licensing.

## Next iteration
Read CI log and offline screenshot. If native PMTiles fails, diagnose and fix until
actual tile lines render with connectivity disabled. Keep all fixes on working branch.
