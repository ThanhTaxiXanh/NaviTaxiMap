# Real OSM Hanoi road-only offline PMTiles: verified Android evidence

Date: 2026-11-13 (Asia/Bangkok).
Repository: ThanhTaxiXanh/NaviTaxiMap.
Branch: develop/phase0-android-maplibre.
Tested **code commit**: `08afe5a16b316f363750fc21b006cf82f39817de`.
GitHub Actions: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43371354137

## Result: ALL JOBS PASS
- Android `:app:testDebugUnitTest`, `:app:assembleDebug`: PASS.
- Synthetic PMTiles unit tests 3/3, sovereignty-source validation 4/4, real OSM generator unit tests 3/3 and deterministic asset regeneration: PASS.
- Android API 34 emulator: debug APK install + cold launch online: PASS.
- Emulator airplane mode: synthetic PMTiles renderer screenshot pixel regression: PASS.
- Android DocumentsUI ACTION_OPEN_DOCUMENT system picker: selects synthetic PMTiles, confirms success and original SHA-256: PASS.
- Simulated import crash/restart with four leftover app-private files: cleanup succeeds; previously committed archive unchanged: PASS.
- **Small real OSM Hanoi road-only PMTiles**: emulator airplane mode cold-launch, local MapLibre vector source, colored road geometry visible: **PASS**.
- Real OSM pixel regression with Python stdlib PNG decoder counted **58,015 blue local-road pixels** in central map viewport. No online street tiles were involved.
- App status UI includes **© OpenStreetMap contributors (ODbL)** and **KHÔNG dẫn đường**; tested against Android UI XML.
- Saved emulator log has no AndroidRuntime FATAL EXCEPTION in this test.
- Evidence artifact `NaviTaxiMap-emulator-evidence` of run 43371354137 includes `osm-pilot-screenshot.png`, `osm-pilot-ui.xml`, `osm-pilot-full-logcat.txt`, online/synthetic screenshots, import, picker and crash-recovery files.

## Data source and integrity
- Source: official OSM API 0.6, one-time bbox `105.849,21.031,105.858,21.037`; snapshot at 2026-11-13 05:04:02 UTC.
- Raw XML: 6,321,243 bytes; SHA-256 `db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697`.
- Source contains 17,828 nodes and 354 highway ways; normalized source 1,691 point references.
- Committed frozen source: `tools/osm_hanoi_pilot/snapshot/roads.json.gz`, renderer: `app/src/main/assets/hanoi_real_roads.pmtiles`.
- Deterministic vector conversion: 21 tiles, zoom 14–17, 53,956 bytes, SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d`.
- Original raw source held in one-time workflow [43369521518](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43369521518) as `Hanoi-OSM-pilot-source-UNREVIEWED`.
- Provenance/build: [43370147765](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43370147765), `Hanoi-real-OSM-road-preview-UNREVIEWED`.
- License and attribution: **© OpenStreetMap contributors**, ODbL 1.0, https://www.openstreetmap.org/copyright.
- APK of green CI 43371354137: 44,709,152 bytes, SHA-256 `7a91b814c4b6cf1a32b12eb56cf4a863b7b3e7e85bf70afb9c83e1245abd6c36`, ZIP integrity checked. DEBUG only.

## QA incident resolved
- First run 43370534933 rendered roads and saved screenshot but new image test failed on missing Pillow (`ModuleNotFoundError: No module named PIL`). Saved screenshot was independently inspected with 66,575 blue/local and 32,027 orange/major road-colored pixels.
- Fix `08afe5a` reuses existing standard-library PNG decoder instead of Pillow. New CI 43371354137 ALL JOBS PASS.

## Limitations
This is a small **real OSM geometry sample**, not a complete map of Hà Nội, a certified government boundary dataset, a route graph or an approved product. OSM highway tags do not guarantee traffic rules or drivable status. No comprehensive roads, POI/search, turn restrictions, speed limits, voice navigation, traffic feed or 34-province offline packs are implemented. The ODbL public-distribution compliance audit is still OPEN. No physical Xiaomi, battery/GPS/rotation, abrupt power loss or production signing test. Do NOT use for driving/navigation.
