# Phase 4: Local PMTiles pack manager

**Status (2026-11-13): IN PROGRESS.** Local import / verification prototype is present. This is not a downloader or a complete offline map distribution system.

## Implemented

- `MainActivity` -> **Dữ liệu bản đồ** -> **Nhập tệp PMTiles từ thiết bị** opens Android Storage Access Framework `ACTION_OPEN_DOCUMENT` (with `CATEGORY_OPENABLE`).
- The selected document is streamed through `ContentResolver.openInputStream` into a randomly named staging file in app-private `filesDir/offline-packs`. No broad storage access permission, external API or cloud dependency is needed.
- `LocalPackStore` calculates SHA-256 while copying, enforces a configurable size limit (4 GiB default), optionally checks a supplied checksum, and performs a PMTiles v3 header preflight.
- `PmtilesHeader` checks magic/version, section ranges, internal compression flags, MVT tile type, tile counts, zoom bounds, geographic bounds and center. **This is not full directory, attribution or tile validation**.
- Files are synced and activated by same-filesystem atomic moves. Index metadata uses an app-private `.properties` file and `sourceApproval=UNVERIFIED`.
- Local pack listing, SHA-256 re-check and delete require user actions. Delete removes only the app-owned copied file and index, never the original document.
- Startup and pre-import recovery scans **only app-generated UUID paths** for staging files, orphan PMTiles or indexes and removes incomplete imports. Commit marker remains the .properties index.
- Per-chunk capacity check enforces 4 GiB pack limit and default 8 MiB reserve; imported storage usage is shown in UI.
- A debug-only launch flag imports the known synthetic fixture, then re-checks SHA-256, for repeatable emulator testing in airplane mode.

## Tests and evidence

- JUnit `LocalPackStoreTest`: positive import, checksum mismatch, corruption, storage limit and ID validation.
- Existing `OfflinePackVerifierTest` plus Python fixture/data-policy suites.
- GitHub CI `43361851396` showed Android created a `.pmtiles` and a `.properties` file in app-private storage, while a logcat-only assertion produced a false negative.
- A subsequent CI script reads the actual imported bytes via `run-as`, checks their SHA-256 and verifies the index remains `UNVERIFIED`. Run 43362607683 PASS: CI read the imported PMTiles bytes via Android run-as, verified SHA-256 and confirmed sourceApproval=UNVERIFIED in the index. This verifies the debug import path on emulator, not the interactive Android file picker.
- Previous PMTiles renderer proof (synthetic z0/z1 fixture) was VERIFIED in airplane mode: run `43359838414`.
- [CI 43364225600](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43364225600): ALL JOBS PASS, including force-stop/relaunch cleanup of four injected incomplete files, independent SHA-256 readback and preservation of committed PMTiles. The test models interrupted file creation, not real power loss.

## Open requirements

1. **Real source provenance:** A calculated checksum detects later changes, but does not prove the publisher, license, correctness or legal approval of a user-selected file. Signed/approved manifests and an independent provenance audit are still missing.
2. **No arbitrary PMTiles display:** The current import workflow stores and audits files. Arbitrary external PMTiles cannot be safely rendered without identifying the correct vector source layers and an approved offline style, sprites, glyphs and data extent.
3. **Crash recovery:** Basic startup/pre-import cleanup of incomplete generated UUID files is implemented and emulator-tested (CI 43364225600 PASS). However, atomic moves do **not** form an actual metadata/database transaction: cross-process locking, filesystem directory fsync, real power-loss testing and rollback of an existing installed version remain TODO.
4. **Download manager:** Remote downloading, pause/resume/cancel, retry, range requests and updates are NOT implemented. No mass downloading from OpenFreeMap.
5. **34 provinces:** Real licensed provincial PMTiles, POI, search and routing datasets are NOT present.
6. **Device QA:** Android emulator testing is not physical Xiaomi/Termux/GPS testing; actual picker UI and Android permission lifecycle require follow-up.

References:
- [Android Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files)
- [PMTiles v3 specification](https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md)
- [MapLibre Native Android local PMTiles](https://maplibre.org/maplibre-native/android/examples/data/PMTiles/)
