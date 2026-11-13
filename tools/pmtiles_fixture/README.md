# Local PMTiles v3 integration fixture

This is a deterministic **560-byte synthetic MVT vector tile** stored as PMTiles v3,
not an OpenStreetMap dataset and not navigation data. It contains schematic grid lines
at z=0, no real coordinates, names, POIs, traffic or routing graph. It is intended
solely to prove MapLibre Native's `pmtiles://file://` path end-to-end.

Generator: `python3 tools/pmtiles_fixture/generate.py`
Tests: `python3 -m unittest discover -s tools/pmtiles_fixture/tests -v`
App asset: `app/src/main/assets/offline_demo.pmtiles.b64`
Expected SHA-256 (decoded archive): `16fc20efb488c208d5e7c2c89daf0adf13a2790bb558234d9544a14038c86f3b`.

The app verifies SHA-256 before activating a local copy in `filesDir`. MapLibre
13.5.4 reads the decoded archive directly from `pmtiles://file://`.
Native PMTiles support is not a package downloader and doesn't provide an offline
catalog, update management, map attribution audit, or the 34 Vietnam packs.

Specification: https://github.com/protomaps/PMTiles/blob/main/spec/v3/spec.md
Android integration: https://maplibre.org/maplibre-native/android/examples/data/PMTiles/
