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


## 2026-11-13 approval-gated offline pack download scaffolding
- Implemented internal `PinnedHttpsRangeTransfer` with strict HTTPS reviewed hostname, RFC 7233-style bytes range response checking, app-private partial resume, SHA256 final validation and download-abort cleanup. Wrapper `ApprovedCatalogPackDownloader` maintains false catalog redistribution gate and imports only through existing atomic LocalPackStore.
- Added JUnit tests with simulated 206 partial responses, hash mismatches, incorrect host/path, redirects, non-HTTPS and distribution blocked. Added `PACK_DISTRIBUTION_GATE.md`. No hosted pack URL, background job, user-triggered downloads or shipping features yet.


## 2026-11-13 source-approval gate hardening
- Source registry `ReviewedShortbreadSources.byCode` is statically empty; `ApprovedCatalogPackDownloader.fetchApproved(code)` no longer accepts arbitrary URL/hostname caller input, preventing accidental future bypass on distribution approval. Catalog `distributionApproved=false` unchanged.
- Added three negative/edge JUnit tests for complete corrupted partial SHA, contaminated resume prefix, insufficient free disk space and compressed HTTPS range response. Previous Android CI run 43459478560 now all green; feature CI 43468092472 pending.


## 2026-11-14 00:16 ICT strict Range staging and untrusted progress
- Added network-free `inspectProgress` for app-private PMTiles .partial staging; full byte count is visibly *not yet SHA verified*. Reused validation before network. Reject malformed present Content-Length, bad/stale oversized partial, symlinked staging/partial. Two more JVM test scenarios for fake 206 responses and corrupted full-size progress.
- Existing source approval remains two-gate CLOSED; no URL, host, user UI, release or unauthorized OSM redistribution. CI 43471067313 pending.


## 2026-11-14 verified local Range engine hardening
- 076bd58f progress inspection, strict HTTP lengths and safe staging files; 8 mocked Range tests. GitHub Actions 43471067313 SUCCESS in both Android debug/Gradle/JUnit and emulator, including offline geometry, OSM attribution, SAF SHA fixture. No real network source configured or enabled.


## 2026-11-14 private original archive verification workflow
- Added `tools/vietnam34/verify_private_preservation.py` for read-only ZIP/extracted folder PMTiles full SHA+size/header checks, metadata-only private receipt (no server/upload/data copy) and seven attack/corruption regression tests.
- Added on-push CI `private-shortbread-preservation.yml` to download both original GitHub source artifacts from run 43378133931 into disposable runner and upload only receipt. First workflow 43526911330 SUCCESS with 843734803 source bytes and metadata artifact ~2.9 KB.
- Fixed Android test selection to exclude Shapely-dependent geography pipeline tests while still running all preservation mock tests, 8d6f17462. Pending Android 43527115725. Permanent original data copies require owner-approved private storage before Nov 27 expiration.


## 2026-11-14 original source verification fail-closed even when Python optimized
- Replaced all 31 business integrity `assert` statements in `verify_private_preservation.py` with mandatory `require` exceptions. New optimized-mode subprocess checks reject fake header and corrupt SHA even with real PMTiles v3 magic. Added `python3 -O -m unittest` CI alongside normal mode.
- Workflow 43530370885 SUCCESS, 8/8 tests normal + optimized and full 35 original PMTiles 843734803 byte exact SHA/size/magic. Metadata receipt only, no backup or license approval.


## 2026-11-14 source SHA 3-way ledger, sums and Android catalog agreement
- Pinned source preservation verifier cross-checks all 35 exact filenames, codes, SHA256 and sizes against Android Vietnam35Catalog.kt as an independent third pin, plus preserves false source-distribution and navigation flags. Added tampered-both-manifests regression; 9 tests run normal + `python -O`.
- CI 43530846327 SUCCESS on all test modes and real 843734803 bytes original files, uploads only metadata. No durable backup, distribution host or routing approval.


## 2026-11-14 manual secure local original ZIP copies (opt-in only)
- Added `tools/vietnam34/copy_private_originals.py`: explicit source ZIPs and owner-approved POSIX 0700 directory, full stream PMTiles verification, no network/upload, 0600 temporary 1MiB copy, SHA of whole ZIP, atomic no-overwrite hardlink, fsync, check-on-rerun and local metadata receipt. Never writes PMTiles to GitHub/online destination.
- Added 7 mocked ZIP regression tests and included in Android and source-verification CI. No actual private persistent backup performed; owner must run with saved originals. Previous all-green Android 43530846299 and real source 43530846327 confirmed.


## 2026-11-14 private backup helper QA and real-OSM emulator renderer retry
- Source original private 35-pack verification workflow 43533660474 SUCCESS and private no-overwrite ZIP copy unit tests all PASS. Android same code 43533660507 failed at zero pixels in separate real Hanoi sample MapLibre map despite synthetic geometry and SAF passing; evidence artifact 14088907632 inspected.
- Added strictly bounded single real OSM pilot relaunch in emulator CI only (786823a), retaining original pixel threshold and UI warnings; new CI 43534938551 pending.


## 2026-11-14 Android emulator regression restored, private ZIP backup remains manual
- CI 43534938551 SUCCESS Gradle/JUnit & emulator after a prior intermittent zero-pixel Hanoi pilot rendering error; real OSM roads passed first screenshot, fallback not exercised. Source 43533660474 SUCCESS verifies originals and private backup mocked tests. No source file uploaded to new hosting, no real durable private backup made.


## 2026-11-14 explicit owner-opt-in B2 and Cloudflare R2 preservation transfer
- Added develop-only private original ZIP cloud uploader using exact existing source GitHub artifacts 14015875271 & 14015113186, 35 SHA/size PMTiles check, immutable B2/R2 copies, readback checks and metadata-only success receipt.
- Source data is never uploaded to new GitHub artifacts or released, no public bucket policy or app download change. First workflow 43554159334 preflight PASS and all seven GitHub Secret names configured; uploader deliberately skipped pending explicit owner authorization. GitHub repo main not modified.


## 2026-11-14 owner-approved Backblaze B2 + Cloudflare R2 backup of ORIGINAL ZIPs
- Owner corrected GitHub Secrets; immutable upload of both pinned original GitHub Actions artifact ZIPs to both B2 and R2 under `originals/github-run-43378133931/`, readback validation of both copies and 35 embedded PMTiles checksums fully PASS. [Run 43567301480](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43567301480) both jobs SUCCESS.
- Metadata-only artifact 14103007744 (3272 bytes, B2/R2 confirmations + ZIP SHA + PMTiles receipt), source bytes not published; Android `distributionApproved=false`, trusted URLs empty, no change to main or release.

## 2026-11-14 Android Shortbread region-specific SAF picker
- b0e8f600: 35-row catalog now starts a DocumentsUI picker for a chosen missing region and refuses the wrong filename before streaming. After a valid SHA/byte import, it enters existing local Shortbread visual preview. No new URL/permission/download setting.
- 932282c0: add emulator coverage for selecting Bac Ninh via catalog with airplane mode, full app-private SHA readback and road-pixel/attribution assertions. New dedicated test run 43568694110 pending.
- Feature Android CI 43568446708, Bac Ninh 43568446745, national 43568446748 and HN/HCM 43568446724 already SUCCESS; other shards remain in progress. No release or main change.

## 2026-11-14 real Android Shortbread selector
- b0e8f600 region-specific SAF picker, exact name/size/SHA guard, same local style. 932282c0 true direct picker emulator QA.
- 43568694110 correctly FAILED because 35-row AlertDialog list was hidden. Fixed e5bc37de, scroll-parser corrected e6675812, CI filter af93e2ab.
- Direct region Android QA 43569253498 SUCCESS with pinned Bac Ninh 11,049,420 bytes SHA and 266,196 offline road pixels. Android compile/JUnit/emulator 43569253506 SUCCESS. No public distribution/route changes.

## 2026-11-14 Android wrong-region refusal CI
- QA ebab5fc0 introduces emulator-only misleading filename 01-ha-noi.pmtiles (copy of original Bac Ninh data) to verify direct region 24 DocumentsUI path rejects it without installing, followed by genuine Bac Ninh import and offline pixel assertion.
- CI 43581299023 SUCCESS, 11049420 original bytes and SHA checked; 266196 pixels. Full 34-province UI-fix regression 43569186171 SUCCESS. No public distribution or app release.

## 2026-11-14 integrity QA, same filename wrong bytes
- a1102b73: Android emulator test uses a 1-byte-mutated ephemeral copy of original 24-bac-ninh.pmtiles preserving name, length and header. Requires full SHA refusal before app-private promotion and positive re-import of original. No production code change, no published source or cloud write.
- Initial runs 43582840539, 43582840525 pending; no PASS claim until finished.

## 2026-11-14 source SHA tampering test accepted
- Commit a1102b73 adds test-only byte-mutation copy of original Bac Ninh PMTiles under SAME filename/size/header. Android refuses the wrong SHA, leaves no private data, restores genuine source and proves offline map/attribution.
- CI 43582840539 SUCCESS targeted, CI 43582840525 SUCCESS normal build/emulator. Original ZIPs and B2/R2 backups remain untouched.

## 2026-11-14 stored pack cold-start tamper QA
- 4facd595 added CI-only Android post-import tamper test and workflow path trigger. Genuine Bắc Ninh PMTiles loaded successfully, then one byte of only emulator app-private copy changed. Run 43584011441 initially FAILED at test-only adb dd one-byte readback, before mutation, despite positive map QA PASS.
- 59c40667 removed the brittle byte-readback, instead verifying a changed file via complete app-private SHA. New focused 43584575417 SUCCESS: stored tamper refused before MapLibre loads style, no offline road pixels. General 43584575373 SUCCESS. No production Android code changes or source archive/ZIP/cloud writes.

## 2026-11-14 hot re-verification fails closed (pending Android acceptance)
- 86985f3a: MainActivity clears existing cached Shortbread style to a local blank background on explicit SHA/PMTiles verification failure while offline preview is active, resets offline state, records SHORTBREAD_INVALID_STYLE_CLEARED. No unreviewed online fallback or route claims.
- Add emulator test requiring real catalog re-select after app-private corruption, blank screenshot and no original source alteration, then cold-start refusal. Android workflows running; not marked accepted until terminal QA proof.
