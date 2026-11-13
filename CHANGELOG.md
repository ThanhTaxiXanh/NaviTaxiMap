# CHANGELOG
## 2026-11-13 – Phase 3 PMTiles read/render proof
- Local synthetic PMTiles v3 archive with five MVT tiles, reproducible Python generator.
- Native MapLibre PMTiles file reader using private storage + SHA-256 validation.
- UI toggle online map / offline synthetic diagnostic.
- Added emulator airplane-mode cold-launch test, screenshot regression assertion and saved logcat.
- Fixed blank first offline render, confirmed visible synthetic grid at CI 43359838414.
- Documentation updated, no release or main merge.

## Earlier in the day
- Bootstrapped Android app, CI, offline package verifier and source policy tests.
- Fixed Android SDK / Kotlin compatibility.
- Verified OpenFreeMap/OSM street map in Android emulator.
