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
