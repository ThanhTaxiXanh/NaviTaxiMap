# NaviTaxiMap

NaviTaxiMap is an Android/Kotlin mapping and navigation project for Vietnam with a **zero mandatory paid-service** target. It uses MapLibre Native and OpenStreetMap-based geographic data. The project is a **development prototype**, not a production navigation application.

## Safety and branches

- Development branch: [`develop/phase0-android-maplibre`](https://github.com/ThanhTaxiXanh/NaviTaxiMap/tree/develop/phase0-android-maplibre).
- `main`: **no pushes, merges or pull requests into main without the owner's explicit permission**.
- GitHub Actions builds and tests debug APKs on `develop/**`, without publishing a release.
- Do not use the app while driving: routing, speed signs, offline road data and traffic are not validated or implemented.

## Current capabilities

- Android/Kotlin Activity, MapLibre Native 13.5.4 with online OpenFreeMap Liberty/Dark style using OSM.
- User-initiated GPS location permission. Online map tiles may disclose viewed map area to the third-party tile provider; disclosure precedes location request.
- Tiny synthetic PMTiles v3 tile archive with checksum validation; MapLibre native local PMTiles reader **visibly renders synthetic lines in airplane mode** (CI [43359838414](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43359838414)).
- **Phase 4 import prototype:** `Dữ liệu bản đồ` -> `Nhập tệp PMTiles từ thiết bị`. Android system document picker, streaming app-private file copy, SHA-256, PMTiles header preflight, list/re-verify/delete. Imported files remain **unapproved**, and are not rendered as map layers by this feature.
- Test infrastructure: JUnit, synthetic PMTiles fixture integrity checks, user-supplied geographic data policy checks, emulator screenshot/logcat tests.

See [OFFLINE_MANAGER.md](OFFLINE_MANAGER.md), [ARCHITECTURE.md](ARCHITECTURE.md), [DATA_SOURCES.md](DATA_SOURCES.md), [LICENSES.md](LICENSES.md) and [BUILD_STATUS.md](BUILD_STATUS.md).

## Development build

Required: JDK 17, Android SDK platform 35 and Gradle 8.9.

```sh
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
python3 -m unittest discover -s tools/pmtiles_fixture/tests -v
python3 -m unittest discover -s tools/vietnam-sovereignty-map/tests -v
```

CI config: [`.github/workflows/android-debug.yml`](.github/workflows/android-debug.yml). Debug APK is uploaded as `NaviTaxiMap-debug` in the matching GitHub Actions run. It is not signed for production distribution.

## Not yet developed

Verified geographic offline data packs for 34 provinces/cities, download/pause/resume, search/POI, routing/navigation/TTS, live traffic, Road Intelligence, 3D, legal/historical archive, comprehensive UI and real-device Xiaomi testing.

Nine images provided by the project owner are design references, not screenshots of implemented features. Do not infer real road-network completeness or approved borders from a synthetic test image.

## Phase 4 verified smoke
Run 43362607683 confirms that Android can import a synthetic PMTiles archive into app-private storage with airplane mode enabled, re-read its bytes and check SHA-256 without changing its unverified source status. Full interactive system picker was later verified in emulator CI 43367761361; real provincial data remain untested.

## Phase 4 reliability validation
Latest [CI 43364225600](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43364225600): Android build/unit/emulator tests all PASS, including simulated interrupted-import recovery in airplane mode. Local package recovery and free-space checks are **prototype** features, not proof of fault tolerance under real hardware power loss. Open OSM road-data source/PMTiles pilot requirements are recorded in [REAL_OSM_SOURCE_PLAN.md](REAL_OSM_SOURCE_PLAN.md).

## User-selected file import, verified in emulator
[CI 43367761361](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43367761361) confirms Android's real DocumentsUI picker can open a local `.pmtiles` file, return its content URI to NaviTaxiMap, and finish a private streamed copy with SHA-256 matching the original. In the app, tap **Dữ liệu bản đồ** -> **Nhập tệp PMTiles từ thiết bị** to reach this prototype workflow. Imports are unapproved storage entries, not usable offline driving maps. See [SAF_PICKER_EVIDENCE.md](SAF_PICKER_EVIDENCE.md).

## NEW: Small real OpenStreetMap road preview (Phase 4)
A limited road-only map preview can be opened via **Dữ liệu bản đồ** → **Xem đường OSM Hà Nội offline (mẫu nhỏ)**. It uses 354 real OSM highway ways from a tiny bbox (105.849–105.858 E, 21.031–21.037 N), rendered by MapLibre from 21 locally bundled PMTiles vector tiles (about 54 KB), **without network access**. Provenance, snapshots, deterministic generator, hashes and ODbL duties are in [HANOI_OSM_PILOT.md](HANOI_OSM_PILOT.md). The preview deliberately has no road labels, citywide coverage, driving graph or legal boundaries; do not use for navigation.

## Real-road offline emulator result
[CI 43371354137](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43371354137): **ALL JOBS PASS** for the tiny real OSM Hanoi pilot. Android API 34 in airplane mode visibly rendered road geometry from the locally bundled PMTiles; image regression counted 58,015 road-colored pixels, and UI displayed ODbL attribution and **KHÔNG dẫn đường**. Same run retained prior system picker, checksum/recovery and synthetic PMTiles renderer checks. This is still not a usable road navigation product.

## NEW: Versioned offline pack management + explicit HTTPS (pilot only)
Open **Dữ liệu bản đồ** → **Quản lý / tải gói OSM Hà Nội**. You can inspect v1/source/hash, use a bundled offline copy, explicitly consent to a tiny (54 KB) HTTPS download from a fixed GitHub commit, verify SHA-256, or delete the app-owned copy. Only this limited visual road-preview is available. The pack is **UNREVIEWED**, cannot navigate, and is not a countrywide offline basemap.

[CI 43374393332](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43374393332): **ALL JOBS PASS**, including an **actual** Android HTTPS fetch of the exact pinned PMTiles, byte-for-byte and metadata verification, then offline road-rendering while in airplane mode (58,015 road-colored pixels), Android file picker and catalog UI tests. See [CATALOG_PACK_EVIDENCE.md](CATALOG_PACK_EVIDENCE.md).
