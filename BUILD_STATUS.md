# BUILD_STATUS
2026-11-13 | working branch develop/phase0-android-maplibre

## Verified baseline
- Android AGP 8.7.3, Kotlin 2.2.10, JDK 17, Gradle 8.9, compile SDK 35.
- MapLibre Native Android OpenGL ES 13.5.4.
- Run [43359838414](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43359838414): Android debug build + unit suites + emulator airplane-mode synthetic PMTiles rendering PASS.
- Run [43361851396](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43361851396): Gradle build and unit tests PASS; emulator installed/opened APK, displayed offline geometry and created imported PMTiles + metadata files. Smoke job FAIL because logcat-only success assertion did not see a message.
- New regression script directly reads app-private imported archive bytes and metadata, checks SHA-256 and sourceApproval=UNVERIFIED, rather than relying on logcat.

## Latest pipeline
- Code commit: `62a7cc55bee4b3a44f905fb4360be9dbb5f9d48e`
- Run [43362607683](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43362607683): ALL JOBS PASS: Gradle debug + JUnit, Python PMTiles 3/3, geographic policy 4/4, Android emulator online launch, offline rendering in airplane mode, and importer readback SHA-256 match. Evidence: 1268 bytes, SHA-256 860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407, index sourceApproval=UNVERIFIED.
- Latest APK extracted from run 43362607683, ZIP/APK integrity checked. APK SHA-256 d2cca8abe753af410d44b9fe99ffa342f524edc714fdda9d66c6f2c47737dee4. NOT a production release.

## Blocked
Physical Xiaomi debugging and live GPS; actual Android system-picker interaction; real Vietnam regional tiles, download lifecycle and licensed styles; production signing and navigation QA.

## Latest phase 4 crash-recovery QA (2026-11-13)
- Commit tested: `f7515b29f4b9057eef3d82b1dbaacfdf0d1f5ff0`.
- [GitHub Actions 43364225600](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43364225600): **ALL JOBS PASS**.
- Gradle `:app:testDebugUnitTest`, `:app:assembleDebug`; PMTiles fixture Python 3/3; Vietnam approval policy Python 4/4.
- Emulator API 34: online cold-start, synthetic offline tile screenshot/pixel check in airplane mode, PMTiles import and SHA-256 readback PASS.
- Injected four app-private incomplete files (two staging, orphan PMTiles, orphan index), force-stopped app, relaunched it; recovery discarded incomplete files while keeping prior committed pair byte-identical. `imported-pack.pmtiles` and `recovered-pack.pmtiles` both SHA-256 `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407`.
- Debug APK 44,638,931 bytes, ZIP integrity PASS, SHA-256 `8b0a1aa4c849786809a3ec58b8089ebde2f54870c0997b05243d7423a3db4cf0`.
- Evidence in workflow artifact `NaviTaxiMap-emulator-evidence`.
- Not a production APK. Physical Xiaomi, real road PMTiles, full SAF picker and cross-process recovery remain unverified.

## Latest Phase 4: Real Android SAF picker smoke, 2026-11-13
- Tested code commit: `a3f76d1636a30acca6e56a393fbf60264f3fb7f6`.
- [GitHub Actions run 43367761361](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43367761361): **ALL JOBS PASS**.
- Android :app:testDebugUnitTest, :app:assembleDebug, fixture Python 3/3 and geographic-policy Python 4/4 PASS.
- Emulator API 34 in airplane mode: online app startup, synthetic PMTiles offline tile render and screenshot assertions PASS; synthetic import/readback, simulated interruption cleanup and preserved archive PASS.
- **Real Android DocumentsUI ACTION_OPEN_DOCUMENT selected a PMTiles file from Recent**. `onActivityResult` passed its URI to `LocalPackStore.importPack`, whose storage was read back and checked independently.
- UI selected file: `navitaxi-saf-test.pmtiles`; imported bytes: 1,268; SHA-256 `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407` matching source.
- Persisted metadata includes name, size, hash and `sourceApproval=UNVERIFIED` and was independently inspected; no geospatial source approved.
- APK 44,638,931 bytes; sha256 `bd70084157c20928df57e62e81273e091f24be80b81d9f5c1192e819d16ece50`; ZIP integrity PASS.
- Artifact `NaviTaxiMap-emulator-evidence` contains `saf-success.png`, `saf-result.xml`, imported archive and metadata plus prior smoke screenshots.
- LIMITS: still no hardware Xiaomi/real road map/route graph, no production signing, no true source provenance approval; actual picker test was on Android 34 emulator.

## Hanoi real OSM road-only preview, 2026-11-13
- Source workflow [43369521518](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43369521518): PASS. Original official OpenStreetMap small-bbox XML 6,321,243 bytes, SHA-256 `db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697`, extracted 17,828 nodes and 354 highway ways.
- Deterministic PMTiles conversion [43370147765](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43370147765): PASS. 21 MVT tiles, zooms 14–17, 53,956 bytes; three road source layers, source checksum embedded; PMTiles SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d`.
- Generator + asset byte-for-byte regeneration: Android build job in [43370534933](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43370534933) PASS.
- **Android screenshot from 43370534933 visibly contains real OSM road geometry** with more than 66,000 local-road and 32,000 major-road color pixels in the central viewport (independently measured from saved image). App runs offline in airplane mode, with attribution and non-navigation warning. Emulator job FAILED because new screenshot assertion imported missing Pillow (`ModuleNotFoundError: PIL`), not because the app failed.
- Fixed screenshot assertion to reuse stdlib PNG decoder in `08afe5a`. Rerun [43371354137](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43371354137) **ALL JOBS PASS**. Android emulator airplane mode screenshot shows real OSM lines; the stdlib pixel checker independently counted 58,015 matching local-road pixels. UI has OpenStreetMap attribution and KHÔNG dẫn đường warning. Full emulator logs show no fatal exception.
- Debug APK extracted from prior code-identical app build (43370534933), ZIP integrity PASS; 44,709,152 bytes, SHA-256 `1875279937b61b82e111a5cd0b6e1443e27bc971c3941f22f70d988d4bc04077`. No public release, no Xiaomi hardware validation.

- Latest PASS-run debug APK 44,709,152 bytes, SHA-256 `7a91b814c4b6cf1a32b12eb56cf4a863b7b3e7e85bf70afb9c83e1245abd6c36`, ZIP integrity checked. Artifact `NaviTaxiMap-debug`; evidence `NaviTaxiMap-emulator-evidence`, both from CI 43371354137.
- See `HANOI_OSM_PILOT_EVIDENCE.md` for independent source/citywide-scope limitations.

## Latest Phase 4 catalog + real HTTPS transfer (2026-11-13)
- Tested code SHA: `43a70acc220235fd1f6f13fdf5332eb37f286c59`.
- [GitHub Actions 43374393332](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332) **ALL JOBS PASS**: Android `:app:testDebugUnitTest`, `:app:assembleDebug`, PMTiles generator tests, Vietnam policy tests, emulator online + airplane-mode offline tests.
- JVM: 23 tests (12 new `CatalogPackStoreTest`, plus existing import/verifier tests). Covers pinned URL allowlist/version, checksum, rollback of failed update, zero stale files, cancellation, storage pressure, deletion and tamper rejection.
- Actual Android HTTPS GET: 53,956-byte Hanoi pilot pack fetched from immutable GitHub commit and validated SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d` before activation. App-private `catalog-packs/hanoi-roads-visual.properties` confirms `origin=PINNED_HTTPS`, `version=1`, `navigationApproved=false`, `sourceApproval=UNREVIEWED_VISUAL_PREVIEW` and ODbL 1.0.
- After airplane mode, road-only OSM PMTiles rendered successfully; image checker detected 58,015 local-road pixels. SAF file picker/import, interruption cleanup and catalog UI/provenance checks all PASS.
- Debug APK 44,725,536 bytes (ZIP integrity PASS), SHA-256 `5428dcc5ae920b7f3a183de27883f626703a116757136efba9b5c6b5782911e0`. Downloaded from run's `NaviTaxiMap-debug` artifact, NOT a signed/production APK.
- Emulator evidence: `NaviTaxiMap-emulator-evidence` artifact of 43374393332, including downloaded on-device bytes and properties, catalog-dialog screenshot/XML, offline OSM screenshot, SAF picker and corruption/recovery output.
- NOT tested: full regional catalogs, download pause/resume over actual poor connectivity, physically installed Xiaomi, production provenance approval, route graph, ODbL public distribution audit.

## Large Vietnam data build (not an Android APK change), 2026-11-13
- Source snapshot and provinces: Geofabrik Shortbread `vietnam-shortbread-1.0.mbtiles` 686,698,496 bytes, SHA256 `492402e2ebc49516ba72ff90bb0212eba29fab3cc2a9addcb95bb39d6b1089d5`. PBF OSM audit 332,558,874 bytes, 33 admin4 multipolygons; separately OSM relation 1887959 restored the 34th shape for Khánh Hòa.
- Python+Shapely 2.0.7 region tile-footprint splitter, pinned PMTiles CLI v1.31.2 generated 35 valid v3 vector PMTiles. National **556,229,998 B** (567,706 tiles), provinces **287,504,805 B** total, province zoom 8–14, national zoom 0–14.
- [CI 43378133931](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931): **ALL JOBS PASS**.
- [Independent CI 43379440781](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43379440781): **ALL JOBS PASS**; opened and rehashed all 35 files, checked PMTiles v3/MVT headers, reported identical source and regional manifest metadata.
- Deliverables as two artifacts: `NaviTaxiMap-34-Provinces-OSM-UNREVIEWED` (about 287.5 MB) and `NaviTaxiMap-Vietnam-Whole-Country-OSM-UNREVIEWED` (about 556.2 MB), plus separate small `NaviTaxiMap-Vietnam35-Verified-Manifest-SHA256`.
- **NO 35-pack Android runtime/emulator proof yet**; all 35 are GIS DATA output only. Need Shortbread-compatible style and catalog download integration. Status UNREVIEWED, no navigation approval, no official maritime/admin boundaries.


## 2026-11-13 new Android Shortbread code
- Existing Android CI 43379440694 SUCCESS; Geofabrik real 35-pack generation 43378133931 SUCCESS; independent checksum 43379440781 SUCCESS.
- New feature commit 4a55a873, Android run 43385141310 and real Shortbread emulator run 43385141410: IN PROGRESS at checkpoint. Do NOT report successful new map rendering without pixel evidence.
- No main edits, PR, merge, production APK signing or release.


## Actual Shortbread Android proof
- Dedicated real-pack emulator run https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410: SUCCESS on feature commit 4a55a873.
- SHA verified for 24-bac-ninh.pmtiles (11,049,420 bytes, SHA 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f).
- Android DocumentsUI actual import and on-device bytes SHA PASS; airplane-mode style callback/visible attribution/warning and 266,196 road-color screenshot pixels PASS.
- Evidence: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410/artifacts/14017389861. Standard Android full regression 43385141310 still pending on this update.


## Both development-branch CI workflows SUCCESS
- Android debug run https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141310: SUCCESS (build, JUnit, Python, emulator).
- Real Bắc Ninh Shortbread run https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410: SUCCESS (SHA, real SAF import, airplane mode, pixel screenshot).
- These test source commit 4a55a873. Later documentation-only commits use [skip ci]; do not misstate them as separate tested Android binaries.


## 2026-11-13 new CI evidence
- 43387058111 Android debug build/unit tests: **build job SUCCESS**; full standard emulator regression job was still underway at this checkpoint.
- 43387058322 Hà Nội + Hồ Chí Minh real Shortbread Android: **SUCCESS**; 01 file 17,562,631 bytes / 157,112 road pixels, 79 file 18,217,716 bytes / 183,085 road pixels, SHA and SAF PASS. Artifact 14019316787.
- 43387058086 Bắc Ninh retest: **FAIL** at `adb push` into not-yet-writable emulator /sdcard/Download, unrelated to map pixel callback; do not count as successful regression. Fix a3e53366 and rerun 43387711796; multi recheck 43387711798 and Android full 43387711858 pending.


## Corrected CI outcomes
- 43387711796 (one real Bắc Ninh): SUCCESS, 266,196 road-color pixels with airplane mode; 11,049,420 byte SHA pinned.
- 43387711798 (two real regions): SUCCESS, Hà Nội 157,112 and TP.HCM 183,085 road-color pixels; correct size/hash/SAF.
- 43387711858 Android debug: compile/JVM tests SUCCESS, standard emulator FAILED during old 54KB Hanoi screenshot (pixel 0; strict check retained). Existing other stages up through synthetic PMTiles and import/recovery PASS.
- Commit 59c2192 adds bounded screenshot retries (without weakening assertions) for existing Hanoi pilot; CI 43388632601 triggered; PENDING.


## Latest 2026-11-13 status
- 43388632601: build/JVM SUCCESS but emulator-smoke FAILURE (synthetic PMTiles 0 colored pixels on one screenshot). Total result FAILURE, not PASS.
- Commit fddd00b5 adjusts test synchronization only (no lowered pixel thresholds) and triggered 43389395898, currently pending. Separate genuine Shortbread jobs 43387711796 and 43387711798 remained full SUCCESS.
