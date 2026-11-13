# BUILD_STATUS
Branch: develop/phase0-android-maplibre
Android: AGP 8.7.3, Gradle 8.9, JDK 17, compileSdk 35, Kotlin 2.2.10
Map renderer: org.maplibre.gl:android-sdk-opengl:13.5.4

## Actual validation
- GitHub Actions run 43351850610: FAIL SDK tools package deprecated; fixed with setup-android@v4.
- GitHub Actions run 43351946604: FAIL Kotlin metadata mismatch; fixed compiler to 2.2.10.
- GitHub Actions run 43352195061: PASS Gradle testDebugUnitTest + assembleDebug, PASS 4 Python approval tests, APK artifact available.
- Downloaded build artifact: APK ZIP archive intact; APK has ARMv7, ARM64, x86 and x86_64 libraries.
- APK installation/launch on Android emulator: PENDING, automated smoke job newly added.
- APK installation/launch on Xiaomi: BLOCKED (no authorized ADB/Termux connection).
- Production release: NOT STARTED; the map style is demonstration-only.

Original ZIP's Python tests: 4/4 PASS local; source check and Android build PASS on GitHub CI.
