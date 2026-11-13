# PHASE 4: PMTiles Import QA Evidence

Date: 2026-11-13. Development branch: develop/phase0-android-maplibre.
Code commit tested: 62a7cc55bee4b3a44f905fb4360be9dbb5f9d48e.
GitHub Actions run: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43362607683

## Actual verified test outcomes
- Android Kotlin: :app:testDebugUnitTest and :app:assembleDebug PASS.
- PMTiles generator tests 3/3, Vietnam source policy tests 4/4 PASS.
- Android 34 emulator installed and launched debug APK (cold start).
- Emulator airplane mode enabled; local PMTiles synthetic vector renderer screenshot pixel check PASS.
- Debug-only import calls the same LocalPackStore.importPack streaming path used by the Android document picker.
- Imported pack and .properties index exist in app-private storage.
- CI reads imported archive back with Android run-as and calculates SHA-256 independently.
- Imported data: 1,268 bytes, sha256 860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407.
- Metadata: sha256 matches, size=1268, minZoom=0, maxZoom=1, sourceApproval=UNVERIFIED.
- APK archived and ZIP integrity checked. APK sha256 d2cca8abe753af410d44b9fe99ffa342f524edc714fdda9d66c6f2c47737dee4.
- Evidence artifact NaviTaxiMap-emulator-evidence in run 43362607683 includes imported-pack.pmtiles, imported-pack.properties, emulator screenshots and logs.

## Failure and fix
Run 43361851396 imported PMTiles and metadata but a logcat-filter success assertion was empty, causing a false-negative smoke failure. Replaced log-based success with independent byte-level verification; run 43362607683 PASS.

## Limitations
- The imported map is a **synthetic** diagnostic fixture. No real Vietnam roads or routing data.
- The interactive Android Storage Access Framework picker code is compiled and unit-tested, but the system picker UI itself has not been exercised on a real Xiaomi.
- Header checks do not constitute a full archive, geography, source provenance or licensing audit.
- The user-selected file import is not yet a complete offline package manager: no pause/resume, remote download, catalog of 34 provinces, automatic map style or crash recovery.
- No signed release and no change to main. Phase 4 remains IN PROGRESS.
