# BUILD_STATUS
Branch: develop/phase0-android-maplibre
Android configuration: AGP 8.7.3, Gradle 8.9, JDK 17, compileSdk 35, Kotlin 2.0.21
Map renderer: org.maplibre.gl:android-sdk-opengl:13.5.4

Local environment: có Java 21 và Kotlin compiler; không có Android SDK, Gradle hoặc ADB; không thể build APK tại container.
CI: GitHub Actions `.github/workflows/android-debug.yml` trên push develop/**
CI status: PENDING tại thời điểm tạo commit.
Release APK: NOT STARTED.
Physical Android install/debug: BLOCKED, cần thiết bị được kết nối hoặc kênh log hợp lệ.
Python ZIP source tests: 4/4 PASS (local).
