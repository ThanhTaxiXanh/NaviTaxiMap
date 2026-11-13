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
