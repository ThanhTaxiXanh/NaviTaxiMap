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


## 2026-11-14 06:33 ICT latest verified checklist
- [x] Verify full actual source 35/35 in workflow 43526911330: 843734803 streamed original PMTiles bytes, SHA and PMTiles v3, metadata-only 2872-byte receipt.
- [x] Close Android Python test glob failure: workflow 43527115725 full build/JUnit and emulator SUCCESS after 8d6f17462; no added dependency or relaxed tests.
- [ ] PRIORITY: before 2026-11-27 06:25 UTC, download/save BOTH 34-region and 556MB national original artifact ZIPs in **owner-approved private persistent storage** and run local verification against saved copies. CI receipt is not a backup. Do not publish externally without consent and ODbL legal review.
- [ ] Review ODbL redistribution, sustainable HTTPS server, immutable manifest, consent-based downloads, Xiaomi device and routing/legality as separate gates. Keep distributionApproved=false and source registry empty.


## 2026-11-14 06:54 ICT next action after fail-closed Python QA
- [x] Replace all 31 unsafe Python optimized-mode-removable `assert` checks in original archive verifier with mandatory `require` validations; verify correct-v3-header bad-SHA case under `python -O`.
- [x] CI 43530370885 SUCCESS, 35 original PMTiles 843734803 bytes read and SHA verified; 8 test cases under normal and `-O`, metadata-only receipt uploaded.
- [ ] Confirm emulator/Android CI 43530370833 for a1926367 and do not mark green before both jobs conclude.
- [ ] URGENT: approved private durable destination and copy of two original source ZIPs **before 2026-11-27 06:25 UTC**; verify saved copies after copying. Existing receipt is NOT backup; no new storage decision without authorization.
- [ ] Keep HTTPS origin approval empty and `distributionApproved=false`; physical Xiaomi/ODbL/route graph/border approvals remain independent.


## 2026-11-14 07:05 ICT urgent remaining work
- [x] Make private verifier fail-closed with Python optimized mode (31 mandatory checks) and validate all 35 pins against Android catalog in addition to ledger + SHA sums.
- [x] CI 43530846327 SUCCESS, 9/9 regular + `python -O`, 35 originals exact full SHA, 843734803 bytes, no PMTiles uploaded.
- [ ] Check Android CI 43530846299 final two-job status and preserve failure details honestly if emulator fails.
- [ ] Before 2026-11-27 06:25 UTC, save both original temporary artifacts to an explicitly owner-approved **private persistent storage** and verify saved ZIPs. The CI receipt (metadata) is NOT backup; repository/CDN/Releases upload not authorized.
- [ ] Keep distribution and navigation approvals false; ODbL, legally reviewed borders, Xiaomi, durable HTTPS origin and consent-based download remain separate gates.


## 2026-11-14 immediate owner-driven backup readiness
- [x] Android emulator 43530846299 SUCCESS both jobs; private original source checksum CI 43530846327 SUCCESS all 35.
- [x] Implement manual, no-network, owner-path-required `copy_private_originals.py` storing exact original ZIP container privately, with 7 fake ZIP regression tests and atomic no-overwrite, SHA all internal originals.
- [ ] Confirm CI 43533660474 and 43533660507 terminal results. Report failures truthfully; do not claim private backup created by CI.
- [ ] Owner: download BOTH original artifact ZIPs from 43378133931, nominate/create a private POSIX destination (mode 0700), run `copy_private_originals.py` and re-verify saved originals BEFORE 2026-11-27 06:25 UTC. Without manual private storage, both old artifacts will expire. Do not upload real ZIP data to new services without owner authorization.
- [ ] ODbL, hosting, source approval, Xiaomi, navigation/legal boundary gates remain CLOSED.


## 2026-11-14 next concrete verification
- [x] Build manual owner-invoked private ZIP local copy helper; fake tests, 43533660474 source checker all SUCCESS. No owner-approved private destination or real backup yet.
- [x] Inspect failed 43533660507 emulator artifact 14088907632: real Hanoi road viewport blank even with correct OSM/non-navigation UI and previously working synthetic geometry. Strict 500+ blue-road acceptance deliberately did not pass.
- [x] Add one bounded real OSM pilot app relaunch, preserve first blank screenshot/logcat and require same road pixels, commit 786823a.
- [ ] Inspect terminal CI 43534938551; if still FAIL, diagnose from second-round actual screenshot/logs, do not skip real OSM road geometry.
- [ ] PRIORITY before Nov27 2026 06:25 UTC: owner must save BOTH short-lived original ZIP artifacts to explicitly approved PRIVATE durable storage, then use copy tool or verifier to validate copied bytes. Current CI receipt is not a backup.


## 2026-11-14 07:42 ICT immediate decisions
- [x] Close 43534938551 Android CI; both Gradle and emulator SUCCESS (real OSM render round 1, no fallback triggered).
- [x] Close private copy helper QA, 43533660474 source 35 real ZIP verification and 7 mock ZIP copy tests SUCCESS.
- [ ] If the intermittent real OSM empty viewport recurs, investigate rendering/lifecycle from actual screenshot+logcat; CI-only one restart is bounded and still enforces strict pixel threshold, but not yet separately exercised by a forced blank-map test.
- [ ] **Before 2026-11-27 06:25 UTC**, owner manually copy both original temporary PMTiles ZIPs to approved durable PRIVATE storage and verify real saved bytes. Neither CI receipt nor synthetic backup test is durable source backup.
- [ ] Keep routing, border/legal review, ODbL redistribution, reviewed HTTPS origin, Xiaomi device tests as separate prohibited/unapproved gates.


## 2026-11-14 ~11:00 ICT private cloud backup gates
- [x] Install development-only B2/R2 private ZIP backup workflow using GH artifact IDs; full PMTiles and container hashes, immutable copy, readback both, metadata-only CI evidence, no PR/main/public release.
- [x] Preflight GitHub run 43554159334 SUCCESS: all 7 secrets configured, 9+7 tests PASS; uploader intentionally skipped.
- [ ] Owner confirm B2 bucket Private and Cloudflare R2 Public Development URL disabled / no public custom domain, token scopes bucket-only, and storage budget.
- [ ] Owner explicitly authorize private B2+R2 upload. Then trigger trusted develop commit with [private-backup-go] and verify final remote readback; DO NOT claim backup complete before both private cloud objects pass and downloaded checksums match.
- [ ] Original Actions ZIP artifacts expire Nov27 2026 06:25 UTC; if transfer fails, investigate before expiry. Avoid putting raw PMTiles into GitHub repository/Artifacts/Release.


## 2026-11-14 owner cloud credential correction before backup
- [x] Trigger private B2/R2 workflow after explicit owner authorization, record run failures without claiming copy.
- [x] Source original 35 PMTiles verified again on temporary runner, 843734803 bytes, SHA and v3 magic (43555218616).
- [ ] On phone, GitHub → repository Settings → Secrets and variables → Actions, EDIT `B2_BUCKET` to match the Backblaze Application Key's **exact allowed bucket** and `R2_ACCOUNT_ID` to the real 32 hex Cloudflare account ID. NEVER send values to chat.
- [ ] After fixes, trigger another owner-authorized develop [private-backup-go] run and confirm actual B2 and R2 private uploads with FULL remote byte-readback before claiming backup. Current result is FAILED/NOT BACKED UP.
- [ ] Before 2026-11-27 06:25 UTC expiration, preserve both original GitHub Actions artifacts; remain separate from any ODbL redistribution or routing approval.


## 2026-11-14 13:43 ICT cloud preservation accomplished
- [x] Correct GitHub B2_BUCKET and R2_ACCOUNT_ID Secrets (owner).
- [x] Authorized [43567301480](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43567301480) SUCCESS: exact original 2 ZIP artifacts now on B2 and R2, complete SHA-256/bytes/PMTiles readback PASS; 35 originals 843734803 bytes.
- [x] Metadata receipt archived as artifact 14103007744; does NOT contain PMTiles itself.
- [ ] Owner verify **Backblaze bucket Private** and Cloudflare R2 **Public Development URL Disabled/no custom public domain**, remove any public ACL/URLs, enable 2FA, establish lifecycle versions and optional immutable Object Lock retention to avoid deletion. CI verifies CONTENT, not bucket policy.
- [ ] Schedule periodic readback integrity and test restoration of 35 PMTiles from each provider; keep separate provider credentials in secret manager. For future updates, use new versioned folders and reviewed owner-authorized upload only. No public release.
- [ ] ODbL/provenance and Vietnam territorial boundaries review, true road graph/navigation, official HTTPS source approval/Android device tests remain independent.

## 2026-11-14 14:00 ICT new regional selector acceptance gates
- [x] Commit b0e8f600 exact region DocumentsUI picker and frozen 35 SHA/size/zoom guard; no download endpoint.
- [x] Commit 932282c0 direct picker emulator smoke script; airplane mode, real Bac Ninh, strict SAF SHA/bytes + screenshot assertions. Script written, run pending.
- [x] Verify feature CI 43568446708 (Android), 43568446745 (Bac Ninh), 43568446748 (556MB national), 43568446724 (HN/HCM): SUCCESS.
- [ ] Verify four-shard 43568446697 and both newer 43568694116/43568694110 to terminal state. For direct picker require real original SAF/hash and road pixel evidence, not just build success; investigate any FAIL.
- [ ] Owner independently confirm private B2 and R2 bucket privacy, no public URLs, 2FA and retention. Existing copy/readback CI confirms exact content, not security settings.
- [ ] ODbL redistribution, immutable approved HTTPS hosting/manifest, explicit network opt-in, Xiaomi QA, routable road graph, legal borders remain blocked. Keep distributionApproved=false, no PR/main/merge/release.

## 2026-11-14 ~14:03 ICT required direct picker bug acceptance
- [x] Investigate real failed 43568694110 log, identifying AlertDialog message suppressing selectable 35-region list.
- [x] Fix e5bc37de UI list with warning title, add scroll-by-accessibility in smoke; correct Python bounds regex e6675812 and rerun trigger af93e2ab.
- [ ] Confirm 43569253498 native DocumentsUI region 24 selection, full SHA/bytes, immediate offline Shortbread pixels+OSM/no-navigation status; success required before closing UX bug.
- [ ] Check 43569253506 normal Android and all-34 provinces prior and new shard CI final; no false green assertions.

## 2026-11-14 ~14:11 ICT completed direct picker gate
- [x] Restore all 35 Android Shortbread catalog rows hidden by AlertDialog message/list interaction; preserve safety warning.
- [x] Verify direct select Bắc Ninh through actual DocumentsUI and SHA+byte+offline 266,196 pixel proof: CI 43569253498 SUCCESS.
- [x] Revalidate full Android compile/JVM/emulator on same latest test/code SHA: 43569253506 SUCCESS.
- [x] Full original 34-province feature regression CI 43568446697 SUCCESS.
- [ ] Check UI-fix-specific full 34-shard 43569186171 to final verdict; do not infer completion while pending.
- [ ] Add wrong-file selected UI negative test and more direct UI province coverage when resources allow; unique-code/filename JUnit already exists.
- [ ] ODbL redistribution and official immutable HTTPS source consent, private B2/R2 policy verification, actual Xiaomi QA, road graph and border legality remain unapproved. No PR/merge/release/main.

## 2026-11-14 ~16:43 ICT follow-through
- [x] Confirm all 34 provincial recheck 43569186171 SUCCESS (compile plus 4/4 emulator shards).
- [x] Add and prove Android SAF wrong-region negative case ebab5fc0, no app-private artifact written and genuine Bắc Ninh import remains functional: CI 43581299023 SUCCESS.
- [ ] Confirm full Android 43581299079 emulator terminal result; do not infer from Gradle job alone.
- [ ] Later test correctly named but corrupted PMTiles bytes on emulator, and direct catalog selection of other provinces; no host or release approvals granted.
- [ ] Keep independent review gates for ODbL redistribution, private B2/R2 access/lifecycle, immutable HTTPS service with user consent, Xiaomi device, routing graph and territorial data. Do not change main, PR, merge or release.

## 2026-11-14 16:45 ICT close final remaining CI
- [x] Final Android debug 43581299079 SUCCESS, both build/JUnit and emulator jobs; focused wrong-file QA 43581299023 SUCCESS. Corrective work validated, no open test result from this batch.
- [ ] Additional negative case for same correct filename with modified bytes; physical Xiaomi and all other approvals still separate.

## 2026-11-14 ~17:05 ICT checksum-tampering acceptance gate
- [x] Write emulator-only same-name/same-length/header-preserving corrupt PMTiles copy and negative SAF test on develop, commit a1102b73. Original ZIPs and B2/R2 remain untouched.
- [ ] Inspect 43582840539 Android full-payload hash rejection, no archive/index/partial, genuine restored source SHA, road pixels; FAIL CLOSED if mismatch in assertions. Confirm terminal result, not just code presence.
- [ ] Check 43582840525 Gradle/JUnit and emulator final result.
- [x] Recognize existing B2/R2 backup SUCCESS 43567301480; do not upload them again without owner approval.
- [ ] Expand regional direct UI QA or local pack lifecycle QA only after tamper regression. ODbL, approved HTTPS source, Xiaomi, routing and legal boundary gates remain blocked.

## 2026-11-14 ~17:05 ICT SHA corruption acceptance
- [x] Test bad checksum with SAME file name, SAME size and SAME 127-byte PMTiles header through actual Android DocumentsUI: 43582840539 SUCCESS, rejected without private archive/index/partial.
- [x] Re-import true source after restore and verify 11049420 bytes, frozen SHA, 266196 offline Shortbread pixels and credits: 43582840539 SUCCESS.
- [x] Confirm normal Android build/JUnit/emulator 43582840525 SUCCESS.
- [ ] Potential next task: mutate an already imported private file and verify refusal to render, without damaging unrelated valid data. Extend direct region catalog UI QA beyond Bắc Ninh.
- [ ] Xiaomi physical test, ODbL redistribution, reviewed app HTTPS source, routing graph, legal boundary review and B2/R2 bucket settings remain separate approval gates; no PR/merge/release/main.

## 2026-11-14 ~17:24 ICT post-import tamper milestone complete
- [x] Test real imported app-private PMTiles originally verified and rendered; flip one byte in app-private copy and prove whole-file SHA differs, original Downloads SHA intact.
- [x] Cold-restart production code path refuses modified stored file before MapLibre style load; UI reports SHA-256/PMTiles changed, no offline road screenshot from corrupt file. Targeted CI 43584575417 SUCCESS.
- [x] General Android build/JUnit/emulator at identical code SHA 59c40667, 43584575373 SUCCESS.
- [x] Document prior 43584011441 FAILED due test harness one-byte dd readback returning empty; fixed with full private file hash test-only 59c40667.
- [ ] Additional protection/QA for modification WHILE an offline MapLibre style is already displaying, plus alternate region direct-picker UX and local data management.
- [ ] Physical Xiaomi, licensing/ODbL review, reviewed HTTPS origins/manifest and explicit download consent, legally correct maritime/admin layers, routing graph and B2/R2 private bucket-policy/lifecycle check remain not approved. Two owner ZIP backups already uploaded and checksum-confirmed; do NOT re-upload without authorization. No PR/merge/release/main.

## 2026-11-14 ~17:43 ICT active offline SHA re-check gate
- [x] Implement local-only blank style when a user explicitly reopens a pinned Shortbread region and stored SHA/PMTiles verification fails (86985f3a). No network fallback, navigation or region border layers.
- [x] Add emulator QA for real displayed road pixels -> app-private one-byte mutation -> reselect same region from catalog without restarting -> explicit verification failure, cleared background, zero road pixels -> cold-start refusal.
- [ ] Verify targeted 43586295098 terminal log marker SHORTBREAD_HOT_TAMPER_INVALID_STYLE_CLEARED_PASS plus original 266196 road pixels, SHA and cold-start markers; investigate any test failure without weakening assertions.
- [ ] Check 43586295069 full normal Android, 43586295082 national, 43586295062 Hanoi/HCM and all four shards 43586295180 terminal results.
- [ ] Still requires separate continuous in-session mutation detection, Xiaomi physical QA, ODbL redistribution, approved HTTPS hosting, routing graph, legal boundaries and private B2/R2 bucket policy/lifecycle review. Owner already saved both original ZIP artifacts to B2/R2; never upload again without authorization.

## 2026-11-14 17:51 ICT active-map checksum acceptance
- [x] Prove native Android user catalog recheck clears previously displayed cached local offline style if installed pack SHA differs: 43586295098 SUCCESS, no road-colored screenshot from corrupted data, cold-start still refuses.
- [x] Recheck original nationwide 556MB real SAF+offline screenshot 43586295082 SUCCESS; Hanoi/HCM 43586295062 and Android debug 43586295069 SUCCESS.
- [ ] Verify all 4 provincial shards in 43586295180 to terminal SUCCESS, compile done; don't infer until complete.
- [ ] Consider adding same fail-closed blank style for local pack management Kiểm tra lại verification UI and optionally integrity detection on app resume. Current behavior requires user explicit catalog recheck, NOT automatic real-time monitoring.
- [ ] Xiaomi hardware, ODbL redistribution, approved HTTPS map origins, proper routing and legal boundaries still blocked. Private B2/R2 backups already exist, no new upload or release.

## 2026-11-14 17:54 ICT close all current CI gates
- [x] Final targeted live-view verification 43586295098 SUCCESS: explicit regional catalog reselect after app-private byte tamper invalidates stale cached offline map, no corrupt style/roads and cold-start still fails.
- [x] Entire provincial archive recheck 43586295180 SUCCESS five jobs including four emulator shards: 34 unique PROVINCE_PASS codes, no duplicates; 34/34.
- [x] Nationwide 556MB real SAF SHA + 157112 offline road pixels 43586295082 SUCCESS; Hanoi/HCM 43586295062 SUCCESS; general Android 43586295069 SUCCESS.
- [ ] Next: ensure pack-management Kiểm tra lại action uses the same cached-offline-style invalidation when verifying a corrupt pack. Optional resume-time integrity monitoring requires separate design/test; no background continuous monitoring exists yet.
- [ ] Continue reviewing ODbL distribution, official approved HTTPS map source and opt-in download, Xiaomi device, legal maritime borders, routing graph and owner B2/R2 bucket policy/lifecycle before release; keep distributionApproved=false and navigationApproved=false, main unchanged.
