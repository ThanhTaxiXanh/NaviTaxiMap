# LICENSES / DATA USE AUDIT
Updated: 2026-11-13

- **Application source:** owner has not chosen a redistribution license; no release declared.
- **MapLibre Native Android:** BSD-2-Clause, pinned to `android-sdk-opengl:13.5.4` (see https://github.com/maplibre/maplibre-native). Transitive-dependency license inventory remains OPEN.
- **OpenStreetMap data:** ODbL, attribution and any derivative database obligations must be assessed before distributing real offline packs.
- **OpenFreeMap public service:** not an API-key or subscription dependency, but not a guaranteed production service. Terms effective 2026-10-14: https://openfreemap.org/tos/. Offline tile download limits apply and the independent offline packs must be sourced separately.
- **PMTiles format:** synthetic archive generated in-house (no third-party geographic data); reader is provided by MapLibre itself.
- **User-supplied sovereignty-map ZIP:** imported policy-validator logic is project-scoped; original resources are not automatically authorized for public release or display as verified boundaries.

These checks do not clear third-party data licensing for a production APK. Keep an explicit credits page in final UI.
