# LICENSES / DATA USE AUDIT
Updated: 2026-11-13

- **Application source:** owner has not chosen a redistribution license; no release declared.
- **MapLibre Native Android:** BSD-2-Clause, pinned to `android-sdk-opengl:13.5.4` (see https://github.com/maplibre/maplibre-native). Transitive-dependency license inventory remains OPEN.
- **OpenStreetMap data:** ODbL, attribution and any derivative database obligations must be assessed before distributing real offline packs.
- **OpenFreeMap public service:** not an API-key or subscription dependency, but not a guaranteed production service. Terms effective 2026-10-14: https://openfreemap.org/tos/. Offline tile download limits apply and the independent offline packs must be sourced separately.
- **PMTiles format:** synthetic archive generated in-house (no third-party geographic data); reader is provided by MapLibre itself.
- **User-supplied sovereignty-map ZIP:** imported policy-validator logic is project-scoped; original resources are not automatically authorized for public release or display as verified boundaries.

These checks do not clear third-party data licensing for a production APK. Keep an explicit credits page in final UI.

## Real OSM road-preview source, 2026-11-13
- One small rectangular OpenStreetMap API v0.6 bbox snapshot, attribution `© OpenStreetMap contributors`, **Open Database License 1.0**. See https://www.openstreetmap.org/copyright and https://osmfoundation.org/wiki/Licence/Attribution_Guidelines.
- Raw data source SHA-256 `db180ccb4a19e287888af400513dff2104d4c692b32d060a8416f1b6c08c7697`; normalized frozen roads stored as `tools/osm_hanoi_pilot/snapshot/roads.json.gz` and derived PMTiles as `app/src/main/assets/hanoi_real_roads.pmtiles`.
- Source attribution is displayed in real-road preview UI. **No endorsement of dataset fitness for navigation, legal borders or production distribution**. ODbL share-alike/Produced Work, style credit and release compliance remain open.

## Downloadable visual pilot (not a production release)
- The development APK pins a single exact SHA-256 from OSM-derived 53,956-byte PMTiles, downloadable from a fixed commit on the project's public GitHub repository only on user request. Attribution **© OpenStreetMap contributors (ODbL 1.0)** is preserved in UI and the private index.
- App-private catalog metadata flags `sourceApproval=UNREVIEWED_VISUAL_PREVIEW` and `navigationApproved=false`. Passing checksum/CI cannot replace ODbL publication assessment, proper data credits, any share-alike requirements, geographic fitness review or independent legal overlay verification.

## Full Vietnam Shortbread data production (unreviewed, no public release clearance)
- Source OSM vector tiles from Geofabrik `vietnam-shortbread-1.0.mbtiles`. `© OpenStreetMap contributors`, data ODbL 1.0; derived tiles and works may have attribution/share-alike obligations depending on distribution. Record source URL and SHA256 in `VIETNAM35_PACKS.md`.
- 34 technical polygon masks from OSM relation geometries, same ODbL scope. No public/government boundary certification; an OSM admin_level=4 tag cannot certify legal maritime areas. The user-provided sovereignty-policy resource still needs independent approval before any legal overlay is rendered.
- 35 artifacts are QA/engineering output; `distribution_approved=false`, `navigation_approved=false` in manifest. **Do not release the 35 packs through the app or market as legal/road-authoritative without a documented rights and geography audit.**
