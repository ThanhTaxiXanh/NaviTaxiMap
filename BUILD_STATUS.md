# BUILD_STATUS

Branch: develop/phase0-android-maplibre
Verified code commit: 24d83a802109bb368044cc793d00e2d13faf2f76
CI: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43359838414

## Verified
- AGP 8.7.3, Gradle 8.9, JDK 17, Kotlin 2.2.10, compileSdk 35.
- MapLibre native OpenGL ES 13.5.4.
- Android :app:testDebugUnitTest and :app:assembleDebug PASS.
- Python PMTiles v3 tests: 3/3 PASS. Vietnam data approval logic: 4/4 PASS.
- Android API 34 emulator: APK install Success; MainActivity COLD launch OK.
- Emulator airplane mode enabled; offline-mode MainActivity COLD launch OK.
- PMTiles source from app-private file rendered synthetic vector grid in screenshot.
- Image inspection script confirms line-color pixels in central map viewport.
- No AndroidRuntime FATAL EXCEPTION in saved log.
- Binary APK ZIP integrity validated separately.
- Offline renderer proof-of-concept: DONE, only for synthetic MVT fixture.

## Still unverified or blocked
- Real Xiaomi installation, GPS accuracy, speed/compass, memory, battery, landscape.
- Real PMTiles province data, legal source licenses, size/updates/corruption recovery.
- Production APK and navigation behavior: NOT STARTED.
