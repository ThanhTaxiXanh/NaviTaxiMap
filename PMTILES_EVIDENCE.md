# PMTiles Android emulator evidence

**2026-11-13** | CI run `43359838414` | Commit `24d83a802109bb368044cc793d00e2d13faf2f76`

https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43359838414

Reproducible synthetic fixture: `tools/pmtiles_fixture/generate.py`
Expected SHA-256: `860e0383397b64eac881932bacf625bbb740db23abba4e008ac846967e7c1407`
Internal compression: none, tile type: uncompressed MVT, z0/z1, 5 entries.

Validation matrix:
1. `testDebugUnitTest`: PASS.
2. `test_fixture.py`: 3/3 PASS.
3. `tools/vietnam-sovereignty-map/tests`: 4/4 PASS.
4. Android API 34 install: Success.
5. Online cold launch: Status ok.
6. Airplane mode enabled; second offline cold launch: Status ok.
7. AndroidRuntime errors: empty; process alive after launch.
8. Pixel check `ci/check-offline-screenshot.py`: PASS; actual MVT grid lines in screenshot.

Artifact: `NaviTaxiMap-emulator-evidence` includes `offline-screenshot.png`,
`offline-full-logcat.txt`, `offline-window.xml`, install and launch logs.

Do not interpret screenshot as a map of Vietnam: geometric lines are intentionally
fabricated for decoder integration testing. Real tile data, routing and province
packages are pending.
