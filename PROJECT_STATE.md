# PROJECT_STATE
Last verified: 2026-11-13, Asia/Bangkok
Repository: ThanhTaxiXanh/NaviTaxiMap
Branch: develop/phase0-android-maplibre
main: protected by request; DO NOT push, merge, create PR or release without owner consent.

## Evidence and status
- PHASE 0: IN PROGRESS. GitHub repo, permissions, Android CI checked; no access to Xiaomi Termux/Shizuku.
- PHASE 1: IN PROGRESS. Kotlin Android shell with MapLibre, UI and on-demand location; emulator launched.
- PHASE 2: IN PROGRESS. OpenFreeMap/OSM roads online visible in screenshot; needs production data review/attribution and error handling.
- PHASE 3: IN PROGRESS. PMTiles v3 MVT native `pmtiles://file://` READ/RENDER PROOF VERIFIED on Android emulator (synthetic 5 tile pack, airplane mode).
- PHASE 4–18: NOT STARTED except initial policy validation and prototype GPS source code.
- Debug APK: available in GitHub run 43359838414; build, test, emulator smoke and screenshot assertion PASS.
- Xiaomi physical test: BLOCKED until authorized device access.

## Runtime proof
Run: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43359838414
Commit tested: 24d83a802109bb368044cc793d00e2d13faf2f76
Offline synthetic package decoded SHA-256: 860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407
Evidence: `NaviTaxiMap-emulator-evidence` artifact includes offline-screenshot.png,
offline-full-logcat.txt, offline-window.xml, airplane-mode.txt, install/launch logs.
Image visibly includes synthetic grid lines. Test code checks pixels inside map viewport.
No FATAL EXCEPTION recorded in smoke test.

## Constraints
Not a usable navigation app: no real offline road data, search/POI, routing graph,
34 provinces, navigation TTS, traffic, speed signs, 3D or approved legal sea overlays.
No production claim from a synthetic proof.
