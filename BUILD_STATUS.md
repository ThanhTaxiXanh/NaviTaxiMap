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
- Run [43362607683](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43362607683): ALL JOBS PASS: Gradle debug + JUnit, Python PMTiles 3/3, geographic policy 4/4, Android emulator online launch, offline rendering in airplane mode, and importer readback SHA-256 match. Evidence: 1268 bytes, SHA-256 860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407, index sourceApproval=UNVERIFIED.
- Latest APK extracted from run 43362607683, ZIP/APK integrity checked. APK SHA-256 d2cca8abe753af410d44b9fe99ffa342f524edc714fdda9d66c6f2c47737dee4. NOT a production release.

## Blocked
Physical Xiaomi debugging and live GPS; actual Android system-picker interaction; real Vietnam regional tiles, download lifecycle and licensed styles; production signing and navigation QA.

## Latest phase 4 crash-recovery QA (2026-11-13)
- Commit tested: `f7515b29f4b9057eef3d82b1dbaacfdf0d1f5ff0`.
- [GitHub Actions 43364225600](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43364225600): **ALL JOBS PASS**.
- Gradle `:app:testDebugUnitTest`, `:app:assembleDebug`; PMTiles fixture Python 3/3; Vietnam approval policy Python 4/4.
- Emulator API 34: online cold-start, synthetic offline tile screenshot/pixel check in airplane mode, PMTiles import and SHA-256 readback PASS.
- Injected four app-private incomplete files (two staging, orphan PMTiles, orphan index), force-stopped app, relaunched it; recovery discarded incomplete files while keeping prior committed pair byte-identical. `imported-pack.pmtiles` and `recovered-pack.pmtiles` both SHA-256 `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407`.
- Debug APK 44,638,931 bytes, ZIP integrity PASS, SHA-256 `8b0a1aa4c849786809a3ec58b8089ebde2f54870c0997b05243d7423a3db4cf0`.
- Evidence in workflow artifact `NaviTaxiMap-emulator-evidence`.
- Not a production APK. Physical Xiaomi, real road PMTiles, full SAF picker and cross-process recovery remain unverified.
