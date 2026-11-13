# BUILD_STATUS
2026-11-13 | working branch develop/phase0-android-maplibre

## Verified baseline
- Android AGP 8.7.3, Kotlin 2.2.10, JDK 17, Gradle 8.9, compile SDK 35.
- MapLibre Native Android OpenGL ES 13.5.4.
- Run [43359838414](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43359838414): Android debug build + unit suites + emulator airplane-mode synthetic PMTiles rendering PASS.
- Run [43361851396](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43361851396): Gradle build and unit tests PASS; emulator installed/opened APK, displayed offline geometry and created imported PMTiles + metadata files. Smoke job FAIL because logcat-only success assertion did not see a message.
- New regression script directly reads app-private imported archive bytes and metadata, checks SHA-256 and sourceApproval=UNVERIFIED, rather than relying on logcat.

## Latest pipeline
- Code commit: `62a7cc55bee4b3a44f905fb4360be9dbb5f9d48e`
- Run [43362607683](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43362607683): latest byte-level emulator result pending at time of documentation update.
- Debug APK (earlier same Kotlin code): created, zip integrity verified. Not a production release.

## Blocked
Physical Xiaomi debugging and live GPS; actual Android system-picker interaction; real Vietnam regional tiles, download lifecycle and licensed styles; production signing and navigation QA.
