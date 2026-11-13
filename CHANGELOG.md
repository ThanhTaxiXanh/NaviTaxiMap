# CHANGELOG

## 2026-11-13 Phase 4: Local PMTiles import
- Implemented `LocalPackStore` stream importer with SHA-256 and size limits.
- Added PMTiles v3 structural checks and MVT-only type gating in `PmtilesHeader`.
- Added Android system file picker, imported-pack listing, checksum re-check and confirmed deletion.
- Added JUnit regression tests and emulator fixture import check.
- Replaced fragile logcat-only CI signal with direct verification of imported archive bytes and metadata, confirmed in CI 43362607683 (all jobs PASS).
- Phase 4 not complete until real packages, Android picker and lifecycle/recovery are tested.

## Earlier
- Android project + MapLibre online street map bootstrapped and CI fixed.
- Synthetic PMTiles v3 reader/render verified in Android 34 emulator airplane mode.

## 2026-11-13 Phase 4 reliability
- Added app-private PMTiles import crash-recovery cleanup; only generated UUID staging, orphan archives and orphan index files are targeted.
- Added configurable minimum free-space margin, streaming space checks, storage usage summary and JVM regression tests.
- Added Android emulator test of four simulated incomplete files and preservation of valid archive after restart.
- Verified CI 43364225600 all jobs PASS; documented separately from physical/power-loss reliability and real-road limitations.
- Documented Geofabrik ODbL Vietnam PBF as a candidate source and tilemaker conversion gates, not integrated into app.

## 2026-11-13 Phase 4 Android DocumentsUI end-to-end validation
- Added real system-picker automation using UIAutomator XML, text-bounds matching and Android native system file picker.
- Fixed test confusion between file title and the DocumentsUI preview icon, after inspecting saved evidence from first failing run.
- Verified user-selected synthetic PMTiles import, UI confirmation, app-private file and metadata, SHA-256 readback in offline emulator (CI 43367761361 all jobs PASS).
- No change to data-provenance approval or production readiness, no merge into main.

## 2026-11-13 tiny real Hanoi OSM road pilot
- Fetched one small real OSM API bbox and preserved raw XML SHA-256, timestamp and ODbL attribution.
- Added deterministic Python pipeline for MVT/PMTiles with Hilbert tile ordering and three category-based vector layers.
- Committed frozen compressed normalized road snapshot and 53,956-byte PMTiles asset to dev branch.
- Added offline MapLibre real-road preview with explicit ODbL and no-navigation label.
- Android emulator screenshot run 43370534933 shows real roads in airplane mode; new QA initially failed because Pillow was missing. Replaced Pillow dependency with existing stdlib PNG parser. Final CI 43371354137 **ALL JOBS PASS**, including user-selected PMTiles import, simulated interrupted-import cleanup and real OSM Hanoi road screenshot assertion.

## 2026-11-13 Phase 4 versioned OSM pack catalog and real HTTPS
- Added pinned catalog with immutable commit GitHub raw URL, source OSM SHA, PMTiles SHA, size, version, ODbL and explicit non-navigation status.
- Added atomic versioned pack storage, bounded streaming SHA, previous-version preservation on failed update, orphan cleanup and opt-in HTTPS fetcher with redirect denial.
- Added Android catalog manager with inspect, reinstall from bundled asset, manually download/verify, cancel and delete actions.
- Added 12 JVM tests covering tampering, downgrade, rollback, cancellation, storage pressure and allowlist; 23 total tests PASS.
- Android API 34 in CI 43374393332: **ALL JOBS PASS**. Fetched 53,956 bytes via real HTTPS into app-private pack, independently checked SHA/version/license/non-navigation status, then rendered actual real OSM roads in airplane mode and verified catalog UI.
- One early test incorrectly rejected URL due `/main/` within Android's legitimate `src/main/assets` path; corrected to check the repository Git ref segment in `510c12c`.
- Retains development-only status; no main merge, no 34-province data or route graph.

## 2026-11-13 Data milestone: 35 Vietnam Shortbread PMTiles
- Added official 34 administrative code/name mapping, OSM admin4 geometry audit and full-relation recovery for missing Khánh Hòa polygon.
- Implemented reusable real MVT tile-footprint intersection splitter and pinned go-pmtiles conversion into 34 provincial tilesets and one nationwide tileset.
- Built **35 actual PMTiles** from frozen Geofabrik Shortbread source: 556,229,998 bytes nationwide, 287,504,805 bytes province aggregate; 567,706 country tiles. Did not create placeholders.
- First workflow failed only because Python 3.14 lacked pinned Shapely 2.0.7 binary wheel; fixed using Python 3.12 setup. Final CI build 43378133931 PASS.
- Independent 35-file manifest/head/tile-type/byte-size/SHA256 regression CI 43379440781 PASS, published checksum summary artifact.
- Explicitly not a legal border product or navigable offline route database. No main/release changes, app multi-region catalog still pending.


## 2026-11-13 Shortbread catalog hardening and multi-region visual QA
- Catalog now pins every 35 package exact size from previously independent SHA verification logs (file: tools/vietnam34/PACK_BYTES_VERIFIED.tsv), validates known-region manual SAF imports against expected bytes/hash and storage budget.
- New tests: PinnedRegionImportTest for mismatch rejection, 35-entry Kotlin/Python size/hash tests, real offline Android emulator workflow for Hà Nội + Hồ Chí Minh (CI 43387058322 PASS).
- 01 Hà Nội: 157,112 offline road-color pixels; 79 Hồ Chí Minh: 183,085. Including earlier Bắc Ninh: 3/35 archives screenshot-proven.
- CI file-storage race found in 43387058086. Added retry + byte-length verification helper in a3e53366, repeat CI pending.
- No main, PR, release or distribution changes.


## 2026-11-13 emulator stability follow-ups
- CI storage readiness retry a3e53366 verified with two real Shortbread workflows all green, preserving 3/35 proven region visuals.
- Standard Android debug 43387711858 exposed a one-frame regression in the old tiny Hanoi renderer (0 colored pixels); no production change, strict screenshot wait/retry CI fix 59c2192 and rerun pending.

## 2026-11-13 four-shard Android visual QA
- New true 34/34 province *test harness* `ci/shortbread-coverage-{shard,one}.sh` and workflow `android-shortbread-coverage.yml`, reusing original SHA-pinned provinces, mandatory SAF/airplane-mode/pixel checks. Not a production feature or approved data distribution.
- Province-specific early verified images: Cao Bằng and Tuyên Quang now join Bắc Ninh/Hà Nội/TP.HCM, totaling 5/35; full province workflow still unproven. Test-only fixes for stdin swallowing (781644d) and corrupted shell replacement/eager syntax lint (e015ebf2).

## 2026-11-13 improved real provincial visual previews
- Four-shard 34 original archive QA 43393047973 checked all 34 provinces; 28 PASS screenshot, six blank at geographic archive midpoint. Distinct SHA data verified independently, source unchanged.
- Visual-only product fix ad7a8dde sets known populated viewing anchors for Hai Phong, Hue, Da Nang, Khanh Hoa (Nha Trang), An Giang (Rach Gia), Ca Mau, based on city coordinates from UNGEGN/GeoNames/UNT; no changes to jurisdiction/navigation. Strict offline real-geometry QA rerun launched.


## 2026-11-13 targeted 34-province closure and national OSM QA
- First real 34-province QA with corrected urban cameras yielded 25 new successes in three completed shards, four formerly empty areas fixed; one shard cancelled from emulator Launcher ANR. 32/34 distinct provincial screenshot proofs remain valid across workflow attempts.
- Code af381426 creates 31/48 targeted bounded emulator test to avoid long stalled shards.
- Code a9cd6ce introduces streaming 556MB PMTiles host-side adb readback hash verification for nationwide QA, longer SAF modal wait, debug-only urban detail screenshot without modifying normal national overview. Targeted and nationwide result pending; no release or legal approval.


## 2026-11-13 original 35 OSM Shortbread PMTiles Android emulator milestone
- 34/34 original provincial maps verified in entire-success 43438780272; original 556,229,998-byte Vietnam-wide map additionally verified in independent-success 43441211821, using true Android DocumentsUI, streamed app-private full SHA-256 and airplane mode road screenshot (157,112 pixels). 35/35 unique archives screenshot-proofed, not a release.
- Diagnosed extra run 43441211834 failure as test instrumentation XML accidentally imported from /sdcard/DocumentsUI instead of a region PMTiles. Fix 1ce22829 moved UIAutomator XML into shell-private tmp, app rejects non-.pmtiles URI names, added test, privacy hygiene for debug log. CI retest pending.
