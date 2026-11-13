# Phase 4 pinned HTTPS visual pack: verified Android end-to-end evidence

Date: 2026-11-13 (Asia/Bangkok).
Repo: ThanhTaxiXanh/NaviTaxiMap.
Working branch: develop/phase0-android-maplibre.
Verified code commit: \`43a70acc220235fd1f6f13fdf5332eb37f286c59\`.
CI: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332

## Result: ALL JOBS PASS

- Android Gradle: \`:app:testDebugUnitTest\` (23 total JVM tests, including 12 new catalog tests), \`:app:assembleDebug\`, PMTiles generator regression and policy-validator tests: PASS.
- Android emulator API 34: application online cold start and map UI smoke: PASS.
- Debug-only CI launcher requested one real pinned HTTPS transfer while emulator was ONLINE. Host is \`raw.githubusercontent.com\`, exact immutable commit URL listed below. Transfer did not start silently in normal/release builds.
- **Downloaded 53,956 bytes** via Android \`PinnedHttpsPackDownloader\`; read them back directly from app-private storage with \`run-as\`. Independent comparison confirms byte-for-byte identity with checked-in asset; SHA256 matches \`ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d\`.
- Read back metadata from \`files/catalog-packs/hanoi-roads-visual.properties\`: \`id=hanoi-roads-visual\`, \`version=1\`, \`size=53956\`, \`origin=PINNED_HTTPS\`, \`navigationApproved=false\`, \`sourceApproval=UNREVIEWED_VISUAL_PREVIEW\`, \`license=ODbL-1.0\`, and exact source OSM SHA.
- Emulator switched to AIRPLANE MODE, confirmed local real-OSM road-preview drawing via 58,015 blue local-road pixels in central viewport and UI ODbL/no-navigation warning.
- Real Android DocumentsUI file-picker import: PASS. Simulated interrupted import recovery and original imported pack unchanged: PASS.
- The catalog UI opened on the emulator. UIAutomator XML/screenshot asserts displayed \`Đã cài v1\`, \`ODbL\`, \`SHA-256\` and \`CHƯA DUYỆT\`: PASS.
- Artifacts: \`NaviTaxiMap-debug\` and \`NaviTaxiMap-emulator-evidence\` from run 43374393332.
- Evidence ZIP contains \`pinned-downloaded.pmtiles\`, \`pinned-downloaded.properties\`, \`catalog-installed.pmtiles\`, \`catalog-installed.properties\`, \`saf-catalog-dialog.xml\`, \`saf-catalog-dialog.png\`, \`osm-pilot-screenshot.png\`, screenshots/logs from previous regressions and the actual source bytes.
- Debug APK: 44,725,536 bytes, ZIP integrity PASS, SHA256 \`5428dcc5ae920b7f3a183de27883f626703a116757136efba9b5c6b5782911e0\`.

## Pinned source and limits

- Pack URL: \`https://raw.githubusercontent.com/ThanhTaxiXanh/NaviTaxiMap/554cfe62013c80146842aea85cb3625e583f2ced/app/src/main/assets/hanoi_real_roads.pmtiles\`.
- Source is OSM API small bbox \`105.849,21.031,105.858,21.037\`, snapshot at 2026-11-13 05:04:02 UTC, raw XML SHA256 \`db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697\`. 354 real OSM highway ways, 21 MVT tiles at zoom 14–17, **not** citywide geography.
- Attribution: **© OpenStreetMap contributors**, Open Database License 1.0, https://www.openstreetmap.org/copyright.
- SHA pinning proves identity to the archived input, not correctness, legal approval or usefulness for taxi routes.
- This proof covers one explicitly selected pinned visual sample. It does **not** prove resumable 34-province downloading, signed remote catalog/manifests, real road routing, traffic conditions, road restrictions, legal boundaries or a full ODbL distribution-compliance audit.
- No physical Xiaomi testing, app signing, release, PR, merge or touch of main. Do not use for actual driving.

## Earlier test issue

- CI 43373898829 and 43374048067 built successfully but one JVM test confused the legitimate Android \`src/main/assets\` path with a forbidden GitHub \`main\` branch ref. Fixed check of \`/NaviTaxiMap/main/\` in commit \`510c12c\`.
- CI 43374393332 is the full green end-to-end verification and includes real network transfer, not just a mocked InputStream.
