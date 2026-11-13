# ERROR_LOG
## Resolved from actual GitHub Actions logs
1. CI 43351850610: Android setup action v3 attempted obsolete SDK tools package. Fixed by v4.
2. CI 43351946604: MapLibre 13.5.4 Kotlin metadata 2.2.0 vs Kotlin compiler 2.0.21. Fixed by Kotlin Gradle plugin 2.2.10.
3. CI 43352195061: Gradle unit tests and APK assembly PASS.

## Open
- Emulator installation and launch: pending CI smoke evidence.
- Real Xiaomi: no ADB/Shizuku/Termux channel in current environment, so hardware QA blocked.
- Demo map style is online, not production tile service.
- No PMTiles runtime or approved data, GPS only tested at build level; full navigation not available.
