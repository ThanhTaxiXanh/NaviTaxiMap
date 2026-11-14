# PROJECT_STATE
Updated: 2026-11-13, Asia/Bangkok
Repo: ThanhTaxiXanh/NaviTaxiMap
Working branch: develop/phase0-android-maplibre
main: protected; do not push, merge, create PR or release until owner explicitly approves.

## Phase status
- Phase 0: IN PROGRESS; GitHub/CI verified, Xiaomi Termux/Shizuku not connected.
- Phase 1: IN PROGRESS; Kotlin Android shell and UI, build/emulator smoke proven.
- Phase 2: IN PROGRESS; MapLibre + OpenFreeMap online roads rendered in emulator. Still not a production basemap.
- Phase 3: IN PROGRESS; native PMTiles v3 offline reader/render **proof verified only for synthetic MVT data** (CI 43359838414).
- Phase 4: IN PROGRESS; a **small true OSM Hanoi road-only pilot** (bbox 105.849–105.858 E, 21.031–21.037 N) has been fetched, pinned and built into 21 PMTiles vector tiles, 354 highway ways, SHA-256 ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d. Offline road geometry visibly rendered on Android emulator screenshot in run 43370534933; the run's new image-check step failed only because Pillow was not installed. Retest 43371354137 with standard-library PNG checker pending. No full Hanoi/34-province map, legal boundaries or routing graph. Previous import foundation: LocalPackStore, PmtilesHeader and Android SAF import UI written, unit tested, APK assembled. The emulator test observed actual archive + metadata files; byte-level archive/index regression verified in CI 43362607683 and interruption-recovery emulator regression verified in CI 43364225600, both PASS. **Actual Android system DocumentsUI file-picker import** verified in CI 43367761361 (all jobs PASS, user selection of local synthetic .pmtiles and independent SHA-256 readback). Real regional map packs remain pending.
- Phase 5-18: NOT STARTED, apart from preparatory data-policy validators and simple location code.

## Current code / workflow
- Source code: `app/src/main/java/vn/navitaximap/`.
- Pack import: `data/LocalPackStore.kt`, `data/PmtilesHeader.kt`.
- Unit tests: `app/src/test/java/vn/navitaximap/data/`.
- Emulator CI script: `ci/smoke-emulator.sh`.
- Latest verified CI: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332 (tested code 43a70acc220235fd1f6f13fdf5332eb37f286c59, all jobs PASS: Android build/23 JVM tests, pinned real HTTPS transfer, SAF, crash recovery, real OSM offline map and catalog UI).

## Not demonstrated
No complete province/city PMTiles packs (only a tiny real OSM sample), PMTiles download manager, route graph, POI/search, navigation, traffic, speed signs, terrain/3D or approved legal maritime overlays.
Not installed or tested on the owner's Xiaomi. No production-ready release APK.

## Next verification
Real OSM pilot source snapshot: workflow https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43369521518 (17,828 nodes, 354 road ways); deterministic build: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43370147765 (21 tiles, 53,956 bytes). Full regression CI https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43371354137 **ALL JOBS PASS**: 58,015 real OSM local-road-colored screenshot pixels in central map viewport, source/license warning exposed, no FATAL EXCEPTION. See HANOI_OSM_PILOT.md.

## Phase 4: versioned preview pack catalog, verified 2026-11-13
- **Implemented, limited pilot only:** `PilotCatalog`, `CatalogPackStore`, `PinnedHttpsPackDownloader`. One user-initiated download option, immutable 40-hex GitHub source commit URL, pin to exact 53,956 bytes and SHA-256 `ff932662ba476e570debd19d8323e2c9fed11dfa98322dfd39a4023ed3e0cf9d`.
- Catalog pack installation is private, streamed and staged; archive is validated before atomically promoting index. Failed updates preserve prior installed version in tested scenarios; recovery removes generated orphan files. This is NOT a cross-process/durable database transaction.
- Local catalog UI supports checking installed version/hash, reinstalling from APK, confirmed delete and explicitly confirmed HTTPS transfer with cancellation hook.
- CI [43374393332](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332) ALL JOBS PASS. Emulator online received actual file through `PinnedHttpsPackDownloader` (origin=PINNED_HTTPS), re-read 53,956 bytes SHA exact, then airplane mode displayed roads and 58,015 colored pixels. Native DocumentsUI picker, simulated interrupted imports and provenance dialog PASS.
- **sourceApproval=UNREVIEWED_VISUAL_PREVIEW; navigationApproved=false**. Full 34-region catalog, signed external manifests, pause/resume downloads, usable navigation and physical Xiaomi QA remain NOT STARTED/IN PROGRESS as appropriate.

## Data expansion: 35 actual OSM PMTiles, 2026-11-13
- **ALL 35 DATA PACKS GENERATED** (34 official-code province/city pack labels + one whole-country extract), separate downloadable artifacts, source OSM/Geofabrik Shortbread tiles, 567,706 national MVT tiles through z14.
- Total 34 provincial archive bytes: **287,504,805**. Nationwide archive bytes: **556,229,998**. Nationwide SHA-256: `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`.
- [Generation 43378133931](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931) ALL PASS, [independent 35 SHA verification 43379440781](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43379440781) ALL PASS.
- 34 polygons resolved from OpenStreetMap ODbL administrative relations, including independently recovered Khánh Hòa relation. Selection is tile-level intersection, not exact legal borders or full feature clipping. NOT navigation ready, NOT sovereignty overlays, NOT approved for public release.
- **APK has NOT integrated all 35 files**: existing app only supports the small Hanoi visual-preview catalog. Latest Android build unchanged from prior successfully tested code.
- See `VIETNAM35_PACKS.md` and `tools/vietnam34/SHA256SUMS.txt`.


## 2026-11-13 Shortbread Android 35-pack candidate (new CI not yet verified)
- Starting development HEAD eba19b95b224194c641e529dbff399e877245f35; feature commit 4a55a8734dff9f75b451cdb29414414c92a64104.
- Android now includes SHA-pinned 35-pack manually imported preview catalog plus local Shortbread 1.0 style (streets/land/water/buildings). Existing tiny Hanoi road-only pilot preserved. Files are not bundled into APK.
- New Android CI 43385141310 and real Bac Ninh SAF import / airplane-mode pixel CI 43385141410 are PENDING, not PASS at time of checkpoint.
- OSM Shortbread packs and derived technical admin polygons are NOT road-routing, legal boundaries or approved distribution. No long-lived trusted 35-pack download URL, resume, labels, routing, traffic, Xiaomi tests or release.


## Real Bắc Ninh Shortbread Android renderer VERIFIED (2026-11-13, limited scope)
- GitHub Actions run 43385141410 (commit 4a55a873) SUCCESS: full Gradle debug build/JVM tests, SHA-pinned province artifact retrieval, native Android DocumentsUI import into app-private storage, airplane-mode MapLibre Shortbread 1.0 style and road-pixel screenshot checks.
- Imported **24-bac-ninh.pmtiles**, exactly **11,049,420 bytes**, sha256 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f; 266,196 Shortbread road-color pixels measured in screenshot central viewport.
- Screenshot and XML/log evidence: run 43385141410, artifact 14017389861, expiring 2026-11-27.
- Only **one** of 35 regional archives is proven rendered by Android; all 35 SHA entries are pinned/compared to tool SHA256SUMS in unit tests. Nationwide render, other 33 provinces, label glyphs, network updates and Xiaomi remain unverified.


## Final test checkpoint 2026-11-13: ALL new jobs PASS
- Full Android regression run 43385141310 concluded SUCCESS. New Kotlin unit tests + Python style/catalog checks passed; Android emulator tested existing HTTPS Hanoi catalog, synthetic map, SAF and Hanoi real-road regression. Legacy Hanoi screenshot still showed 58,015 road-colored pixels.
- New real province Shortbread workflow 43385141410 concluded SUCCESS: original Bắc Ninh PMTiles, 11,049,420 bytes, SHA 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f; imported via DocumentsUI and rendered 266,196 road-color pixels offline. All CI results are for feature SHA 4a55a873.
- Interpretation: Shortbread offline renderer VERIFIED for Bắc Ninh; 35 entries hash-pinned and catalog tests PASS; 35/35 on-device rendering NOT verified, 35-pack direct HTTPS download NOT implemented.


## 2026-11-13: exact-size 35 pack guard and 3/35 real Android visual evidence
- Feature commit `7d747d1c64704b4143e4358c00d75724afaea591` pins **exact size bytes** and existing SHA-256 for all 35 catalog entries from previously successful independent verifier run 43379440781. Source ledger: `tools/vietnam34/PACK_BYTES_VERIFIED.tsv`; 34 province total 287,504,805 bytes, country 556,229,998 bytes.
- Android manual SAF import checks known archive filename, provider-reported size (when present), app available capacity, expected byte count and SHA-256 before committing data. Existing unrecognized imports stay UNVERIFIED, with no approval promotion. New Kotlin negative tests exercise wrong size/checksum; Python ledger tests check all 35 exact entries.
- Dedicated multi-region Android offline QA [43387058322](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43387058322) **SUCCESS**: 01 Hà Nội 17,562,631 bytes, 157,112 road-color pixels; 79 Hồ Chí Minh 18,217,716 bytes, 183,085 pixels. Genuine SHA-verified SAF import, airplane-mode style, OSM attribution and no-navigation warning. Evidence artifact 14019316787 expiring 2026-11-27.
- Previously verified 24 Bắc Ninh 11,049,420 bytes, 266,196 pixels (CI 43385141410). **3/35 region files now Android pixel-verified**, NOT the full 35-pack app.
- Run 43387058086 failed before rendering because emulator Download storage was not writable immediately after boot. CI fix commit `a3e53366e622f3d856bbf17f627edd86aee9ab72` retries storage readiness and checks exact pushed bytes. Retest runs 43387711796/43387711798 and standard 43387711858 are **PENDING** at this checkpoint; no success claim until evidence arrives.


## 2026-11-13 follow-up: storage retry passes, broader Android emulator regression awaiting strict pixel retry
- Source fix commit a3e53366: re-check /sdcard/Download readiness and pushed exact byte count; real pack CI 43387711796 **SUCCESS** for Bắc Ninh again (266,196 pixels) and 43387711798 **SUCCESS** for Hà Nội (157,112) and HCM (183,085) in airplane mode.
- Standard Android run 43387711858 **PARTIAL**: APK build and JVM/static tests SUCCESS; emulator smoke completed with FAILURE at legacy tiny Hanoi pilot screenshot (0 real-road pixels on its single 15s screenshot), after synthetic/offline, HTTPS, SAF, simulated crash recovery all PASS. Unchanged old Hanoi style; cannot call full regression green.
- Screenshot test hardening candidate 59c2192 retries actual on-device screenshot and requires the same strict nonzero road-pixel assertion; Android CI 43388632601 PENDING. The prior 3/35 real regional Shortbread visual PASS evidence remains valid and separate from this legacy smoke failure.


## Follow-up full regression, 2026-11-13: visual CI still under verification
- Retest run 43388632601: Android build/JVM PASS; emulator FAILED at SYNTHETIC PMTiles screenshot on first fixed-time capture (0 vector-color pixels). This precedes the old Hanoi road-pilot screenshot check and does NOT invalidate separate 3/35 genuine Shortbread regional SUCCESS workflows.
- CI-only fix commit fddd00b5e848d0a0442f55c010947b565d197e62 adds bounded screenshot polling to synthetic offline mode, preserving its strict original 150-pixel threshold; real Hanoi road-only already got strict 500-pixel screenshot polling in 59c2192.
- New Android regression run 43389395898 is PENDING; do not mark the full workflow PASS until all emulator steps conclude successfully. No renderer source changes or skip of checks.

## 2026-11-13 15:10 VN: full Android CI recovered; original 34-region real QA started
- Full Android regression workflow [43389395898](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43389395898) **SUCCESS** (both Gradle/debug and full emulator), using code SHA fddd00b5. Legacy synthetic offline PMTiles 39,985 vector pixels; small Hanoi OSM 58,015 road pixels. SAF DocumentsUI, immutable HTTPS pilot, crash recovery and catalog UI all PASS. Previous screenshot timing regression closed by strict bounded pixel retries (no threshold relaxation).
- Commit 32ff213997e35a6a1a8c844083ee6c56dd7bc1b2 starts independent emulator SHA+SAF+airplane-mode screenshot validation of **all 34 provincial original** Shortbread PMTiles, sharded across 4 emulator jobs. CI [43391332272](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43391332272) build job PASS, province jobs IN PROGRESS at checkpoint. Distinct from already completed 3/35 visual proofs, and nationwide 556MB remains separate.
- To avoid republishing copyrighted/provisional PMTiles in QA evidence artifact, coverage scripts record SHA, size, SAF dialogs, screenshot PNG and log, but omit extra full PMTiles copy by setting NAVITAXI_SAF_PRESERVE_ARCHIVE=0. Original verified 34 provincial source archive still expires 2026-11-27.

## 2026-11-13 15:27 ICT: corrected 34-shard harness, partial real province evidence
- New four-shard coverage CI attempt 43391332272 ended FAIL due harness bug: adb consumed loop stdin; four shards sampled only one each. Despite failed overall run, individual **Cao Bằng 04** (20,026 road pixels) and **Tuyên Quang 08** (32,639) passed complete source SHA/SAF/airplane-mode screenshot assertions. Hà Nội 01 reverified 157,112. Điện Biên 11 encountered emulator Pixel Launcher ANR before SAF, not a PMTiles failure. Thus **at least 5/35 distinct archives** have independent Android visual proofs including previous 01,24,79; do NOT mark all provinces passed.
- First fix 781644d also hit a generated shell syntax error detected by four emulator jobs on CI 43392189567. Root cause was JS string replace interpreting `$'` in bash `$'\\t'` as special expansion. No province QA was performed in that failed run. The reproducible correction e015ebf22f042c8a7d548983bc20c1ed4510e4b5 restores literal IFS tab syntax, isolates TSV fd3 from adb stdin and adds an early `bash -n` syntax gate before Android emulation.
- CI [43393047973](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43393047973) compile/static shell syntax PASS, four actual province emulator jobs IN PROGRESS at checkpoint. Standard debug 43391332249 and Bắc Ninh 43391332168 SUCCESS from feature commit. Main untouched.

## 2026-11-13 15:40 ICT: 34-region emulator attempt completed, urban camera retest
- Run [43393047973](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43393047973) validated all 34 provincial archives through real Android emulator, exact original SHA/size, DocumentsUI SAF, airplane mode and strict screenshot road-pixels: **28 province PASS**, **6 province FAIL screenshot=0** (31 Hải Phòng, 46 Huế, 48 Đà Nẵng, 56 Khánh Hòa, 91 An Giang, 96 Cà Mau). CI overall failure by design, never call 34/34 green. Other provinces with sparse areas, including Điện Biên, proved road pixels, and one transient Pixel Launcher ANR recovered after one retry.
- These six files were imported and SHA verified; blank screenshot may reflect default geographic midpoint of province bounding box (sea/forest) rather than damaged PMTiles. The rendering cause is a hypothesis, to be retested.
- Feature commit **ad7a8dde9a39a2338ffeace647de40c65c4ffb02** chooses grounded urban viewing anchors for only these six visual previews: Hải Phòng (20.86481,106.68345), Huế (16.46190,107.59546), Đà Nẵng (16.067778,108.220833), Khánh Hòa via Nha Trang (12.25,109.183333), An Giang via Rạch Giá (10.016667,105.083333), Cà Mau (9.176944,105.15). These points DO NOT encode provincial boundaries, legal jurisdiction or routing. Source city coordinate basis: UNGEGN GNS and GeoNames/UNT.
- Full 34-shard rerun [43395054211](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43395054211) plus standard Android/Bắc Ninh/Hà Nội+HCM regression workflows triggered and PENDING at checkpoint. National 556MB is the 35th distinct archive, still untested on Android.


## 2026-11-13 20:55 ICT: 32 distinct provinces Android-proven; targeted and national QA running
- 43395054211 cancellation does not negate 3 *completed successful* emulator shards: 25 provincial original Shortbread PMTiles imported through SAF and shown offline, including four previously blank provinces 46/56/91/96. Shard 0 had unrecovered launcher ANR and was cancelled; its 7 other provinces succeeded on previous 43393047973, yielding 32/34 unique proven provincial maps across runs. 31 Hai Phong and 48 Da Nang have urban camera fix but no new screenshot proof as of checkpoint.
- 43396020034 Android full debug + emulator SUCCESS. 43396019905 HN/HCM special workflow FAIL because Pixel Launcher ANR despite Android Downloads SHA matched original.
- Code af381426 adds bounded and independently logged remaining 31/48 Android proof, workflow 43438393919 PENDING. Code a9cd6ce adds **streaming** 556MB national archive SHA QA and debug urban road camera without changing standard nationwide overview, workflow 43438780443 PENDING. Preserve no-navigation, no distribution permission, no fabricated URLs.


## 2026-11-13 Android real provinces: 34/34 distinct screenshots now demonstrated
- Narrow [43438393919](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438393919) SUCCESS, original 31 Hai Phong 7,279,113 bytes / 102,298 actual offline road-color pixels, 48 Da Nang 11,974,113 bytes / 217,958 pixels. Correct source SHA, native SAF and OSM/no-navigation UI. Hai Phong recovered from one Launcher ANR.
- Together with 32 proven before, **all 34 distinct provincial archives** have real offline Android pixel evidence from multiple workflow runs. Entire 34-shard single run not all green because an emulator job was cancelled; do not conflate those claims.
- Genuinely independent nationwide 556,229,998-byte PMTiles Android QA run 43438780443 remains in progress at writing. No nationwide proof, endorsed navigation, legal boundary or approved distribution.


## 2026-11-13 national 556MB verification: byte-level success, SAF render pending
- After 34/34 distinct provincial Android visual proofs, true nationwide archive `00-viet-nam-all.pmtiles`, 556,229,998 bytes, SHA `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55` was verified original and on Android Downloads by 43438780443. This job FAILED on Pixel Launcher ANR before SAF was invoked, not a rendering/PMTiles checksum failure.
- Fix aa2f975 adds exactly one recovery/retry on launcher ANR, maintains mandatory streaming full-file readback SHA, correct-size assertion and actual airplane-mode road screenshot. New national QA 43439851626 IN PROGRESS. 35/35 NOT proven until real evidence.
- Fresh HN and HCM visual QA 43438780281 SUCCESS; 34/34 province proofs intact. No approved source hosting, map legal borders or navigation approval.


## 2026-11-13 confirmed single-run 34-province Android CI success, national unresolved
- Run 43438780272 **SUCCESS (5/5 jobs)**: all 34 original provincial OSM Shortbread PMTiles in Android SAF+SHA+airplane-mode screenshot, shards 9+9+8+8=34 passing with zero failures. Standalone HN/HCM 43438780281 SUCCESS, full standard Android 43438780381 SUCCESS.
- 556MB national run 43439851626 failed at Android SAF import with message `Not a PMTiles v3 archive`, despite original + Android Downloads full-file SHA correct and earlier independent artifact verifier checking PMTiles v3 magic. Another run 43438780443 had failed on Launcher ANR before SAF. Root cause unproven.
- Debug-only probe 02b91f7 adds non-sensitive `SAF_SELECTED_STREAM` magic validity and region name matching log from ContentResolver stream and captures it in failure evidence. National CI 43441211821 RUNNING. 34/35 visual files proven, not 35/35.


## 2026-11-13 final 35/35 Android screenshot evidence and DocumentsUI regression fix
- Genuine national 00-viet-nam-all.pmtiles (556,229,998 bytes) Android emulator run 43441211821 fully SUCCESS: correct original/Android Downloads SHA 678704bef..., SAF picker original archive after one Launcher ANR retry, app-private streamed SHA256 of full 556MB returned EXACT match, airplane mode actual Hanoi view from national pack 157,112 road-color pixels, OSM and no-navigation UI.
- Earlier 43438780272 entirely SUCCESS 34/34 provincial package screens, four shards 9/9+9/9+8/8+8/8. **Combined 35/35 individual archive visual proof complete in actual Android emulator**. This does NOT approve ODbL distribution, sovereign borders or navigation data; physical device and general geographic accuracy still require work.
- 43441211834 extra province recheck overall FAILED one test: uiautomator wrote `navitaxi-picker.xml` on shared /sdcard, it was exposed to DocumentsUI and selected by a race instead of 08 Tuyen Quang; actual selected stream debug proved XML. Fix 1ce22829 writes instrumentation XML to /data/local/tmp and rejects non-.pmtiles from Android app, with new regression test. New 5 workflow suite started (43454411956,43454411510,43454411678,43454411692,43454411531); conclusions pending.


## 2026-11-13 post-DAL documentation, nationwide retest green
- National original 556,229,998-byte Shortbread PMTiles **REPASS** 43454411510: first-attempt Android SAF import, full streamed app-private SHA matches original, airplane mode 157,112 road-color pixels.
- Android debug + emulator 43454411956 SUCCESS, and genuine Bac Ninh 43454411692 SUCCESS, checking code 1ce22829 (move XML UIAutomator hierarchy to /data/local/tmp and reject non-PMTiles names).
- Independent HN/HCM recheck 43454411531 failed at emulator Pixel Launcher ANR **before SAF** despite exact host/Android Downloads input SHA. One controlled system ANR-only retry added by 720efe42; lint gate in e05b1ed. Multi region rerun 43455374999 and its normal Android regression 43455375003 remain PENDING at checkpoint.
- Four-shard 34-province QA 43454411678 compile/JVM tests passed; province emulators RUNNING. Previous all-success 34 original 43438780272 and nationally verified 35/35 distinct original archive evidence remain valid.


## 2026-11-13: all prior CI green, dormant HTTPS Range client design
- Verified previous unfinished runs: 43454411678 full 34 real provincial Shortbread emulator five jobs SUCCESS, 43454411510 national 556MB screenshot + streamed app-private SHA SUCCESS, 43454411692 Bac Ninh SUCCESS, 43455502132 Hanoi/HCM SUCCESS, 43455502057 full Android debug + emulator SUCCESS.
- Introduced dormant `PinnedHttpsRangeTransfer.kt` with strict HTTPS hostname gate, exact 206 Range resume and pinned size+SHA, app-private partial staging, second LocalPackStore verification. `ApprovedCatalogPackDownloader` refuses all transfers while `Vietnam35Catalog.distributionApproved=false`. No hostname, URL or product download UI configured. Explicit design `PACK_DISTRIBUTION_GATE.md`.
- Feature commit 389d71bf; Android CI 43458014933 PENDING at this note. Existing all-35 Android proofs do not grant routing/boundary/distribution approval.


## 2026-11-13 new HTTPS Range engine: unit green, emulator launcher overlay diagnosed
- Feature 389d71bf internal HTTPS Range continuation, strict reviewed host and SHA pinned, default blocked by `distributionApproved=false`; JUnit and Android compile run 43458014933 SUCCESS.
- Emulator phase 43458014933 FAILED at synthetic offline UI status due actual Pixel Launcher ANR overlay. Artifact 14055795953 inspected: 40,721 genuine colored PMTiles pixels, visible app status behind overlay, UIAutomator XML only system dialog. This is not evidence of a failed renderer or download.
- CI-only fix 3b141de adds bounded overlay recovery while still enforcing UI status and vector pixel proof. Android CI 43459478560 RUNNING at checkpoint. Previous 35 real archive offline visual proof across 43454411510+43454411678 remains SUCCESS.


## 2026-11-13 final Android full CI restored; safe downloader origin policy tightened
- Standard Android debug + full emulator 43459478560 SUCCESS, strict offline PMTiles synthetic screenshot and UI status + Hanoi OSM pilot proofs. Launcher system overlay regression resolved in CI-only bounded retry.
- Code a0a0be16 removes runtime caller-supplied URL and reviewedHost from catalog transfer interface, forces static empty `ReviewedShortbreadSources.byCode` *and* `Vietnam35Catalog.distributionApproved=false`. Adds three more JUnit scenarios: corrupt full partial, corrupt resume prefix, missing free space/gzip response. Now 6 Range JUnit cases. Approval and actual host still absent; no network activated. Android CI 43468092472 PENDING at checkpoint.


## 2026-11-14 00:16 ICT: dormant Range engine progress inspection tightened
- Android CI 43468092472 **SUCCESS both Gradle/JUnit and device emulator** for approved-source-gated Range code a0a0be16. Production download gate still CLOSED: `distributionApproved=false`, compiled reviewed origins empty, no URL in app.
- New feature 076bd58f adds `PinnedHttpsRangeTransfer.inspectProgress` to read *untrusted* .partial byte count without network, even when size equals expected bytes; only full SHA can mark verified. Rejects symlinked staging directory, symlinked or oversized partial and malformed/negative/overflow `Content-Length` when present. Two more JUnit tests (total 8). New Android CI 43471067313 pending as of checkpoint.
- Real OSM Shortbread 35/35 original PMTiles offline visual proof unchanged, legal/admin/routing/redistribution approvals absent.


## 2026-11-14 ~00:24 ICT: new Range safety QA independently green
- 43471067313 GitHub Actions **SUCCESS** Gradle/JUnit and emulator, code 076bd58f. Emulator synthetic PMTiles offline geometry and UI status pass, exact SHA SAF fixture import pass, real Hanoi OSM pilot road pixels plus attribution pass.
- Range `inspectProgress` is read-only/unverified and HTTP Content-Length invalid lengths, symlinked staging or oversized partial are strictly rejected. 8 mocked Range JUnit cases; no actual approved network origin, download UI, shipping or routing.


## 2026-11-14 source artifact full-stream private preservation verifier
- New Python CLI `tools/vietnam34/verify_private_preservation.py` reads both original GitHub artifacts or downloaded/extracted local copies WITHOUT upload or extraction. Checks 35 pinned file names and codes from PACK_BYTES_VERIFIED.tsv cross-consistent with SHA256SUMS, byte sizes, full 1MiB-stream SHA256 and PMTiles v3 8-byte magic; rejects directory symlink, ZIP traversal, duplicate/extra/missing PMTiles. Optional metadata-only JSON receipt with 0600 mode. Tests `test_preservation_receipt.py` mock zip/directories/attacks.
- Actual real 843,734,803 original PMTiles bytes verified in new workflow 43526911330 **SUCCESS**, only receipt ZIP artifact id 14085237703 (~2.9k) uploaded. NO enduring private backup made; original 34 + VN source artifacts run 43378133931 expire Nov 27 2026 around 06:25 UTC. Manual backup guidance `PRIVATE_ORIGINAL_BACKUP.md`.
- Android CI initial 43526690969 FAILED because test glob included Shapely-dependent test_pipeline. Narrowed patterns to two independent unittest modules in 8d6f17462. New Android 43527115725 pending at checkpoint. No approved distribution, routing, or legal borders.


## 2026-11-14 06:33 ICT two final green workflows
- Original ZIP/source validation 43526911330 SUCCESS, 35/35 source files 843734803 bytes exact size, SHA, PMTiles magic; metadata-only 2872-byte receipt, not original archive backup.
- Android code+emulator regression 43527115725 SUCCESS both jobs following isolated Python test patterns, including UI offline geometry, SAF SHA fixture and OSM Hanoi attribution.
- Source 34+national GitHub original artifacts expire 2026-11-27 ~06:25 UTC; owner-approved PRIVATE durable backup still outstanding. `PRIVATE_ORIGINAL_BACKUP.md` and CLI enable offline verification after manual download. No download/distribution approved.


## 2026-11-14 optimized Python original-archive verification protection
- Original private source verifier previously depended on Python `assert` statements which `python -O` disables. Security commit 9935d982 replaces 31 business integrity validations with `require(...)` raising ValueError regardless of optimized interpreter, including SHA/full bytes/magic/header, source ZIP/path and receipt directory. Follow-up a1926367 adds true wrong-SHA-with-valid-magic optimized mode test (8 Python source-verifier tests).
- Real two original GitHub source artifact verifier CI 43530370885 **SUCCESS**: 8/8 tests normal and 8/8 with `-O`, exact 35 original PMTiles 843734803 bytes SHA/size/magic, receipt only. No public data distribution or private durable backup made.
- Android CI 43530370833 remains pending as of documentation. Main branch protected and unchanged. Two original source artifacts expire 2026-11-27 06:25 UTC, user-approved private backup required before then.


## 2026-11-14 07:05 ICT three-way source pin cross-validation
- Private preservation verifier now checks exact 35 source pins across `PACK_BYTES_VERIFIED.tsv`, `SHA256SUMS.txt` and live Android `Vietnam35Catalog.kt`; requires distribution/navigation false. Code 64838b and a ninth regression test for simultaneous ledger+sum modification. Mandatory validation also remains enabled with Python `-O`.
- Full actual archive CI 43530846327 SUCCESS (9/9 normal + 9/9 optimized tests, 35 real archives, 843734803 bytes, metadata receipt only). CI Android 43530846299 pending as of checkpoint. No durable backup or distribution authorization.


## 2026-11-14 07:22 ICT local private ZIP preservation helper (not executed on originals)
- Previous Android 43530846299 SUCCESS both build/emulator; 35 original source archive 43530846327 SUCCESS 843734803 full SHA/magic. Original source GitHub artifacts expire 2026-11-27 ~06:25 UTC, no durable backup has been made.
- Code 40c6ffd adds explicit owner-invoked, network-free `copy_private_originals.py`. Requires manually downloaded source ZIPs, existing POSIX private destination 0700, SHA-verify source PMTiles and original ZIP container, bounded 1MiB copy to 0600 temp, full destination verify and fsync; atomic no-overwrite hardlink and safe idempotent resume, local metadata-only receipt. Seven fake ZIP unit tests cover private mode, no overwrite, corruption, symlink, low disk and receipt immutability. No real original pack copy is performed from CI or this chat. Source/Android workflows 43533660474 and 43533660507 pending.


## 2026-11-14 private copy helper and emulator Hanoi pilot blank viewport
- Local private original ZIP copy CLI code 40c6ffd (never auto-run against originals), seven fake ZIP regressions and source 43533660474 all SUCCESS, 35 originals 843734803 bytes verified. Original source artifacts expire 27 Nov UTC; no destination approved, no durable backup.
- Android 43533660507 build+unit SUCCESS but emulator FAIL on actual Hanoi pilot blank screen: zero blue roads after 9 probes; artifact 14088907632 screenshot and XML show valid UI attribution and no-navigation status, but blank map viewport. Synthetic PMTiles earlier in SAME run 39985 pixels and SAF import passed, so not evidence of original 35 source corruption.
- CI-only fix 786823a permits one bounded relaunch of genuine pilot, then strict road pixel/attribution/SAF checks. Follow-up Android 43534938551 pending at checkpoint.


## 2026-11-14 07:42 ICT private copy helper regression all green
- Source 43533660474 SUCCESS (35 original PMTiles full SHA/magic, 9+9 tests Python normal/optimized + 7 mock private ZIP copy tests). Android 43534938551 SUCCESS (Gradle/JUnit+emulator, synthetic offline, real SAF, real Hanoi OSM roads and ODbL).
- The fallback for blank MapLibre pilot was added in CI 786823a but this successful run rendered real OSM at render_round=1 attempt=1, so fallback wasn't exercised; previous 43533660507 failure remains documented in ERROR_LOG with screenshot evidence.
- No real owner-approved private data copy exists; GitHub artifacts for two PMTiles ZIPs expire Nov27 06:25 UTC. Distribution source list remains empty, distribution flag false.


## 2026-11-14 ~11:00 ICT configured external private backup destinations, upload opt-in still locked
- User reports Backblaze B2 and Cloudflare R2 created + GitHub secrets. Workflow 43554159334 preflight SUCCESS verified seven *names configured* without exposing secret values; Bash syntax + source verifier nine unit tests and seven private-copy mock tests passed. Auth correctness and private bucket flags not programmatically verified.
- New uploader ci/private-original-cloud-backup.sh + .github/workflows/private-original-cloud-backup.yml pinned to original run 43378133931 artifact ZIP IDs, validates 35 original PMTiles SHA, performs immutable no-overwrite B2/R2 uploads and downloaded readback, emits metadata receipts only. It is blocked by exact marker [private-backup-go] on trusted develop branch and **has not transferred any source ZIP**. Main unchanged, distributionApproved=false.
- Manual owner confirmation/private bucket check required prior transfer; original Actions artifacts expire 2026-11-27 06:25 UTC.


## 2026-11-14 13:43 ICT original ZIP preservation complete B2 and R2
- Owner-approved workflow 43567301480 SUCCESS both jobs: 35 original PMTiles exact 843734803 bytes full SHA verified before copy, then 2 original ZIP artifact containers copied to both Backblaze B2 and Cloudflare R2 private-oriented remotes under `originals/github-run-43378133931/` with `--immutable`. Full B2 and R2 remote byte-readback checks both PASS; logs `PRIVATE_B2_READBACK_PASS`, `PRIVATE_R2_READBACK_PASS`, `PRIVATE_B2_AND_R2_BACKUP_PASS`.
- Metadata-only receipt artifact 14103007744 3272-byte ZIP, contains only two cloud confirmation texts, original ZIP sha256 and 35-file JSON receipt; not original map data.
- Cloudflare R2 first transfer attempts returned transient NotImplemented with rclone v1.60.1-DEV; retries succeeded, remote readback passed. No permanent backup integrity error remains. Bucket public exposure, object lock and lifecycle controls still require owner dashboard verification; cloud access rights and continuing retention not proven by checksum CI.
- GitHub originals expire Nov27 but now have TWO verified private cloud copies. No app source URL/approval changed, distributionApproved=false and reviewed sources empty.

## 2026-11-14 ~14:00 ICT 35-region Shortbread direct picker (partial CI proven)
- Verified original 35/35 standalone PMTiles Android offline evidence: provincial run 43438780272 and national 556MB run 43441211821 SUCCESS. Pre-change normal Android 43567301415 SUCCESS; owner-authorized private B2/R2 ZIP readback 43567301480 SUCCESS. These do not imply legal or navigation clearance.
- Feature b0e8f600 on develop: exact region-specific Android DocumentsUI import directly from frozen 35-row catalog, 35 non-overlapping request codes, mandatory original filename/size/SHA/zoom checks, unchanged native local Shortbread preview and no HTTP transfer. JUnit test covers all region picker codes and wrong names.
- Test 932282c0 on develop: actual Bac Ninh DocumentsUI selection from catalog in airplane mode plus app-private SHA, OSM attribution and road pixels. Its own Android CI 43568694110 is IN PROGRESS, not yet proof.
- Feature SHA CI: Android general 43568446708 SUCCESS, Bac Ninh 43568446745 SUCCESS, national 556MB 43568446748 SUCCESS, HN/HCM 43568446724 SUCCESS; all-34-shards 43568446697 IN PROGRESS. New test SHA Android general 43568694116 IN PROGRESS.
- No PR, merge, release, edit to main or unapproved distribution. DistributionApproved=false; reviewed map HTTPS sources empty. Private B2/R2 are backups, NOT app-serving URLs.

## 2026-11-14 ~14:11 ICT verified 35-region picker
- New feature b0e8f600 opens the exact selected province PMTiles via system DocumentsUI, rejects a wrong region filename, checks original pinned SHA/size/zoom before rendering with local Shortbread. No network transfer.
- CI 43568694110 FAILURE exposed preexisting AlertDialog setMessage/setItems conflict hiding province rows. Fixed by e5bc37de, Python scroll bounds regex corrected e6675812, workflow path filter af93e2ab.
- Final commit af93e2ab Android direct picker CI 43569253498 SUCCESS: authentic 24-bac-ninh.pmtiles 11,049,420 bytes, SHA-256 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f, immediate airplane-mode 266,196 road pixels, OSM attribution and warning; normal build/JVM/emulator 43569253506 SUCCESS.
- CI full 34 original provinces on feature b0e8f600 43568446697 SUCCESS; national 556MB 43569186220, HN/HCM 43569186134, general 43569186209 SUCCESS on UI-fix SHA; 34-shard UI-fix run 43569186171 pending at checkpoint. Earlier 35/35 original visual evidence unchanged, but direct picker UX tested only in Bac Ninh.
- main remains ea76d1c110bd32f3393be7c4dbf24f8424772a2a, no PR/merge/release. Sources for public download still not approved, B2/R2 backup is private, navigation/legal boundaries and Xiaomi still unverified.

## 2026-11-14 16:43 ICT: confirmed full 34-region regression and wrong-name SAF rejection
- Prior pending 43569186171 is SUCCESS, including compile and four Android emulator shards covering 34/34 original provincial archives after the catalog UI fix. National 556MB evidence 43569186220 SUCCESS, direct correct Bắc Ninh UI 43569253498 SUCCESS.
- QA commit ebab5fc0 on develop tests selecting a test file named 01-ha-noi.pmtiles while region 24 Bắc Ninh was requested. That misleading filename contains only real Bac Ninh bytes inside isolated emulator and must never be treated as a genuine Hanoi source.
- CI 43581299023 SUCCESS. Log SHORTBREAD_WRONG_REGION_REJECTED_PASS confirms rejection before app-private archive/index; immediately afterward original 24-bac-ninh.pmtiles 11049420 bytes SHA 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f imported and 266196 actual Shortbread road pixels rendered offline. Existing OSM and no-navigation checks retained.
- CI 43581299079 build/JUnit SUCCESS but its emulator job remained IN PROGRESS at this checkpoint; check terminal verdict before claiming full-workflow PASS. Main not changed, no public HTTPS distribution, release, PR, merger or navigation/legal approval.

## 2026-11-14 16:45 ICT final new feature CI closure
- Both workflows for negative SAF QA code ebab5fc0 are terminal SUCCESS: dedicated Android native DocumentsUI 43581299023 (wrong filename rejected/no private file, real Bac Ninh SHA and offline pixels passed), general Android 43581299079 (build/JUnit + emulator both SUCCESS). No CI remains pending for this code change. Separate 34-shard 43569186171 SUCCESS.

## 2026-11-14 ~17:05 ICT source-backup confirmation and same-name corruption QA (pending)
- Owner confirms the two original source ZIP artifacts were already preserved to Backblaze B2 and Cloudflare R2. Independent authorized run 43567301480 SUCCESS: both full original cloud copies/byte readback, 35 original PMTiles 843734803 bytes validated. No new backup upload initiated; private copies are NOT app-download origins. B2/R2 bucket public access/lifecycle dashboard settings remain separate from byte-integrity QA.
- New code a1102b73 on develop only: targeted emulator negative test copies real Bac Ninh PMTiles locally, flips one body byte while keeping exact 24-bac-ninh.pmtiles filename, size and first 127 header bytes, checks pin SHA mismatch, attempts regional DocumentsUI import offline, asserts explicit Source checksum mismatch and no app-private archive/index/partial, then restores genuine original local file and verifies SHA and road-colored pixels. Original archive, cloud backups and repository map assets remain untouched.
- CI 43582840539 targeted and 43582840525 general Android STARTED; do NOT mark tamper defense PASS until emulator log marker and both workflow terminal statuses are verified. Baseline 43581299023/43581299079 wrong-filename/positive-control SUCCESS, 43569186171 34/34 originals SUCCESS and 43569186220 nationwide 556MB SUCCESS.
- No changes to main/PR/merge/release, distributionApproved=false, navigationApproved=false and ReviewedShortbreadSources.byCode empty.

## 2026-11-14 ~17:05 ICT same-name corrupt PMTiles Android QA VERIFIED
- New test a1102b73: focused GitHub run 43582840539 SUCCESS; full-size 11049420-byte original 24-bac-ninh.pmtiles verified first, then an isolated runner-local copy with exactly one changed data byte, same filename/size/127-byte PMTiles header. App correctly reports Source checksum mismatch when selected through true regional DocumentsUI; test asserts NO private archive/index/partial, marker SHORTBREAD_SAME_NAME_BAD_SHA_REJECTED_PASS.
- Test then restores the original on emulator and independently checks original SHA, imports exact source bytes, renders 266196 road-colored pixels offline before and after app restart, OpenStreetMap and KHÔNG dẫn đường visible. Previous wrong-filename negative QA also PASS in same run.
- Normal Android Gradle/JUnit/emulator workflow 43582840525 SUCCESS, both jobs. These prove manual file integrity, not navigation/public distribution. Two source ZIP backups on B2/R2 were already complete and remote-readback verified in 43567301480; no new copy required. Main unchanged.

## 2026-11-14 ~17:24 ICT post-import archive tamper fail-closed VERIFIED
- Developed on develop only: commit 4facd595 adds real Android emulator post-import tamper regression via ci/shortbread-post-import-tamper.py, wired into Bac Ninh smoke and workflow path filter. Initial 43584011441 FAILED after original SAF/import/266196 pixels PASS because test-only adb exec-out dd returned no one-byte readback; no evidence of product checksum bypass. Code fix 59c40667 removes this unreliable byte-readback and requires full private file SHA to prove mutation.
- FINAL test/code 59c40667: focused native Android [43584575417](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43584575417) SUCCESS. Real 24-bac-ninh.pmtiles 11049420 original bytes/SHA imported through DocumentsUI and rendered 266196 road pixels in airplane mode, including post-relaunch. Test then changed one byte in app-private imported COPY, checked its SHA differed and exact size/index persisted, left original Downloads SHA untouched. Cold restart through same production showVerifiedShortbread and packStore.verifyPack refused corrupt source; log SHORTBREAD_POST_IMPORT_TAMPER_COLD_START_REFUSED_PASS, no SHORTBREAD_STYLE_LOADED and no colored offline road pixels in screenshot, no Android fatal exception. Previous wrong-region and same-name wrong-SHA SAF negatives remain PASS in same job.
- General Android build/JUnit/emulator workflow [43584575373](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43584575373) SUCCESS both jobs. Test proves COLD-START reopen refusal, not continuous protection while an already opened MapLibre style is in memory.
- Existing original ZIP cloud backups on Backblaze B2 and Cloudflare R2 remain independently checksum/readback-confirmed 43567301480 SUCCESS; no new copies or public host. DistributionApproved=false, navigationApproved=false, no reviewed public download source, main remains protected.
