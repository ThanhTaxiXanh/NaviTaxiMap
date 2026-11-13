# BUILD_STATUS
Branch: develop/phase0-android-maplibre
Android: AGP 8.7.3 / Gradle 8.9 / JDK 17 / Kotlin 2.2.10 / compileSdk 35.
MapLibre: org.maplibre.gl:android-sdk-opengl:13.5.4.

## Proven
- CI 43351850610: SDK setup FAIL, fixed by setup-android@v4.
- CI 43351946604: Kotlin metadata mismatch FAIL, fixed by Kotlin 2.2.10.
- CI 43352195061: Gradle debug + Android tests PASS.
- CI 43352713250: emulator installation and basic app launch PASS.
- CI 43353698195: live street map style build and emulator launch PASS;
  screenshot shows roads, water features and traffic network geometry.
- Synthetic PMTiles generator tests 3/3 PASS locally using Python standard library.

## Pending
- Latest offline PMTiles branch change: CI compile + emulator airplane-mode smoke pending.
- Must verify actual MVT vector geometry is visible in screenshot, not only that app launches.
- Xiaomi/Termux device installation and GNSS behavior remain untested.
- Not a release and not a usable navigation APK.

- CI 43358323365: Gradle + fixture tests PASS, emulator started offline in airplane mode but screenshot has blank map. NOT VERIFIED. Started repair with z0+z1 tiles and screenshot pixel assertion.
