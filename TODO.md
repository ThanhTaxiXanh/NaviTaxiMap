# TODO
- [x] Render deterministic synthetic PMTiles vector tile in airplane mode on Android emulator.
- [x] Add local streamed PMTiles import, SHA-256, PMTiles header checks and JUnit tests.
- [x] Add SAF import and list/verify/delete user interface code.
- [x] Confirm byte-level importer emulator smoke after replacing logcat-only signal (CI 43362607683 PASS).
- [x] Automate real Android system DocumentsUI PMTiles picker and independently verify stored import in airplane mode (CI 43367761361 PASS).
- [ ] Automate UI deletion, tampered file recovery, cancellation and process-interruption tests beyond controlled startup cleanup.
- [ ] Add signed/approved pack manifests, attribution and independent provenance approval.
- [x] Add basic interruption recovery for private staging/orphan PMTiles/index and streamed free-space safety checks (CI 43364225600 PASS).
- [ ] Hardening: multi-process locking, metadata/directory durability after sudden power failure, disk-full/injected I/O testing on physical Xiaomi and transactional update rollback.
- [x] Capture a tiny REAL OSM Hanoi bbox with source hash, reproducible PMTiles preview and Android screenshot of actual road geometry (21 tiles, 354 highway ways; first emulator image proof 43370534933).
- [x] Confirm fully green emulator QA for real OSM road-only preview in airplane mode (run 43371354137, 58,015 pixel assertion PASS).
- [ ] Obtain independently reviewed, properly segmented licensed real-road data for a meaningful Hanoi/Vietnam province pilot; source-policy/legal/geographic review remains OPEN.
- [ ] Region catalog of 34 provinces, download/pause/resume/cancel/version/update/delete.
- [ ] Integrate local fonts/sprites and reviewed MapLibre style to render real MVT layers.
- [ ] Real Xiaomi GPS and UI performance/rotation/battery tests (BLOCKED).
- [ ] POI/search, offline routing graph, navigation/TTS, road warnings, traffic, 3D.
- [ ] Hoàng Sa/Trường Sa legal/historical archive with independent source review.
- [ ] Production build, third-party attribution audit and signed release only after owner approval.

## Phase 4 versioned pack download
- [x] Add **one** locally pinned/allowlisted HTTPS archive and source-hash-provenance catalog entry (Hanoi visual road preview v1).
- [x] Add bounded on-device HTTPS streaming, checksum/size/PMTiles header verification, rollback-safe versioned promotion, no redirect, manual confirmation/cancel, installed-version UI.
- [x] Verify **actual** HTTPS GET into app-private storage on Android emulator, then airplane-mode drawing of the same 53,956-byte OSM pack and independent SHA-256 readback (CI 43374393332 ALL PASS).
- [ ] Build signed/audited remote manifest and approved release/distribution pipeline; APK-pinned checksum is NOT legal or navigation approval.
- [ ] Download/resume with range requests, pause/resume after process death, retry/backoff, configurable data budget, per-region catalog and 34 provincial packages.
- [ ] Cross-process locking, fsync of parent metadata directories, tested atomic update rollback through sudden power loss on physical device.
- [ ] Test real Android UI cancellation, download retry, corrupt data and delete/reinstall under network loss and rotation; currently only core cancellation is JVM tested.

## 35-region geographical data
- [x] Establish authoritative 34 province/city codes and names effective July 2025.
- [x] Obtain real OSM administrative polygon inputs for all 34 areas, including full-relation recovery for Khánh Hòa; label all as **UNREVIEWED**.
- [x] Download OSM Geofabrik Shortbread Vietnam source, produce 34 regional PMTiles + one national PMTiles (35 total), with per-file SHA256 and no placeholder packs (CI 43378133931).
- [x] Independently reread/rehash/inspect all 35 PMTiles v3/MVT outputs, source fingerprint, 34 official province codes (CI 43379440781 ALL PASS).
- [ ] Integrate Shortbread v1.0 compatible offline road map style into Android and offer a 35-pack verified download/catalog manager; current APK remains limited to small Hanoi pilot.
- [ ] Add streaming resume, partial-download recovery, device storage planning, expiration-safe artifact hosting and update manifests. GitHub Actions artifacts are temporary.
- [ ] Review polygon overlaps, maritime disputed areas/island coverage, province boundary precision, source attribution, ODbL distribution and 35-pack legal release clearance independently.
- [ ] Validate every generated pack with real Android pixel-render smoke, physical Xiaomi, latency, memory, storage and low-bandwidth connectivity tests.


## 2026-11-13 Shortbread Android verification TODO
- [ ] Inspect 43385141310 Android and 43385141410 real Bac Ninh in-airplane-mode screenshot pixel CI; fix any failures before claiming DONE.
- [ ] Confirm 35-pack byte-accurate sizes from generated manifest and establish legal, durable, immutable distribution endpoints before enabling downloads.
- [ ] Test all remaining regional archives, local glyphs/labels, boundary safeguards, ODbL rights and physical Xiaomi.


## 2026-11-13 verified milestone update
- [x] Render ONE real regional Shortbread 1.0 PMTiles (Bắc Ninh) in Android emulator airplane mode with SHA/SAF/pixel/license evidence, CI 43385141410 PASS (266,196 pixels).
- [ ] Wait for standard Android regression CI 43385141310 to complete and record actual result.
- [ ] Extend actual Android pixel QA to other 33 provincial archives and national 556MB archive, optimize storage and memory before marking 35/35 rendered.


## 2026-11-13 full regression result
- [x] Confirm both CI jobs from new feature commit: 43385141310 Android debug + emulator SUCCESS, 43385141410 real Bắc Ninh Shortbread SUCCESS.
- [ ] Android verification 35/35 PMTiles files (only 1/35 new regional render proven); national large archive memory/storage testing pending.
- [ ] Add approved long-term 35-pack download distribution with exact byte lengths, Range resume, metadata/signature policy and cancellation.


## Follow-up after 2026-11-13 exact-size and multi-region work
- [x] Pin exact byte lengths (35/35) alongside SHA-256 using independent verified log, and cross-check via Python/Kotlin.
- [x] Verify Android offline real Hà Nội 01 and Hồ Chí Minh 79 using true SAF import, airplane mode and screenshot pixels; CI 43387058322 PASS; combined new shortbread Android visual coverage is now 3/35 (Bắc Ninh, Hà Nội, TP.HCM).
- [x] Add expected byte-length cap and SHA-pinned preactivation checks to known-region SAF imports, with JVM negative cases (new build unit job successful).
- [ ] Check latest CI of emulator storage-ready retry: 43387711796/43387711798, and full standard Android 43387711858.
- [ ] Test remaining 32/35 packs on actual Android (especially 556 MB nationwide); establish approved long-lived artifact hosting and secure resumable download; no fabricated URLs.


## Latest CI tasks
- [x] Recheck new Android single- and multi-region Shortbread real pixel workflows after emulator-storage retry: 43387711796 and 43387711798 SUCCESS; 3/35 region visuals verified.
- [ ] Recheck Android full smoke 43388632601 after 59c2192 wait/retry screenshot amendment. Prior 43387711858 failed at single-frame Hanoi small pilot screenshot, not marked PASS.
- [ ] If strict retry still fails, inspect MapLibre lifecycle/source/loading diagnostics; preserve known-good 54KB Hanoi mode and screenshot evidence before further feature work.


## Full Android emulator strict evidence next
- [ ] Inspect result and logs for 43389395898 (fddd00b5), with bounded retry screenshots for both synthetic and real Hanoi pilots. Do not mark PASS before both genuine vector pixel assertions pass.
- [ ] If still red, inspect local PMTiles lifecycle and MapLibre thread/source status, retain screenshot and log artifacts for diagnostic reproduction.

## 2026-11-13 15:10 next steps
- [x] Validate final screenshot retry fix via all-success Android run 43389395898, including synthetic PMTiles and real tiny OSM Hanoi pixels.
- [ ] Read complete results for four-shard 34-province run 43391332272; prove each with original SHA, SAF and offline screenshot. Update coverage numerator only from successful individual gói, not from workflow startup.
- [ ] After coverage, inspect 556,229,998-byte national PMTiles import on Android with bounded streaming verification and urban street view. National and physical Xiaomi not tested; no approved distribution host or navigation rights.

## 2026-11-13 QA follow-up
- [x] Verify 04 Cao Bằng and 08 Tuyên Quang individual real offline Android screenshots (20,026/32,639 pixels), increasing demonstrated unique Shortbread archive coverage to **5/35** with previous 01,24,79.
- [x] Diagnose/fix TSV stdin-consumption and accidental bash script templating corruption; introduce static `bash -n` before costly emulators, e015ebf2.
- [ ] Verify all 4 jobs of 43393047973, enumerate PASS/FAIL for each of 34 provinces; investigate legitimate sparse-region pixel failures without lowering thresholds or inventing roads.
- [ ] Test national 556,229,998-byte archive separately, avoid Python buffering full file; no production or distribution permission.

## 2026-11-13 15:40 next technical gates
- [x] Test actual 34/34 distinct province archives on Android via four shards and log each result: 28 genuine pixel PASS, 6 zero-pixel views in 43393047973.
- [x] Add grounded urban preview cameras for six failing regions, preserving SHA-pinned sources and strict pixel threshold; commit ad7a8dde.
- [ ] Verify 43395054211 on four shards after camera fix. Require all provinces each >=500 pixels and end-of-shard pass=selected before asserting 34/34.
- [ ] Distinct nationwide 556,229,998-byte OSM PMTiles: streaming full-file storage and pixel test on emulator, no huge Python in-memory buffering; test Xiaomi later.


## 2026-11-13 20:55 active verification next
- [x] Verify 43395054211 cancelled, not success; count 25 PASS in three shards plus 7 historical shard0 PASS = 32/34 unique provinces with Android pixel proofs.
- [ ] Check 43438393919 for both real 31 Hai Phong and 48 Da Nang; require 2/2 SOURCE SHA+SAF+offline pixels and no launcher hang before claiming 34/34 unique provinces.
- [ ] Check 43438780443 for national 556,229,998 byte archive; require genuine streaming Android readback SHA and offline screenshot, not merely download/build. Debug-only camera at Hà Nội proves one urban spot within national archive, not all 63 regions nor legal boundaries.
- [ ] Check 43438780381 normal Android regression and other workflows after national code edit. No release, main, PR/merge or ODbL distribution approval.


## 2026-11-13 confirmed milestone
- [x] Close last two province archive screenshot gaps: 31 Hải Phòng 102,298 pixels, 48 Đà Nẵng 217,958 pixels, CI 43438393919 PASS; 34/34 provincial maps proven offline individually across runs.
- [ ] Await and analyze nationwide 556MB CI 43438780443. Its success is still necessary for 35/35 distinct archives.
- [ ] Normalize emulator launcher race without diluting real SHA/SAF/pixel assertions; possible independent CI regressions can fail even if a region was proven previously.


## Exact remaining large archive acceptance gate
- [x] Establish true 556,229,998-byte national source SHA and byte-identical Android Downloads transfer from 43438780443.
- [ ] Verify actual full national file SAF import, streaming readback SHA (without 556MB Python in-memory copy), style loaded and offline road screenshot via run 43439851626; do NOT mark 35/35 until then.
- [ ] Investigate continued CI Android launcher ANR if one recovery also fails; preserve source SHA, UI screenshots and hard fail checks. 34/34 distinct provincial visual proofs are already completed independently.


## 2026-11-13 follow-up after all-green provincial run
- [x] Verify single-workflow 34/34 success 43438780272 (9+9+8+8, screenshots and pinned SHA). Keep legal/navigation approval FALSE.
- [ ] Analyze 43441211821 logs, especially `SAF_SELECTED_STREAM` actual name, catalog pinned region and `isPmtilesV3`, to diagnose national 556MB `Not a PMTiles v3 archive` despite correct original and Android Download SHA.
- [ ] Repeat national emulator until **imported** 556,229,998 exact bytes, streamed SHA, offline road screenshot PASS; without this NO 35/35. Do not bypass DocumentsUI or skip SHA/pixel acceptance.


## 2026-11-13 verified milestones and next gates
- [x] 34 original provincial archives true Android SAF/pixel-proof in one all-green run 43438780272.
- [x] 556,229,998-byte national archive true Android SAF/streaming full SHA/airplane-mode pixel-proof 43441211821 SUCCESS, completes **35/35** actual distinct package visual evidence.
- [x] Diagnose 43441211834 DocumentsUI test data collision: `navitaxi-picker.xml` accidentally selected instead of `08-tuyen-quang.pmtiles` via Recents, not real map corrupt. Change CI XML path out of /sdcard and require .pmtiles before app streams. Commit 1ce22829.
- [ ] Check all five CI runs triggered by 1ce22829, especially full 34-shard + national, and preserve any actual FAIL without inventing resolution.
- [ ] Design secure approved long-term regional distribution with stable HTTPS URLs, Range download resume and exact byte/SHA staging; artifact originals expire 27/11/2026 and are not enduring store. No URLs may be invented.
- [ ] Real Xiaomi testing, navigation-appropriate graph/route approval, legal geography, ODbL redistribution attribution/rights audit remain separate.


## Pending CI after 35/35 original offline proof
- [x] True second national 556MB SAF/streamed SHA/offline pixel pass 43454411510, no longer only a one-off.
- [x] Full Android regression and Bac Ninh smoke after correcting SAF XML contamination: 43454411956 and 43454411692 SUCCESS.
- [ ] Check 43454411678 four-shard 34 province recheck after moving XML to /data/local/tmp. Mark clean only from four jobs passing.
- [ ] Check HN/HCM CI 43455374999 and Android CI 43455375003 after controlled launcher retry and syntax gate; do not mask non-system faults.
- [ ] Continue infrastructure design for approved, durable HTTPS 35-pack distribution with resumable Range and pinned manifest; current Actions artifacts expire 2026-11-27, no approval or public URLs.


## 2026-11-13 next development gate
- [x] Verify previous 43454411678 5/5 success for 34/34 maps, national 43454411510, HN/HCM 43455502132, debug 43455502057 all-green.
- [x] Add dormant HTTPS Range staging/resume prototype with HTTPS-only reviewed host and 206+SHA/size rules, fail-closed default distribution gate and mock JUnit. Feature 389d71bf; source-host and UI deliberately absent.
- [ ] Inspect Android CI 43458014933 for compiler/JUnit errors and emulator, fix if needed before declaring new feature PASS.
- [ ] Obtain explicit authorization for ODbL redistribution and actual permanent HTTPS origin, allowlisted host, manifest lifecycle and cache policy before enabling any downloader UI/network.
- [ ] Reduce 2x temporary disk usage and test real network/physical Xiaomi separately, with no automatic background transfer prior to user opt-in.


## 2026-11-13 immediate CI gate
- [x] Diagnose emulator 43458014933 failure with downloaded evidence artifact 14055795953; system Pixel Launcher modal hid actual app status, while 40,721 PMTiles pixels rendered.
- [x] Add bounded Launcher-only overlay recovery to standard `ci/smoke-emulator.sh`, maintaining strict UI and pixel assertions, commit 3b141de.
- [ ] Validate complete 43459478560 Android CI (build/JUnit and emulator) before marking new Range staging feature all-green.
- [ ] Keep `distributionApproved=false` until HTTPS origin, ODbL redistribution permission and durable manifest are explicitly approved. Local test transfer is not a user download.


## 2026-11-13 23:58 next gates
- [x] Check terminal 43459478560 SUCCESS both jobs, underlying Android emulator and pixel verification, eliminating previous pending CI ambiguity.
- [x] Prevent future URL/host injection via reviewed-host argument in catalog download API; empty compile-time reviewed source registry and distribution flag false, a0a0be16.
- [x] Expand mocked Range failure tests to corrupt partial, corrupt resume prefix, low space and gzip body, preserving no-network guarantees.
- [ ] Read final 43468092472 Android compile+JUnit+emulator verdict; if FAIL inspect compiler/assertion logs and fix. No user-triggered downloader or permanent source host has been approved.
- [ ] Independent legal ODbL/attribution review and reviewed HTTPS provider with immutable manifest, physical Xiaomi QA, resumable UI with consent, and routing-specific data constraints remain open.


## 2026-11-14 00:16 ICT gates
- [x] Verify 43468092472 all-success build and emulator.
- [x] Add untrusted read-only progress snapshot and fail-closed malformed Content-Length + symlink/oversized partial checks; 8 JUnit Range test methods, 076bd58f.
- [ ] Verify 43471067313 final Android CI build/JUnit/emulator and fix any genuine errors without weakening checksum or pixel tests.
- [ ] Before any live HTTPS transfer, obtain explicit approved ODbL distribution source, reviewed exact origin and manifest. Catalog `distributionApproved` remains false, registry empty.
- [ ] Later add user-consented progress UI, partial retention policy, physical Xiaomi test and mobile network interruptions. Do not tie current tests to unapproved public URL.


## 2026-11-14 ~00:24 ICT outstanding after full green CI
- [x] Recheck 43471067313: build+JVM and Android emulator both SUCCESS; close earlier pending CI checkpoint.
- [ ] Get explicit ODbL redistribution/hosting approval and stable reviewed immutable HTTPS origin(s) before any live downloader or enabling `distributionApproved`. Reviewed map remains empty.
- [ ] After source approval, add user-consented UI and full real HTTPS Range interruption/resume tests plus Xiaomi physical QA; preserve zero-cost constraint.
- [ ] Preserve original 35 generated PMTiles before short-lived Actions artifacts expire 27/11/2026, via a storage destination approved by the project owner; do not silently publish them.


## 2026-11-14 urgent original PMTiles retention gate
- [x] Build local file/ZIP streaming verifier against 35 pinned hashes + bytes + PMTiles v3, reject missing/extra/duplicate/symlink/ZIP traversal; add seven mock Python tests and receipt-only mode.
- [x] Run actual private original 35-pack verification on GitHub ephemeral CI: 43526911330 success, 843734803 bytes, only 2872-byte metadata receipt artifact 14085237703.
- [x] Document manual private original ZIP backup and validation in `PRIVATE_ORIGINAL_BACKUP.md`; NO private durable backup has been created.
- [ ] **Before 2026-11-27 06:25 UTC**, save BOTH original artifacts (14015875271, 14015113186) to an owner-approved PRIVATE durable destination, verify the copies with script. Do NOT upload to Releases/CDN/repo/third-party public service; ask owner for private storage approval if needed.
- [ ] Confirm Android CI 43527115725 after isolating from optional Shapely pipeline tests.
- [ ] Long-term ODbL rights/attribution, durable reviewed HTTPS hosting and manifest, network opt-in UI, Xiaomi physical QA and navigation-ready routing remain unapproved.
