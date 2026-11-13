# Phase 4: Real Android DocumentsUI picker import evidence

Date: 2026-11-13 (Asia/Bangkok).
Repository: ThanhTaxiXanh/NaviTaxiMap.
Development branch: develop/phase0-android-maplibre.
Tested source commit: `a3f76d1636a30acca6e56a393fbf60264f3fb7f6`.
GitHub Actions run: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43367761361
Status: **ALL JOBS PASS**.

## End-to-end procedure and proof

1. Android 34 emulator installed the debug APK and successfully displayed online map UI.
2. Airplane mode enabled and Wi-Fi/data disabled.
3. Previous proof of synthetic offline PMTiles rendering, synthetic import and SHA-256 readback, interrupted-import cleanup and preserved committed pack passed.
4. CI put `navitaxi-saf-test.pmtiles` (1,268-byte self-generated fixture) in Android Downloads.
5. UIAutomator selected the **Dữ liệu bản đồ** button and **Nhập tệp PMTiles từ thiết bị** menu item.
6. Android **DocumentsUI**, the actual system `ACTION_OPEN_DOCUMENT` chooser, showed the file. CI clicked the file's `android:id/title` view, **not** its preview icon.
7. MainActivity received the selected content URI and `LocalPackStore` streamed the bytes to the app-private store.
8. App displayed a success confirmation for `navitaxi-saf-test.pmtiles`.
9. CI re-read the app-owned archive with Android `run-as` and checked SHA-256, size, and index metadata.

Actual contents independently checked:
- Source file bytes: **1268**.
- Imported PMTiles bytes: **1268**, byte-identical to source.
- SHA-256: `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407`.
- Index: `name=navitaxi-saf-test.pmtiles`, `size=1268`, `sha256=...`, `sourceApproval=UNVERIFIED`.

Evidence artifact in run 43367761361: `NaviTaxiMap-emulator-evidence`.
Relevant files: `saf-success.png`, `saf-result.xml`, `saf-picker-start.png`, `saf-picker-recent.xml`, `saf-imported-pack.pmtiles`, `saf-imported-pack.properties`, `saf-result.txt`, airplane-mode and offline renderer screenshots.

## Failure/root cause corrected

Initial run 43367038303 failed only new picker smoke because automation tapped a `preview_icon` node with content description containing the filename. That opened the preview instead of returning a selected file. `saf-failure.png` and `saf-failure.xml` prove the file was visible in DocumentsUI. Fix in `a3f76d1` uses exact file-title resource ID. This run 43367761361 is fully green.

## Limitations

This is proof of actual system UI selection, not merely a debug fixture import, **but** the selected file is synthetic; it contains no real streets or approved political boundaries. Provenance is not cryptographically authenticated by this test. Rendering an arbitrary imported PMTiles with a verified source/style, a real Vietnam pilot, offline routing, full deletion workflow, physical Xiaomi testing and production signing are NOT done. Phase 4 remains IN PROGRESS. No changes were made to main.
