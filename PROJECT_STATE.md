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
