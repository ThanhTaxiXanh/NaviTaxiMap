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
