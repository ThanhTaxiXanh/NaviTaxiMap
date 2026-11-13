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


## 2026-11-13 Shortbread milestone candidate
- Commit 4a55a873: 35-entry SHA-pinned manually imported catalog, offline-only MapLibre Shortbread style, Android menu, separate real Bac Ninh emulator workflow, and Kotlin/Python tests.
- Old Hanoi real-road preview retained. CI 43385141310 (Android) and 43385141410 (Shortbread) pending; do not mark DONE.
- Open: stable licensed hosting, integrity-pinned sizes for in-app downloading, Range resume, local fonts/labels, ODbL audit and physical Xiaomi QA.


## 2026-11-13 real Android Shortbread QA verification
- Run 43385141410 all jobs SUCCESS with one real Bắc Ninh 11,049,420 byte PMTiles. DocumentsUI imported exact file/hash; offline renderer displayed 266,196 blue road-color pixels in airplane-mode screenshot. ODbL attribution and KHÔNG dẫn đường warning asserted.
- Evidence artifact 14017389861. Existing full Android regression run 43385141310 was still in progress at this checkpoint.


## Concluded CI checkpoint
- Run 43385141310: both Android build/unit-tests and standard emulator smoke jobs SUCCESS, old Hanoi pilot regression 58,015 road pixels.
- Run 43385141410: real Bắc Ninh importer and Shortbread offline pixels SUCCESS (266,196 pixels). Next: durable distribution, resume, other 34-pack on-device verification and Xiaomi.


## 2026-11-13: verified extra Shortbread provinces
- **PASS** CI 43387058322, true Android DocumentsUI importing original Hanoi and HCM OSM/Shortbread PMTiles, then airplane-mode screenshot verified 157,112 and 183,085 road-color pixels respectively. Combined with earlier Bắc Ninh pass: 3/35 actual Android visual proofs.
- 35/35 entries now pin exact byte lengths as well as filenames/zooms/SHA; import rejects mismatched bytes or hash, with capacity preflight. Feature commit 7d747d1; checks from SHA-audited 43379440781 logs.
- Emulator storage transient failure 43387058086 diagnosed, retry fix a3e53366; repeat CI pending. Stable hosting, Range resume, 32 other Android archives, ODbL and Xiaomi remain unfinished.


## Latest 2026-11-13 CI update
- Real northern/southern Shortbread: 43387711796 Bắc Ninh and 43387711798 Hà Nội + HCM both SUCCESS after robust emulator Download copy helper. Original 3/35 region proof intact.
- Full Android emulator old Hanoi-only pilot regression run 43387711858 FAILED at screenshot 0 road pixels, though its build, HTTPS, synthetic, SAF/recovery checks PASS. Candidate 59c2192 adds bounded screenshot retries, still rejects absent geometry. Rerun 43388632601 PENDING.


## Android regression flake follow-up
- 43388632601 build/JVM SUCCESS, emulator FAILED with 0 synthetic offline vector pixels on single screenshot. Earlier 43387711858 failed with 0 real Hanoi pilot pixels.
- fddd00b5 adds bounded asynchronous MapLibre screenshot polling for synthetic test as well as existing real road sample polling. 43389395898 launched, result not yet verified. Existing 3/35 real Shortbread regional PASS unaffected.

## 2026-11-13 15:10 verified Android regression / full province coverage in progress
- **PASS:** run 43389395898 full debug Android + emulator; synthetic 39,985 colored pixels, real OSM tiny Hanoi 58,015 pixels, other regression UI/import checks PASS.
- 32ff2139 adds four-shard original 34-province Android true-source SHA/SAF/airplane-mode pixel test; run 43391332272 compile PASS, awaiting all 34 per-province results, do not count any unverified regions yet.

## 2026-11-13 15:27 coverage attempt accountability
- Additional **04 Cao Bằng** (20,026 pixels) and **08 Tuyên Quang** (32,639) passed genuine Android SAF+SHA+airplane-mode screenshots in logs of partial run 43391332272. Distinct preview evidence now **5/35**, not 34/35. 11 Điện Biên initially blocked by emulator Launcher ANR.
- 43391332272 FAILED (stdin consumed by adb); 43392189567 FAILED before any province due shell syntax mistake; fixed e015ebf2 with `bash -n` compile gate and file descriptor separation. New 43393047973 compile PASS, four provincial jobs underway. No false claims of green overall runs.

## 2026-11-13 15:40 28/34 real provincial visual PASS; retest 6 initial viewpoints
- Completed 34-province QA 43393047973: 28 province-level SHA+SAF+offline screenshot PASS; 6 failed pixel=0 due to unverified initial view. This yields 28/35 total large archives visually proven (national still untested).
- ad7a8dde changed visual initial camera for those six using sourced urban points, not PMTiles or checksum content; 43395054211 launched to verify strict 34/34 again. Keep legal approval false and route disabled.


## 2026-11-13 20:55 continued execution
- **32/34 unique provincial Shortbread archives Android screenshot-proven** over runs 43393047973 + 43395054211, while latter overall CANCELLED (shard 0 launcher freeze). Codes 31/48 still require proof; exact SHA/source-file ledger 34/34 already known.
- Normal Android debug + emulator CI 43396020034 SUCCESS. HN/HCM standalone 43396019905 FAIL on Android Launcher ANR, not SHA mismatch.
- Narrow 31/48 real CI 43438393919 started by af381426; nationwide real 556,229,998 byte stream SHA/SAF/offline CI 43438780443 started by a9cd6ce, results PENDING.


## 2026-11-13 evidence update
- **34/34 actual provincial PMTiles imported/visually proven offline in Android**, accumulated across previously completed successful individual province checks and targeted SUCCESS 43438393919 (31 102,298 px, 48 217,958 px). 34-province all-green in a single workflow remains blocked by emulator launcher.
- Nationwide 556MB streaming SHA/SAF and screenshot CI 43438780443 underway; do not call 35/35 unless its end-to-end proof succeeds.


## 2026-11-13 new QA state
- National 556MB: 43438780443 copied exact SHA byte-identical original to emulator /sdcard/Download but failed before DocumentsUI due Pixel Launcher ANR; rerun aa2f975/43439851626 has one bounded launcher retry; national visual proof pending, 34/35 distinct larger archives proven.
- HN/HCM 43438780281 SUCCESS on latest byte-pin code (157,112 / 183,085 roads pixels), including actual Android shared-storage SHA and SAF.


## 2026-11-13 full provincial CI milestone and 556MB diagnosis
- **34/34 province PMTiles offline proof SUCCESS in single workflow** 43438780272, four shard totals 9/9+9/9+8/8+8/8. All named SHA pinned user-selected SAF imports; renderer offline checked via screenshots.
- Nationwide original 556MB correct header/SHA in build verifier and correct full SHA on Android Downloads, but Android SAF UI import run 43439851626 failed as `Not a PMTiles v3 archive`, so 35th visual proof remains open.
- Added 02b91f7 debug-only actual SAF stream magic/filename log, national CI 43441211821 underway, cannot claim result before end.


## 2026-11-13 35/35 real offline render milestone
- **35/35 Shortbread PMTiles archives have end-to-end Android emulated device screenshot proof**: all 34 province archives in one success 43438780272; 556,229,998-byte national original stream SHA+SAF+airplane-mode 157,112 road pixels run 43441211821 SUCCESS.
- Additional 34-province run 43441211834 FAIL because uiautomator XML in Android DocumentsUI Recents was accidentally imported instead of Tuyên Quang (08), not a broken PMTiles archive. Fixed instrument file path to /data/local/tmp in 1ce22829 and added app extension gate/unit test. Five new CI runs triggered, awaiting proof.
- Pending: durable ODbL-compliant hosting with signed pinned manifest/resumable downloads, Xiaomi physical verification, accessibility/labels, navigation pipeline based on approved routing data, legal borders review.


## 2026-11-13 final milestone retested on new app build
- Real national PMTiles original 556,229,998-byte SHA and airplane-mode render 157,112 pixels, CI 43454411510 SUCCESS again without launcher retry; 35/35 independent genuine files have proven Android visual screenshots across CI.
- Fixed injected UI XML recents pollution in 1ce22829, full Android CI 43454411956 + Bac Ninh CI 43454411692 both SUCCESS.
- New multiregion CI 43454411531 FAILED at system Pixel Launcher ANR; single ANR-only retry 720efe42, lint gate e05b1ed, rerun CI 43455374999 pending. 34-shard 43454411678 also still running at checkpoint.


## 2026-11-13 next slice: approval-gated resumable transfers
- All unfinished actual region workflows from previous conversation now SUCCESS, including 34/34 one-workflow 43454411678 and 556MB national 43454411510.
- Added Range staging engine (feature 389d71bf) + JUnit FakeRange scenarios for partial resume / wrong 206 / denied redirect / SHA mismatch / host/HTTPS authorization. Gate remains `distributionApproved=false`, no URLs, no Android UI or real network call. Build CI 43458014933 PENDING.


## 2026-11-13 23:08 CI QA fidelity
- New Range client compile/JUnit passed 43458014933, emulator failed because system Launcher ANR masked otherwise correct synthetic offline PMTiles status and 40,721 colored road pixels. Inspected real screenshot and XML, not guesswork.
- 3b141de retries only system ANR overlay and demands final app UI status; ci/smoke-emulator.sh syntax gate. Run 43459478560 pending. No download host/approval enabled.


## 2026-11-13 23:58 origin pinning and verified offline regression
- CI 43459478560 both Android build/JUnit and emulator SUCCESS: offline demo colored geometry + exposed UI state + OSM Hanoi road pixels. Earlier failure 43458014933 was system Pixel Launcher overlay.
- Downloader hardening a0a0be16: reviewed host is no longer a free caller argument of `ApprovedCatalogPackDownloader`, only compiled `ReviewedShortbreadSources.byCode` (empty), plus distribution approval remains false. Added integrity/space/encoding JUnit tests. CI 43468092472 in progress.


## 2026-11-14 00:16 ICT continued execution
- Confirmed latest previous Android CI 43468092472 **all SUCCESS**, closing pending emulator test.
- Commit 076bd58f adds read-only untrusted progress accounting and strict HTTP Content-Length / partial symlink/oversize input validation, with 8 mocked Range JUnit total. Download source list remains empty and distribution approval false.
- CI 43471067313 in progress; no claim of new emulator PASS without final log.


## 2026-11-14 ~00:24 ICT acceptance completed
- CI 43471067313 **SUCCESS** compile + unit tests + Android emulator. Range staging 8 mocked JUnit tests + strict offline/SAF/real OSM regression passed. Code 076bd58f fully exercised in CI insofar as its local mocked behavior; real HTTPS transfer remains untested and prohibited until approval.


## 2026-11-14 06:29 ICT original source preservation readiness
- Real source artifact recheck 43526911330 **SUCCESS** 35 archives, 843,734,803 bytes streamed with SHA & PMTiles v3 headers, evidence artifact 14085237703 only metadata (<3k ZIP), no data republished.
- Added manual local ZIP/folder verification tool and seven Python tests, independent of Shapely. Android CI first run 43526690969 failed due accidental Shapely-dependent test discovery, corrected by 8d6f17462. New 43527115725 still in progress.
- No durable backup of original PMTiles and no source hosting approval. Original source artifacts expire Nov 27 2026 UTC; user must approve private storage destination to retain bytes.
