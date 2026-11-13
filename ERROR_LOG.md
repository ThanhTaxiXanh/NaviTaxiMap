# ERROR_LOG
## Resolved
- Android SDK setup tried an obsolete tools package, upgraded setup-android@v4.
- Kotlin compiler 2.0 metadata mismatch with MapLibre 13.5.4, upgraded Kotlin to 2.2.10.
- Original world-demo tiles had no street detail; switched to OpenFreeMap prototype style.
- First synthetic offline PMTiles screenshot blank; fixed five-tile fixture and added screenshot pixel assertion (CI 43359838414 PASS).

## New incident
- CI 43361851396: import smoke returned FAILURE on absence of `DEBUG_IMPORT_PASS` filtered logcat message. On the same emulator, both imported PMTiles and metadata files were present, and online/offline screens passed. This was a verification-signaling failure, not proof that archive import failed.
- Fix commits: `09c6e11` and `62a7cc5`. Script now reads and checks exact import bytes and metadata with SHA-256. CI 43362607683 PASS; logcat false-negative resolved.

## Open
- Byte-level emulator import smoke verified in CI 43362607683; continue real file-picker and recovery tests.
- Need actual system file-picker interaction and source-provided SHA-256 workflow.
- Need recovery for orphaned data between two atomic renames, resume/pause downloads, storage-pressure handling.
- Xiaomi device is not connected, no physical QA.
- No approved regional basemap, POI, offline routing, verified traffic/speed/legal boundary data.
