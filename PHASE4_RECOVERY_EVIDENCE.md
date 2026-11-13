# PHASE 4: CRASH RECOVERY EVIDENCE

Date: 2026-11-13 (Asia/Bangkok).
Branch: develop/phase0-android-maplibre.
Verified code SHA: `f7515b29f4b9057eef3d82b1dbaacfdf0d1f5ff0`.
CI: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43364225600

## Verified results
- GitHub Actions jobs: Android build/tests PASS; emulator install/launch/offline/import/recovery PASS.
- Android API 34 emulator: synthetic MVT/PMTiles geometry visible while airplane mode enabled; screenshot regression PASS.
- Imported fixture through debug-only normal LocalPackStore importer, then re-read its archive from app-private storage; SHA-256 `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407`.
- Injected four files simulating partial import: `UUID.partial`, `UUID.properties.partial`, orphan `UUID.pmtiles`, orphan `UUID.properties`.
- Force-stopped app, cold launched again; app recovered incomplete files. `imported-pack-files.txt` and `recovered-pack-files.txt` contain the same valid UUID archive/index pair.
- `imported-pack.pmtiles` and `recovered-pack.pmtiles` both 1,268 bytes, SHA-256 `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407`.
- `runtime-errors.txt` and `offline-runtime-errors.txt` were empty in saved CI evidence.
- Artifact `NaviTaxiMap-emulator-evidence` in run 43364225600 includes screenshots, imported/recovered pack bytes, metadata, file lists and launch logs.
- Debug APK 44,638,931 bytes; archive integrity PASS; SHA-256 `8b0a1aa4c849786809a3ec58b8089ebde2f54870c0997b05243d7423a3db4cf0`.

## Limits
Synthetic test only; source not a real Vietnam map. This test simulates incomplete leftover files and normal restart; it does not emulate literal sudden power loss, cross-process writers or storage hardware failures. Local real-road data, full Android file-picker UI test, official geographic boundaries, routes, map data update management, and real Xiaomi testing still pending. Do not mark Phase 4 production-ready.
