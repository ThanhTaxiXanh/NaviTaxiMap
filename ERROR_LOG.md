# ERROR_LOG
## Resolved
- Android SDK setup tried an obsolete tools package, upgraded setup-android@v4.
- Kotlin compiler 2.0 metadata mismatch with MapLibre 13.5.4, upgraded Kotlin to 2.2.10.
- Original world-demo tiles had no street detail; switched to OpenFreeMap prototype style.
- First synthetic offline PMTiles screenshot blank; fixed five-tile fixture and added screenshot pixel assertion (CI 43359838414 PASS).

## New incident
- CI 43361851396: import smoke returned FAILURE on absence of `DEBUG_IMPORT_PASS` filtered logcat message. On the same emulator, both imported PMTiles and metadata files were present, and online/offline screens passed. This was a verification-signaling failure, not proof that archive import failed.
- Fix commits: `09c6e11` and `62a7cc5`. Script now reads and checks exact import bytes and metadata with SHA-256. CI 43362607683 PASS; logcat false-negative resolved.

## Open
- Byte-level emulator import smoke verified in CI 43362607683; continue real file-picker and recovery tests.
- System file-picker interaction now verified in Android API 34 emulator (CI 43367761361). Source-provided signed/approved SHA-256 manifest workflow remains OPEN.
- Need recovery for orphaned data between two atomic renames, resume/pause downloads, storage-pressure handling.
- Xiaomi device is not connected, no physical QA.
- No approved regional basemap, POI, offline routing, verified traffic/speed/legal boundary data.

## Phase 4 reliability QA
- No new CI blocker: 43364225600 all jobs PASS including synthetic import and simulated interrupted-import cleanup.
- Risk still open: fsync of parent directory and cross-process locking not implemented. Tests simulate file leftovers and normal app restart, not abrupt hardware power loss.
- Real OSM geographic/attribution validation and Xiaomi physical device QA still blocked.

## DocumentsUI automation incident: resolved
- CI 43367038303: full build, prior offline demo/import/recovery PASS; new picker smoke FAILED because the test selected the `preview_icon` control whose content description includes the filename, rather than the real `android:id/title` text node. Evidence preserved in `NaviTaxiMap-emulator-evidence`.
- Fixed in commit `a3f76d1`: require exact file-title text and resource ID. CI 43367761361 **ALL JOBS PASS**, real system-picker flow verified with UI success dialog and imported bytes/hash.

## Real OSM pilot screenshot image-tooling failure (RESOLVED)
- Run 43370534933: Android build/unit/Python data tests PASS; emulator online launch, synthetic offline map, SAF import and crash recovery PASS. Real road screenshot saved; actual geometry visibly rendered in airplane mode and status attribution displayed.
- First new image regression script imported Pillow, which was not installed on the emulator runner: `ModuleNotFoundError: No module named 'PIL'`. This is a **test dependency issue**, not application crash or missing source geometry.
- Fix commit `08afe5a` imports the existing Python standard-library PNG decoder from `ci/check-offline-screenshot.py`; run 43371354137 ALL JOBS PASS; 58,015 real road color pixels confirmed and attribution/warning visible.

## Catalog URL unit-test false positive (resolved)
- CI 43373898829 / 43374048067: 22 of 23 JVM tests PASS but an assertion erroneously rejected `/main/` appearing inside `app/src/main/assets`, even though source URL was correctly pinned to a 40-digit commit. Not a broken downloader or SHA mismatch.
- Fixed test to inspect only the repository reference segment `/NaviTaxiMap/main/`, commit `510c12c`. CI 43374393332 all jobs PASS including real pinned HTTPS GET and offline renderer.
- Current constraints: no signed remote manifest, no cross-process lock/directory fsync, no real pause/resume/retry integration, no end-to-end source legal certification.

## Vietnam 35-pack production incidents and status
- First new build CI 43377995854 FAILED while attempting to compile Shapely 2.0.7 on runner default Python 3.14 (`geos_c.h` not found). No map packages published. Fixed in 7b7c57a by pinning Python 3.12 with binary wheel; final build 43378133931 ALL PASS.
- OSM Geofabrik admin4 boundary PBF 332,558,874 B exported 33 polygons, missing Khánh Hòa relation area. Retrieved relation 1887959 with full member geometry through official OSM API; independently SHA256 audited, resulting 34/34 shapes. No fabricated fallback shape.
- **Open QA risks:** OSM admin4 polygons are not legal boundaries; large maritime components in some provinces, whole-vector-tile rather than line-geometry clipping, Geofabrik Shortbread extract may omit disputed islands; style, distribution license review, 35-pack Android rendering and offline routing not yet done. No false claim of legally certified sovereign map.


## 2026-11-13 unresolved Shortbread risks
- The new real Bac Ninh emulator screenshot evidence is not yet available; static style validation does not prove visual compatibility.
- GitHub Actions artifact data expires on 2026-11-27. In-app downloads blocked without durable approved distribution hosting. SHA matching is not ODbL/legal-border/navigation approval.


## 2026-11-13 Shortbread test result
- Resolved for limited real Bắc Ninh preview: static style compatibility uncertainty; CI 43385141410 verified genuine road pixels with internet disabled, correct SHA and OpenStreetMap attribution.
- Still OPEN: 34 other Android archives not pixel-tested; ODbL distribution/sovereignty review, durable download host and resume, offline labels, Xiaomi hardware.


## Resolved QA scope, 2026-11-13
- Both new CI 43385141310 and 43385141410 succeeded. No observed regression in existing Android smoke tests; old real Hanoi pilot still 58,015 rendered local-road pixels.
- Remaining unverified items are not errors marked resolved: 34 other on-device PMTiles, physical Xiaomi, 35-pack long-term HTTPS hosting, resumable downloads, label fonts, ODbL/legal-boundary release audit.


## Android emulator storage race (2026-11-13)
- **Observed FAIL:** run 43387058086, original real Bắc Ninh archive SHA OK and debug APK installed, then `adb push ... /sdcard/Download/24-bac-ninh.pmtiles` failed `remote couldn't create file: Operation not permitted`, before SAF or map rendering. Do not attribute this to Shortbread data or style.
- **Fix candidate:** commit a3e53366 adds `ci/push-emulator-download.sh`: waits for writable emulated storage, retries, and compares uploaded length; both single and multi-region workflows use it. New run 43387711796 PENDING, cannot mark resolved yet.
- **Other evidence:** run 43387058322 independently PASS for real Hà Nội and HCM via older script, showing Android renderer worked for both provinces on that runner.


## 2026-11-13 CI outcome update
- Emulator /sdcard/Download not writable on cold boot from 43387058086: retested and resolved for observed jobs 43387711796 and 43387711798 with helper a3e53366; correct 11MB/17MB/18MB bytes pushed, imported and rendered.
- New separate failure: 43387711858 standard Android emulator old real Hanoi 54KB road-only screenshot returned 0 target pixels after 15-second wait. Build passed, pinned HTTPS pilot, synthetic PMTiles, SAF import, interrupted recovery all passed. Baseline previous version returned 58,015 pixels; retain failure as unresolved until rerun.
- Commit 59c2192 adds bounded screenshot polling after map load, does not remove test or reduce 500-pixel threshold. Retest 43388632601 PENDING; inspect log/PNG if still 0.


## 2026-11-13 Android screenshot timing issue, still OPEN
- Run 43387711858: real Hanoi 54KB screenshot checker returned 0 pixels on one capture. Run 43388632601: synthetic PMTiles checker returned 0 pixels on one capture, though all earlier import and HTTPS tests passed. MapLibre draw timing under GitHub virtualized emulator is suspect but not yet proven root cause.
- 59c2192 added strict pixel retry for real Hanoi and fddd00b5 added same for synthetic, without reducing minimum thresholds or skipping geometry verification. Run 43389395898 PENDING.
- Dedicated real Shortbread region 43387711796 / 43387711798 remains SUCCESS, with 3/35 legitimate Android visual evidence.

## 2026-11-13 screenshot timing fix CLOSED with evidence
- CI 43389395898 **SUCCESS**, Android build + emulator including previously failing synthetic and tiny Hanoi screenshots. 39,985 synthetic and 58,015 real Hanoi road-color pixels. Both strict assertions preserved; errors from 43387711858 and 43388632601 were intermittent CI timing conditions under this test configuration; not proof of app deterministic bugs.
- New test effort run 43391332272: original 34 provincial files assigned to four emulator shards, outcomes pending and must be inspected individually, especially remote centers in sparsely mapped areas.

## 2026-11-13 34-shard harness failures, corrective actions
- **43391332272 overall FAILURE:** four-shard bash `while read < ledger` used stdin, and inherited `adb` subprocesses consumed ledger rows. Each processed only one province; 04 and 08 nevertheless proved live valid road screenshots, 11 failed because Pixel Launcher was not responding before picker, not a pack checksum error.
- **43392189567 overall FAILURE before province loop:** previous script-edit string replacement containing shell `$'\\t'` was treated as JS replacement special `$'` and spliced out rest of file; `bash` line 65 syntax error. Fix commit e015ebf2 reconstituted script, verified literal source and added syntax gate to workflow compile job. **43393047973 four live jobs pending** at this checkpoint.
- Keep earlier successful full Android CI 43389395898 and 43391332249, shortbread Bắc Ninh 43391332168; separate them from new test-only harness failures. No testing exceptions or threshold reductions.

## 2026-11-13 six sparse/coastal default-view screenshots under investigation
- True all-34 run 43393047973 found **zero displayed road pixels** for 31 Hải Phòng, 46 Huế, 48 Đà Nẵng, 56 Khánh Hòa, 91 An Giang, 96 Cà Mau on 12.3 zoom at PMTiles archive-header geographic center. The other 28 provinces all passed source SHA, SAF and offline screenshots; 34-shard workflow overall FAILED as intended.
- Cause not yet conclusively proven; geographic region bounding box midpoint can be over water or remote terrain. Candidate fix ad7a8dde uses verified urban points, not synthetic map data, for only these six; test rerun 43395054211 pending. Do not change >500 pixel threshold or treat a camera change as proof before screenshots.


## 2026-11-13 20:55 unresolved emulator UI availability
- 43395054211 34-province full CI CANCELLED: 3 independently successful shard jobs proved 25 real regional archives, but shard0 ANR after first opening and remained blocked until run cancelled. Individual archive SHA and absence of pixels cannot be inferred from incomplete shard0; preserve 32/34 unique province proof count from earlier CI.
- 43396019905 HN/HCM workflow FAIL at launcher modal `Pixel Launcher isn't responding`; live on-device downloaded Hanoi file SHA `ad6f2a...` validated, so failure was before true SAF import (not a shown corrupt PMTiles source). Targeted workflow 43438393919 uses bounded `timeout` with one launcher-only retry, other failures remain hard FAIL.
- National pack memory risk: earlier Python smoke buffered whole original archive; a9cd6ce changes new national QA to `Popen` streamed 1MB readback hash, requires 556,229,998 exact bytes, and prevents retaining raw 556MB archive in evidence. National emulator output pending, not proof yet.


## 2026-11-13 last provincial cases resolved
- In targeted 43438393919 first attempt Hải Phòng blocked by Android Pixel Launcher ANR; one controlled retry succeeded with SHA-verified SAF import and 102,298 actual offline road pixels. Đà Nẵng succeeded 217,958 pixels on first try. These are not new PMTiles errors.
- 34/34 distinct provincial visual proofs now exist across CI runs, although full one-workflow 34-shard success absent. Last 556MB nationwide PMTiles still being tested separately (43438780443).


## 2026-11-13 recurring Pixel Launcher ANR on national import
- National QA 43438780443 verified real nationwide PMTiles source SHA/size and matching SHA within Android Downloads, then UIAutomator saw `Pixel Launcher isn't responding` overlay, blocked `Dữ liệu bản đồ` before SAF. This is **CI environment / system UI failure before data import**, not a map file checksum failure. Do not promote 556MB into proven Android rendered catalog.
- Fix aa2f975: bounded retry **only when** exact Launcher ANR text is found; launch activity again after force-stop launcher, retain all original hash/SAF/pixel assertions. CI 43439851626 not yet concluded. Another separate Bac Ninh workflow 43438780338 also failed on same system dialog; HN/HCM 43438780281 independently succeeded.


## 2026-11-13 national selected SAF stream discrepancy, still OPEN
- Independently confirmed source national PMTiles v3 header and sha256 from 43379440781. Real nationwide source 556,229,998 byte SHA 678704... also confirmed byte-identical in Android shared Downloads by 43438780443 and 43439851626.
- 43438780443 national **FAIL** on Android Pixel Launcher ANR before SAF; 43439851626 with one allowed retry **FAIL** during SAF import with `Not a PMTiles v3 archive`. Because the selected stream is via DocumentsUI, a mismatch between file stored in Download and content provider stream/name is possible, not yet proven. DO NOT label national archive malformed or count visual approval.
- 02b91f7 adds debug-only peek using BufferedInputStream.mark/reset, logging expected catalog code and valid PMTiles v3 magic on actual URI stream, plus failure log capture; CI 43441211821 pending. Retains full original checks and no synthetic files.


## 2026-11-13 resolved national-stream QA; reproduced XML DocumentsUI contamination
- National 556MB prior errors: run 43438780443 launcher ANR, 43439851626 imported non-PMTiles data, now run 43441211821 **SUCCESS** complete true origin SHA, SAF streamed readback, offline 157,112 vector-road pixels after Launcher ANR retry. Source national archive validated, no longer outstanding render defect.
- Additional real 34-region QA 43441211834 had ONE Tuyên Quang 08 failure: debug app explicitly logged `SAF_SELECTED_STREAM name=navitaxi-picker.xml pinned=none headerBytes=8 isPmtilesV3=false`. Android UIAutomator wrote its own hierarchy into shared /sdcard, making it visible in DocumentsUI Recent and causing a wrong-document tap. The same original 08 archive correctly passed run 43438780272. Not a corrupt tile archive.
- Commit 1ce22829 moves hierarchy XML to shell `/data/local/tmp`, deletes legacy shared copy before opening picker, rejects non-.pmtiles document names at app import, removes raw name from debug logging, adds static/mock automated test. New run 43454411678 and other CI underway, do not mark fix regression PASS without completed tests.


## Latest 2026-11-13 emulator flakes vs PMTiles data
- UIAutomator XML collision root cause addressed by 1ce22829; national run 43454411510 SUCCESS and Bac Ninh run 43454411692 SUCCESS. 34-shard rerun 43454411678 in progress.
- HN/HCM 43454411531 failed when Pixel Launcher itself was not responding, BEFORE app's import menu. Host and Android shared file SHA correct. Added one retry strictly gated on that exact system message in 720efe42; standardized `bash -n` in e05b1ed. Follow-up 43455374999 still running, no false green status.


## 2026-11-13 Android Launcher ANR covered offline proof despite correct rendering
- Reproduced run 43458014933: `ci/check-offline-screenshot.py` found 40,721 colored pixels, but `offline-window.xml` had only `Pixel Launcher isn't responding`, `Close app`, `Wait`; artifact screenshot directly showed NaviTaxiMap offline demo status behind modal. Emulator OS launcher overlay, NOT PMTiles missing or Kotlin transfer error.
- Fix 3b141de strictly detects specific system ANR overlay before any automated recovery; at most 3 bounded tries, re-verifies status and retains screenshot pixel, pid and fatal-exception guards. No unconditional skip, no weakened pixel or mock map. New CI 43459478560 pending.


## 2026-11-13 Pixel Launcher regression closed, downloader now two-gate protected
- CI 43459478560 SUCCESS both Gradle and emulator. `OFFLINE_DEMO_STATUS_READY` + `REAL_OSM_PIXELS_READY` in logs, confirming prior missing UI 43458014933 was system launcher modal masking a valid rendered map, not corruption.
- Downloader code formerly accepted a caller-provided `reviewedHost` argument, a future-policy bypass risk if distribution approval were enabled. Fixed a0a0be16: only static compiled `ReviewedShortbreadSources.byCode` permitted, currently empty and catalog redistribution flag still false. New mocked negative JUnit tests added; 43468092472 awaiting verdict.


## 2026-11-14 00:16 ICT security hardening follow-up
- No outstanding failure from previous 43468092472: GitHub workflow completed SUCCESS in both compile/JUnit and emulator.
- Found potential fail-open HTTP header interpretation in original Range staging: present but unparsable `Content-Length` was previously treated like absent. Commit 076bd58f now explicitly rejects malformed, negative, overflow or incorrect present values before writing. Also rejects oversized/symlinked partial and staging dir. New JVM regressions added; 43471067313 awaiting result.
