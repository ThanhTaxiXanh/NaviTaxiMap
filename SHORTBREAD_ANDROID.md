# Shortbread 1.0 Android integration checkpoint

**Status: code committed; new CI pending.** This is NOT a navigation or legally approved map.

The SHA-pinned list of 35 OSM/Geofabrik Shortbread v1.0 research PMTiles is in app/src/main/java/vn/navitaximap/data/Vietnam35Catalog.kt. A catalog entry contains the exact official filename, code, zooms and SHA-256 from tools/vietnam34/SHA256SUMS.txt. No source URL is invented: the old GitHub Actions data artifacts are temporary and the project has no approved durable regional download host.

Android user flow: import an original PMTiles archive with Data menu -> import from device (SAF). The new Data menu -> 35 Shortbread packs visualizes recognized files only after comparing filename+zoom+SHA and computing the whole-file SHA again before rendering. The style in app/src/main/assets/shortbread_offline_style.json uses only the app-private pmtiles://file:// archive, paints streets/land/water/buildings, no sprites, glyphs, labels or legal boundary overlays. It displays OSM attribution and a no-navigation warning. The separate 54KB Hanoi road-only pilot is unchanged.

CI: Android debug workflow runs Kotlin unit tests and Python static schema/catalog tests. Dedicated android-shortbread-35-smoke workflow retrieves the previously generated 34-province artifact, checks actual Bắc Ninh PMTiles SHA-256, drives Android DocumentsUI SAF import and on-device SHA readback, switches to airplane mode, renders map and checks screenshot road-color pixel count, attribution and no-navigation warning. Only report PASS after CI 43385141310 and 43385141410 conclude successfully with evidence.

Unfinished: durable approved storage/download URLs, exact size-pinned resumable download, legality/ODbL audit, physical Xiaomi, nationwide coverage pixel tests, POI labels and routing. Artifacts expire 2026-11-27. Main/PR/merge/release remain prohibited without owner's approval.


## Verified limited milestone
Run 43385141410 completed SUCCESS. Artifact: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410/artifacts/14017389861. The real 24-bac-ninh.pmtiles has exactly 11,049,420 bytes and sha256 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f. Android SAF imported it, offline style drew 266,196 road-color pixels, OSM attribution and no-navigation warning were exposed while airplane mode was enabled. This proves one regional renderer, not 35/35 maps or routing readiness.


## Build and regression completion
Source feature commit: 4a55a8734dff9f75b451cdb29414414c92a64104.
Standard Android CI 43385141310: all jobs SUCCESS, existing Hanoi real-road sample regression preserved (58,015 screenshot colored pixels).
Dedicated Bắc Ninh Shortbread Android CI 43385141410: all jobs SUCCESS, 11,049,420-byte SHA-pinned real archive via SAF DocumentsUI, airplane-mode 266,196 street pixels, OpenStreetMap ODbL attribution and no-navigation warning. Evidence expires 2026-11-27: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410/artifacts/14017389861.
Not a demonstration of 35 region files rendered on device; 35 pins/catalog tested, visual rendering verified for 1 only.


## 2026-11-13 catalog byte pin and multi-region tests
- New `VietnamRegionSpec.sizeBytes` pins 35 exact archives from independent verifier log 43379440781, ledger `tools/vietnam34/PACK_BYTES_VERIFIED.tsv`; all archive names, zooms, lengths and SHA hashes are checksummed during import, no remote catalog.
- SAF import of recognized file performs Android DocumentsUI size preflight if available, free-space check, then bounded streamed SHA/length validation before promotion; unrecognized imports remain untrusted.
- Real screenshots: 01 Hà Nội 157,112 road pixels and 79 Ho Chi Minh City 183,085, CI https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43387058322 success, evidence artifact 14019316787. With 24 Bắc Ninh old run, 3/35 verified. Source NOT approved for navigation/legal maritime lines.
- A fresh Bắc Ninh CI encountered emulator storage race before import (43387058086 FAIL). A separate retry helper was committed (a3e53366), new CI pending.


## CI retest and legacy renderer status
Real-region workflows with robust emulator storage helper are green: 43387711796 (Bắc Ninh 266,196 pixels) and 43387711798 (Hanoi 157,112, HCM 183,085). All were offline and SHA-verified, 3/35 Android real region proofs.
Unrelated full Android smoke 43387711858 failed at legacy 54KB Hanoi screenshot (0 road pixels on one early sample) after compile/JUnit, synthetic tile tests, HTTPS and SAF/recovery steps succeeded. A strict bounded retry was added in 59c2192; full Android retest 43388632601 pending, not green. Do not infer loss or success from earlier code without new emulator evidence.


## Pending overall Android regression
Despite three independently proven full region PMTiles renders (CI 43387711796 and 43387711798), general Android emulator regression 43388632601 failed on a single early synthetic local PMTiles screenshot showing zero target road-color pixels. Earlier 43387711858 failed on a single early old Hanoi pilot screenshot. Both checks now use bounded pixel retry without weakening assertions (59c2192, fddd00b5); new full run 43389395898 PENDING. Do not collapse these results into a claim that all CI is green.

## 2026-11-13 full real province coverage workflow
- Android full standard regression run 43389395898 SUCCESS (synthetic offline 39,985 pixels; small real Hanoi roads 58,015, verified HTTPS, SAF, recovery, UI).
- CI 43391332272 new `android-shortbread-coverage.yml` partitions **34 original provincial real PMTiles** into four independent Android emulator shards. Each must check source SHA/byte, interactive SAF import, stored SHA, airplane-mode actual vector screenshot with >=500 colored road pixels, UI OSM attribution/nonnavigation and no crashes. Evidence excludes redundant raw archive copies. Build passed, real region jobs pending as of checkpoint. Nationwide 556MB excluded and no 34/34 claim before results.

## 2026-11-13 34-province full Android QA harness status
- Four-shard shell/CI now compiled/linted and retesting in run 43393047973. Test loops require each of 34 exact source SHA/size, native SAF picker, stored original SHA, no-network screenshot >=500 road-color pixels, OSM credit and no-navigation UI; no silent skip of untested areas.
- Original 43391332272 partial run provided additional concrete 04 Cao Bằng (20,026 pixels) and 08 Tuyên Quang (32,639 pixels); total unique Android visual proofs now at least 5/35. Harness stdin bug and one launcher ANR caused overall fail. A follow-up 43392189567 encountered malformed generated bash, resolved with e015ebf2 plus mandatory pre-emulator bash -n. Results of active run 43393047973 must be read before extending coverage numerator.

## 2026-11-13 nationwide 34-province coverage attempt
- Full original 34-province emulator 43393047973: 28/34 per-province SHA+SAF+airplane-mode >=500 road-pixel screenshot PASS. Six archive-header-centered viewpoints yielded 0 pixels, causing strict CI FAIL, even though files were selected and imported. Do not interpret a center mismatch as incomplete underlying OSM file without additional evidence.
- `MainActivity.kt` visual-only camera fix ad7a8dde chooses referenced urban centers for six regions, maintaining original zoom 12.3, PMTiles SHA and navigation-disabled ODbL status. Repeat CI 43395054211 pending; no success claim yet.


## 2026-11-13 20:55 concrete Android coverage status
- Provincials: across 43393047973 and 43395054211, 32/34 individually proven offline Android; three completed new shards (25/25 PASS) verified previous blank visual fixes Huế, Khánh Hòa, An Giang and Cà Mau. One remaining shard cancelled on Android launcher ANR. Codes 31 and 48 yet to get successful urban screenshots. Do not claim all 34 despite successful SHA manifest.
- Targeted CI 43438393919 launched to retry 31 Hai Phong and 48 Da Nang with hard per-archive deadline and one ANR-only retry.
- First nationwide 556MB real PMTiles emulator test code a9cd6ce: SAF import and exact SHA/byte streamed in Python to avoid entire 556MB RAM duplication, then debug-only detailed Hanoi screenshot on national archive without changing normal map overview. CI 43438780443 pending, cannot claim 35/35. No automatic download/distribution.


## All 34 provincial real archive proof milestone
- Original source SHA+size verified and imported with SAF, airplane mode screenshots now evidenced for **all 34 provinces**, accumulated across independent CI workflow completions. Missing two 31 Hai Phong and 48 Da Nang closed by successful run 43438393919 (102,298 and 217,958 blue road-color pixels respectively). Not a release, province boundary or routing certification.
- Nationwide 00-viet-nam-all.pmtiles (556,229,998 bytes) still **not** proven Android until CI 43438780443 completes with streamed exact-size/hash import and real screenshot.


## Real national archive currently has only shared-storage SHA evidence
- Source original nationwide PMTiles 556,229,998 bytes, SHA 678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55 was byte-identical on emulator shared Downloads in 43438780443, but that run FAILED when Pixel Launcher ANR blocked Android SAF picker. Therefore 34/35 verified visual archives, NOT 35/35.
- Latest a9cd6ce adds streaming 1MiB readback hash in Python, no extra raw evidence; aa2f975 adds one launcher-ANR-only reattempt. Final stream SHA+airplane mode test run 43439851626 still pending. Normal user national overview unchanged; test uses debug-only Hanoi detail camera.


## 2026-11-13 all original 34 provinces passed one CI run, nationwide still gated
- Run 43438780272 SUCCESS, original 34 region exact SHA/size, real DocumentsUI, byte readback, airplane-mode MapLibre and road pixels, 9/9+9/9+8/8+8/8. Normal Android 43438780381 and HN/HCM 43438780281 separately SUCCESS.
- National 556,229,998-byte file full SHA verified on source and Android shared storage, but first SAF run blocked by Pixel Launcher, second failed `Not a PMTiles v3 archive` after entering DocumentsUI. Debug-only ContentResolver head/name probe 02b91f7 and national CI 43441211821 to distinguish selected URI stream from source; not yet run end-to-end.


## 2026-11-13 all 35 PMTiles real Android screenshot verification DONE
- 34 provinces successfully source SHA + native SAF imported and shown offline in 43438780272 all 4 emulator shards.
- National 00-viet-nam-all.pmtiles 556,229,998 bytes, SHA 678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55 independently SHA-verified through original, Android Downloads and **app-private streamed readback**: 43441211821 SUCCESS after launcher retry, 157,112 actual road-colored pixels at Hanoi in airplane mode. This is *one representative national archive viewport*, not a completeness proof of legal/admin areas, routes, or map labels.
- Extra run 43441211834 exposed DocumentsUI test XML collision; fix 1ce22829 stores UIAutomator snapshot only under /data/local/tmp and rejects non-PMTiles names; CI reruns pending.
- All 35 archives are RESEARCH PREVIEW / NOT APPROVED FOR NAVIGATION OR DISTRIBUTION. No permanent CDN, download/resume, UI labels or physical Xiaomi verification.


## CI after 35/35 genuine original visual proofs
- Full national pack import/sha/airplane screenshot success repeated 43454411510 (first SAF attempt, 556,229,998 byte stream, 157,112 road pixels), alongside successful Android full 43454411956 and Bac Ninh 43454411692.
- Multi-region Hanoi/HCM duplicate QA hit OS Pixel Launcher ANR 43454411531 before selecting the file. Retry mechanism now recovers exactly once for this class only (720efe42); pre-emulator shell/Python syntax gate e05b1ed. CI 43455374999 pending, province rerun 43454411678 ongoing.

## 2026-11-14 verification update, exact region-picker
- Android local Shortbread 1.0 style and frozen 35 SHA pins remained unchanged. All 34 original provincial archives reverified in [43569186171](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43569186171) SUCCESS (4/4 emulator shards plus build). Real national archive visual/stream proof 43441211821 and repeated 43569186220 SUCCESS.
- Region-list UI bug from simultaneous AlertDialog message and items was fixed in e5bc37de. Real user-facing catalog -> select Bac Ninh -> SAF SHA/size -> offline roads 266,196 pixels with OSM attribution and no-navigation warning PASS 43569253498. Direct catalog picker tested on Bac Ninh only, though original archives all have separate offline QA.
- New exact wrong-region negative test ebab5fc0 intentionally labels original Bac Ninh bytes as 01-ha-noi.pmtiles on emulator only. It must be refused for selected region 24 BEFORE storing any archive; then the correct original must still import and render. CI 43581299023 IN PROGRESS, do not claim test PASSED until job proof.
- Manual user-selected archives only; no unreviewed public map download, signed release, navigation or legal boundary approval.
