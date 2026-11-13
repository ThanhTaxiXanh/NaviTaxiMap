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
