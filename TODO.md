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
