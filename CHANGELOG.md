# CHANGELOG

## 2026-11-13 Phase 4: Local PMTiles import
- Implemented `LocalPackStore` stream importer with SHA-256 and size limits.
- Added PMTiles v3 structural checks and MVT-only type gating in `PmtilesHeader`.
- Added Android system file picker, imported-pack listing, checksum re-check and confirmed deletion.
- Added JUnit regression tests and emulator fixture import check.
- Replaced fragile logcat-only CI signal with direct verification of imported archive bytes and metadata.
- Phase 4 not complete until real packages, Android picker and lifecycle/recovery are tested.

## Earlier
- Android project + MapLibre online street map bootstrapped and CI fixed.
- Synthetic PMTiles v3 reader/render verified in Android 34 emulator airplane mode.
