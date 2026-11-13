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

## 2026-11-13 CI checkpoint
- Run 43389395898 **ALL SUCCESS**, including both jobs, resolves two previous flaky MapLibre screenshot failures without weakening pixel checks; synthetic 39,985 and original small Hanoi roads 58,015 pixels.
- Run 43391332272: initial build/JUnit SUCCESS; four independent emulator shards assigned 34/34 original provincial PMTiles, not yet complete. Other CI on new SHA includes 43391332249 normal Android and 43391332168 Bắc Ninh; still pending at writing.

## 2026-11-13 15:27 CI trail
- 43391332249 standard Android all SUCCESS; 43391332168 genuine Bắc Ninh all SUCCESS.
- 43391332272 34-shard coverage overall FAIL: 01/04/08 each individually proved via SHA, SAF, airplane-mode screenshot; 11 blocked by Pixel Launcher ANR; shell loop terminated due adb reading stdin.
- 43392189567 all four coverage emulator jobs FAIL at shell parse error line 65 caused by corrupted template string during fix; no region tests. Corrected e015ebf2 plus `bash -n` gate.
- 43393047973 build/JUnit/static-syntax SUCCESS; 4 emulator jobs RUNNING. Count only province-specific PASS evidence, not total before final logs.

## 2026-11-13 15:40 CI exact outcomes
- 43393047973 overall **FAIL**, 34/34 original provincial archives checked, 28 PASS screenshot+SAF+SHA, six 0-road-pixel images: codes 31/46/48/56/91/96. All are NOT navigation approved.
- 43395054211 34-province regression **PENDING** after adding grounded preview cameras ad7a8dde; related runs 43395054123 (Android debug), 43395054234 (Bắc Ninh), 43395054268 (Hà Nội + HCM) PENDING. No claim of all-green while still executing.


## 2026-11-13 20:55 new CI checkpoint
- 43395054211 34-province workflow CANCELLED due shard0 launcher hang; shards 1,2,3 25/25 SUCCESS, 4 coastal/large-area previous 0-pixel regions now PASS. Combined older shard0 valid proofs gives 32/34 distinct province images. 31 Hai Phong and 48 Da Nang still open.
- 43396020034 Android debug + emulator SUCCESS. 43396019905 HN+HCM FAIL because Pixel Launcher ANR after correct Android Downloads SHA for Hanoi.
- 43438393919 targeted Hai Phong/Da Nang CI PENDING, 43438780443 nationwide 556MB real archive CI PENDING. New national workflow validates QA shell/Python syntax and Kotlin unit tests, consumes original source artifact, streaming Android readback, pixel and attribution, retaining only evidence instead of 556MB copies.


## 2026-11-13 targeted province closure
- Run 43438393919 **SUCCESS**, 31 Hai Phong and 48 Da Nang both exact SHA+SAF+offline real-road screenshots; 102,298 and 217,958 pixels. Result 2/2. All 34 provincial Shortbread PMTiles now have individually supported Android visual proofs across runs.
- Full original 34-shard run 43395054211 remained CANCELLED, three shards 25/25 SUCCESS; standalone targeted run closed the two missing provinces.
- National 43438780443 and further Android regression in progress at checkpoint.


## Current 2026-11-13 national CI runs
- 43438780443 **FAIL** due Android Pixel Launcher ANR before selecting nationwide SAF. It DID pass checksum of 556,229,998-byte source and Android shared-storage copy. No stream readback / rendered map proof yet.
- 43439851626 **RUNNING** after adding one ANR-only restart in aa2f975. 43438780281 HN+HCM SUCCESS; 43438780338 Bắc Ninh special run FAILED on launcher ANR; normal Android 43438780381 build SUCCESS (emulator status pending at checkpoint).


## 2026-11-13 final CI evidence by scope
- 43438780272 all 34 genuine provincial Android offline pack QA **SUCCESS** (5 jobs: compile and four shards 9/9, 9/9, 8/8, 8/8, no failures).
- 43438780281 Hà Nội/TP.HCM standalone **SUCCESS**; 43438780381 standard Android **SUCCESS**.
- 43438780443 national 556MB **FAIL before SAF** on Launcher ANR, 43439851626 national **FAIL during SAF** with `Not a PMTiles v3 archive` after correct Android Download full SHA. Cannot call national ready.
- 43441211821 national QA with debug selected-stream magic diagnostics is **PENDING**. Do not report it green without `SAF_STREAM_SHA256_OK` + road pixels.


## 2026-11-13 authoritative all 35 archive evidence
- 43438780272 **SUCCESS** 34/34 provincial original archives; four shards 9,9,8,8 without failure.
- 43441211821 **SUCCESS** full 556,229,998-byte national archive actual SAF import with streamed SHA, airplane mode 157,112 road-colored pixels; completes 35/35 unique archive visual QA across two workflows.
- 43441211805 Android debug **SUCCESS** both build+emulator; 43441211810 Bắc Ninh **SUCCESS**; 43441211781 Hà Nội+HCM **SUCCESS**.
- 43441211834 **FAIL** 1/34 in duplicate run due instrumentation XML being selected by DocumentsUI instead of 08 Tuyên Quang, evidenced by app's selected-stream debug log.
- Code commit 1ce22829 fixes test pollution and adds early .pmtiles guard; reruns 43454411956 normal, 43454411510 national, 43454411678 34 original provinces, 43454411692 Bắc Ninh, 43454411531 HN/HCM all PENDING at checkpoint.


## 2026-11-13 after test instrumentation collision correction
- Android full 43454411956 SUCCESS both build and emulator; national 556MB QA 43454411510 SUCCESS (streamed SHA after real SAF, 157,112 road pixels); Bac Ninh 43454411692 SUCCESS.
- HN/HCM 43454411531 FAIL because emulator Pixel Launcher ANR blocked app menu before SAF (source/downloaded SHA correct). New multi CI 43455374999 tests one ANR-only retry in commit 720efe42 and shell/Python lint in e05b1ed; result PENDING.
- 34-region full coverage 43454411678 build SUCCESS, 4 shards IN PROGRESS. This does not revoke earlier all-green 43438780272 for 34/34.


## 2026-11-13 release-safe range-stage feature QA
- Prior run 43454411678 34/34 original provinces all SUCCESS; 43454411510 national 556MB SUCCESS; 43455502132 HN/HCM SUCCESS; 43455502057 Android build+emulator SUCCESS.
- 389d71bf adds dormant HTTPS Range staging with JVM tests; 43458014933 Android debug compile/emulator in progress at note. No claim of operational downloads or approved hosting.


## 2026-11-13 Range staging QA and emulator regression details
- 43458014933: build/JUnit SUCCESS, standard Android emulator FAILURE at UIAutomator offline status check. Artifact 14055795953 contains 40,721 synthetic road pixels and screenshot/status under Android system `Pixel Launcher isn't responding` modal; XML contained only launcher dialog. Pixel check PASSED, UI text assertion failed appropriately.
- Code 3b141de CI-only bounded system overlay handling + bash -n check; new 43459478560 PENDING. Does not relax offline pixel or UI status acceptance; source Kotlin Range unchanged.


## 2026-11-13 23:58 build checkpoint
- 43459478560 Android-debug + emulator two jobs **SUCCESS** at code 3b141de, strict PMTiles screenshot/status and real OSM pilot passed.
- 43468092472 following a0a0be16 origin-approval hardening + 6 JUnit range tests **PENDING** at writing; not yet declared green. Catalog download gate still disabled and source list empty.
