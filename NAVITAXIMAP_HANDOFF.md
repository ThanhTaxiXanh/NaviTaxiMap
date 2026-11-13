# BÀN GIAO NGỮ CẢNH DỰ ÁN NAVITAXIMAP

**Mốc bàn giao:** 13/11/2026, múi giờ Asia/Bangkok (UTC+7).  
**Mục đích:** Cho phép một cuộc trò chuyện lập trình mới tiếp tục dự án ngay, không hỏi lại thông tin đã thống nhất, không nhầm những gì đã làm với những gì còn dự kiến.

> ⚠️ Cảnh báo NaviTaxiMap: Cuộc trò chuyện đang có nguy cơ mất ngữ cảnh. Đề xuất tạo bản bàn giao trước khi tiếp tục.

## 1. Quy tắc bảo vệ ngữ cảnh (bắt buộc)

1. Trong các cuộc trò chuyện lập trình NaviTaxiMap, chủ động nhận diện các dấu hiệu lịch sử chat quá dài, mất nội dung tệp/CI/commit cũ, hoặc sắp phải chuyển hội thoại.
2. Cảnh báo sớm bằng đúng câu phía trên, đề xuất hoặc thực hiện ngay bản bàn giao nếu người dùng đã yêu cầu.
3. Luôn ghi đầy đủ: mục tiêu/kiến trúc, stack, những tính năng đã xác minh, tệp/nhánh/commit, dữ liệu bản đồ và giấy phép, quyết định kỹ thuật, lỗi/hạn chế, CI và bằng chứng, công việc kế tiếp và những yêu cầu không thể thay đổi.
4. Cung cấp văn bản có thể sao chép sang chat mới và ưu tiên duy trì bản Markdown trong GitHub nhánh phát triển. Sau mỗi cột mốc lớn phải cập nhật bản bàn giao.
5. Trong chat mới, **đọc bản bàn giao và kiểm tra lại HEAD thực tế**, không suy đoán rằng checkpoint còn mới. Tính liên tục được duy trì qua tệp và mã nguồn, không phụ thuộc vào bộ nhớ chat tự động.
6. Khi người dùng nhắn **“Tiếp tục”**, đó là lệnh triển khai bước kỹ thuật tiếp theo, cập nhật mã/kiểm thử/CI thực tế, không chỉ trình bày kế hoạch. Không hỏi lại những gì đã được trả lời, không tuyên bố hoàn thành nếu chưa có bằng chứng.

## 2. Mục tiêu, yêu cầu không được thay đổi

- Sản phẩm: **NaviTaxiMap**, Android dành cho tài xế taxi ở Việt Nam, hướng tới bản đồ, GPS và dẫn đường **offline** với chi phí dịch vụ bắt buộc bằng **0**.
- Phạm vi bản đồ: 34 tỉnh/thành theo cơ cấu có hiệu lực **01/07/2025** và **01 gói toàn Việt Nam**; tương lai tìm kiếm POI/địa chỉ, lập lộ trình, dẫn đường rẽ, TTS, giao thông, cảnh báo đường, nâng cấp 3D.
- **Chỉ phát triển trên** `develop/phase0-android-maplibre` tại GitHub [ThanhTaxiXanh/NaviTaxiMap](https://github.com/ThanhTaxiXanh/NaviTaxiMap).
- **Tuyệt đối không** sửa/push/merge vào `main`, tạo PR, tạo release, ký APK production hoặc đưa lên cửa hàng **khi chưa có đồng ý rõ ràng của chủ dự án**.
- `main` đã xác minh không đổi, SHA: `ea76d1c110bd32f3393be7c4dbf24f8424772a2a`.
- HEAD nhánh phát triển **trước khi commit bản bàn giao này**: `9d6e2171d76fd616e9940b8b1443892e4142446c`. Sau khi bàn giao được commit, phải lấy lại SHA mới.
- **Không coi OSM là nguồn pháp lý cho ranh giới/chủ quyền.** Các lớp địa giới, biển đảo, Hoàng Sa/Trường Sa phải được thẩm định nguồn riêng. Không vẽ hoặc công bố các ranh giới chưa được phê duyệt như dữ liệu chính thức.
- **Chưa được dùng NaviTaxiMap để dẫn đường khi lái xe.** Không đánh đồng PMTiles đường OSM thật với dữ liệu phân luồng, định tuyến, quyền lưu thông hoặc an toàn giao thông.

## 3. Kiến trúc và công nghệ đang sử dụng

| Thành phần | Cấu hình đã kiểm tra |
|---|---|
| Ngôn ngữ/ứng dụng | Kotlin Android; `applicationId=vn.navitaximap`; phiên bản prototype `0.1.0-dev` |
| Java/Gradle | JDK 17; Gradle 8.9; Android Gradle Plugin 8.7.3; Kotlin 2.2.10 |
| Android | `compileSdk=35`, `targetSdk=35`, `minSdk=26`; Android emulator API 34 |
| Render | MapLibre Native Android OpenGL ES `13.5.4`; local `pmtiles://file://`; OpenFreeMap cho prototype online |
| Định dạng offline | PMTiles v3 chứa MVT; bản 35 gói lớn là **Geofabrik Shortbread 1.0** (đòi hỏi style Shortbread tương thích), mẫu Hà Nội nhỏ dùng ba lớp road riêng |
| Vận hành dữ liệu | HTTPS nguồn ghim theo commit, stream/giới hạn kích thước/SHA-256/metadata, thư mục riêng app, Android DocumentsUI SAF |
| Dựng 35 gói | Python 3.12, Shapely 2.0.7, osmium-tool, go-pmtiles CLI v1.31.2, GitHub Actions |
| Kiểm thử | Kotlin JUnit, Python unittest, Android emulator screenshots + kiểm đếm pixel và đọc lại SHA qua `adb run-as` |

**Các phần mã liên quan:** `app/src/main/java/vn/navitaximap/MainActivity.kt`, `data/LocalPackStore.kt`, `data/PmtilesHeader.kt`, `data/DemoOfflinePack.kt`, `data/HanoiRoadPilot.kt`, `data/PilotCatalog.kt`, `data/CatalogPackStore.kt`, `data/PinnedHttpsPackDownloader.kt`, `ci/smoke-emulator.sh`, `ci/saf-picker-smoke.py`, `.github/workflows/android-debug.yml`.

## 4. Tính năng đã làm và kiểm chứng

- Khởi động ứng dụng Android, map online MapLibre/OpenFreeMap; quyền GPS qua Android LocationManager.
- Chế độ PMTiles offline tổng hợp, chứng minh đọc/hiển thị vector khi bật chế độ máy bay bằng ảnh pixel.
- SAF DocumentsUI thực tế: chọn tệp PMTiles từ Downloads, nhập app-private, đọc lại 1.268 byte và SHA-256; CI **43367761361 PASS**.
- Bộ quản lý PMTiles nhập tay: preflight header, SHA-256, stream, giới hạn dung lượng/bộ nhớ trống, metadata, xem lại/xóa; dọn staging bị gián đoạn; CI **43364225600 PASS**.
- **Mẫu đường Hà Nội thật**: vùng rất nhỏ, dữ liệu OSM API, 354 đối tượng highway, 21 ô vector, 53.956 byte, nguồn ODbL; hiển thị offline trên Android, 58.015 pixel màu đường được kiểm tra, có ghi công và cảnh báo *KHÔNG dẫn đường*; CI **43371354137 PASS**.
- **Danh mục một gói Hà Nội `v1`**: SHA-256, kích thước, giấy phép, hash nguồn, URL raw.githubusercontent.com gắn commit bất biến; cài từ APK hoặc người dùng tự xác nhận tải HTTPS, kiểm chứng source/metadata, rollback lỗi cập nhật, dọn orphan, hủy; UI và tải HTTPS thật trên Android; **23 kiểm thử Kotlin PASS, CI 43374393332 PASS**.
- Đây mới là danh mục **một gói mẫu nhỏ**, không phải danh mục 35 gói trong APK.

## 5. Thành tựu dữ liệu quan trọng nhất: 35 gói PMTiles thật

**ĐÃ SINH VÀ KIỂM TRA ĐỘC LẬP ĐỦ 35 TỆP**: 34 gói tỉnh/thành + 1 gói toàn quốc. Dữ liệu lấy từ Geofabrik OpenStreetMap experimental **Shortbread 1.0**, không có đường giả hoặc tệp placeholder rỗng.

- **Gói toàn Việt Nam:** `00-viet-nam-all.pmtiles` **556.229.998 byte**, **567.706 ô vector**, zoom **0–14**, SHA-256 `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`.
- **34 gói địa phương:** tổng **287.504.805 byte**, zoom **8–14**. Có 34 file và checksum riêng trong `tools/vietnam34/SHA256SUMS.txt` / `VIETNAM35_PACKS.md` / `manifest.json`.
- **Nguồn MBTiles:** `https://download.geofabrik.de/asia/vietnam-shortbread-1.0.mbtiles`, 686.698.496 byte, SHA-256 `492402e2ebc49516ba72ff90bb0212eba29fab3cc2a9addcb95bb39d6b1089d5`.
- **Nguồn phân vùng hành chính kỹ thuật:** Geofabrik PBF OSM 332.558.874 byte, SHA-256 `ddb9155f112af521d0674e4f40b87173f92954e5b8a3f71445d3008b506ef9ed` (33 đa giác); bổ sung quan hệ OSM đầy đủ cho Khánh Hòa `1887959`, 4.647.689 byte, SHA-256 `2b94689adcc3bf8fbfffe83387655421598b08dae3b32a908791e1d995c595b1` (đa giác thứ 34).
- 34 mã tỉnh/thành theo **Quyết định 19/2025/QĐ-TTg** từ 01/07/2025. Danh sách dữ liệu tại `tools/vietnam34/provinces_34.json`.
- **Phương pháp tách tỉnh:** chọn nguyên ô vector MVT có footprint giao đa giác OSM tỉnh (STRtree), **không cắt hình học đường theo biên tỉnh**. Ô gần biên có thể chứa đường tỉnh láng giềng; hình học OSM ven biển có mặt nước/đảo xa; **không phải sản phẩm bản đồ địa giới chính thức**.
- **Nguồn dữ liệu © OpenStreetMap contributors, ODbL 1.0;** cần kiểm toán nghĩa vụ phân phối/ghi công/share-alike trước phát hành; manifest đặt `navigation_approved=false`, `distribution_approved=false`.
- **GitHub Actions tạo dữ liệu 35/35 PASS:** [43378133931](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931).
- **GitHub Actions đọc lại độc lập 35 file, PMTiles header/SHA-256 PASS:** [43379440781](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43379440781).
- **Bộ 34 gói ZIP khoảng 288 MB:** [GitHub artifact 14015875271](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931/artifacts/14015875271).
- **Gói toàn quốc ZIP khoảng 556 MB:** [GitHub artifact 14015113186](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931/artifacts/14015113186).
- **Manifest/checksums:** [GitHub artifact 14015264272](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43379440781/artifacts/14015264272).
- **CẢNH BÁO HẾT HẠN:** Artifact PMTiles giữ **14 ngày** từ ngày build (khoảng 27/11/2026), manifest 30 ngày. Cần sao lưu sang nơi lưu bền hơn theo giấy phép phù hợp. Gói toàn quốc > 512 MiB, môi trường tải tệp ChatGPT có thể không nhận trực tiếp; dùng GitHub Actions hoặc chia tệp theo cách bảo toàn checksum.

### Tệp và workflow 35 gói

- `tools/vietnam34/provinces_34.json`: 34 mã, tên, tỉnh cấu thành và nguồn tham chiếu.
- `tools/vietnam34/audit_osm_boundaries.py`: phân tích multi-polygon OSM admin4.
- `tools/vietnam34/build_national_and_provinces.py`: cắt các ô vector và tạo 35 gói PMTiles.
- `tools/vietnam34/test_pipeline.py`: kiểm thử cấu trúc danh mục, tọa độ TMS và từ chối hình học thiếu.
- `tools/vietnam34/SHA256SUMS.txt`: hash của 35 tệp.
- `.github/workflows/vietnam34-boundary-audit.yml`: kiểm PBF OSM; **run 43376868151 PASS**.
- `.github/workflows/vietnam34-khanhhoa-audit.yml`: bổ sung Khánh Hòa; **run 43377276622 PASS**.
- `.github/workflows/vietnam34-build-35-once.yml`: xây bộ 35 file; **run 43378133931 PASS**.
- `.github/workflows/vietnam34-final-verify.yml`: kiểm lại 35/35; **run 43379440781 PASS**.
- `VIETNAM35_PACKS.md`: **tài liệu đầy đủ nhất** về dữ liệu, danh sách 35 file, số ô, SHA, hạn chế và đường tải.

## 6. Trạng thái thực sự, những gì CHƯA làm

**KHÔNG được nói đã hoàn thành app 35 tỉnh.** 35 gói PMTiles là **artifact dữ liệu riêng**, chưa được đưa vào danh mục tải trong APK; Android vẫn chỉ có gói Hà Nội nhỏ 54 KB. Chưa có style offline tương thích lớp MVT **Shortbread 1.0** cho cả nước, chưa kiểm pixel/khởi động/tải 35 gói trên emulator hoặc Xiaomi.

Chưa thực hiện: tải theo khu vực có resume/HTTP Range/chống đứt mạng và quota, cung cấp dữ liệu lâu dài ngoài GitHub Actions, ghim SHA/manifest 35 gói trên app, tùy chọn tải/xóa/giữ dữ liệu, bộ tìm đường offline, định tuyến rẽ, một chiều, biển báo, POI/search, TTS, bản đồ chủ quyền/hải đảo được thẩm định, 3D, traffic, thử thiết bị Xiaomi, đánh giá pin/bộ nhớ, ký và phát hành sản phẩm.

Cần chú ý: OpenFreeMap chỉ là online prototype. Không cào hàng loạt tile từ nhà cung cấp công cộng. Không dùng URL mutable hay tệp chưa kiểm hash làm nguồn dữ liệu được tin cậy. ODbL và kiểm toán biên giới phải được xử lý trước phát hành. GitHub Actions artifacts tạm thời, không thích hợp làm endpoint cập nhật lâu dài.

## 7. Lỗi đã giải quyết và rủi ro đang mở

**Đã giải quyết:** Kotlin metadata không hợp MapLibre (nâng Kotlin 2.2.10); SDK setup lỗi (dùng `setup-android@v4`); PMTiles offline synthetic screenshot trắng; Android picker test bấm nhầm nút preview thay vì title; Pillow thiếu trong CI (dùng parser PNG chuẩn); unit test hiểu nhầm `src/main/assets` là nhánh `main`; bản thử 35 gói thất bại vì Python 3.14 và Shapely 2.0.7 không có wheel (ghim Python 3.12); Geofabrik không dựng được đa giác Khánh Hòa, dùng API relation 1887959 đầy đủ. Mọi lỗi này có commit/CI ghi trong `ERROR_LOG.md`.

**Đang mở:** OSM admin4 không phải biên giới pháp lý; tách theo **cả ô** có tràn vùng; Shortbread chưa có style địa phương và Android pixel test; kích thước gói toàn quốc ~556 MB; vắng resumable download, lock liên tiến trình và fsync thư mục khi mất điện; API network+chính sách phân phối; kho dữ liệu bền vững; dữ liệu route/turn restrictions chưa có; chưa có kiểm thử Xiaomi thật, hiệu năng và ODbL release review.

## 8. Việc ưu tiên trong cuộc trò chuyện tiếp theo

**Việc 1:** Lấy HEAD branch và `main`, đọc `PROJECT_STATE.md`, `VIETNAM35_PACKS.md`, `TODO.md`, `DATA_SOURCES.md`, `ARCHITECTURE.md`, `ERROR_LOG.md`, file bàn giao này; xác minh CI 43378133931 và 43379440781, các artifact còn hạn.

**Việc 2:** Thiết kế/tích hợp **style MapLibre Shortbread 1.0 offline** cùng nguồn PMTiles local, tự cấp font/sprite nếu cần, hiển thị một gói tỉnh thật (ưu tiên Bắc Ninh hoặc Hà Nội) qua Android emulator trong airplane mode, kiểm screenshot pixel, kiểm attribution và cảnh báo không dẫn đường. Không đổi style đã xác minh của mẫu Hà Nội 54 KB nếu chưa có phương án hồi quy.

**Việc 3:** Chuẩn bị `catalog` 35 gói có tên/mã/kích thước/SHA-256/đường tải chính xác, kiểm được vùng và tính toàn vẹn trước/sau cài. Tránh nhúng toàn bộ 844 MB vào APK; cần luồng tải theo gói và lưu trữ có thời hạn dài (theo giấy phép), quota/độ trống bộ nhớ/cancel/resume.

**Việc 4:** Mở rộng kiểm thử khắc phục giữa chừng, đóng ứng dụng đột ngột, hết dung lượng/gián đoạn mạng, thay đổi dữ liệu, hồi quy 35 gói; kiểm toán giấy phép và nguồn pháp lý; kiểm Xiaomi thật khi có quyền truy cập.

**Quy tắc khi tiếp tục:** Mỗi bước cần commit thật lên **chỉ nhánh phát triển**, chạy CI, kiểm bằng chứng, cập nhật `PROJECT_STATE.md`, `PROGRESS.md`, `BUILD_STATUS.md`, `TODO.md`, `ERROR_LOG.md` và tệp bàn giao. Không hứa làm nền hoặc đánh dấu PASS trước khi chạy.

## 9. Các tài liệu và chứng cứ tra cứu nhanh

- **Trạng thái:** [PROJECT_STATE.md](https://github.com/ThanhTaxiXanh/NaviTaxiMap/blob/develop/phase0-android-maplibre/PROJECT_STATE.md)
- **35 gói:** [VIETNAM35_PACKS.md](https://github.com/ThanhTaxiXanh/NaviTaxiMap/blob/develop/phase0-android-maplibre/VIETNAM35_PACKS.md)
- **Việc chưa làm:** [TODO.md](https://github.com/ThanhTaxiXanh/NaviTaxiMap/blob/develop/phase0-android-maplibre/TODO.md)
- **Kiến trúc:** [ARCHITECTURE.md](https://github.com/ThanhTaxiXanh/NaviTaxiMap/blob/develop/phase0-android-maplibre/ARCHITECTURE.md)
- **Bản đồ Hà Nội:** `HANOI_OSM_PILOT.md`, `HANOI_OSM_PILOT_EVIDENCE.md`
- **Danh mục HTTPS:** `OFFLINE_MANAGER.md`, `CATALOG_PACK_EVIDENCE.md`
- **Dữ liệu/quyền sử dụng:** `DATA_SOURCES.md`, `LICENSES.md`
- **Bằng chứng build và sự cố:** `BUILD_STATUS.md`, `ERROR_LOG.md`, `CHANGELOG.md`
- **Các mốc CI PASS:** Android danh mục 43374393332; Android debug mới nhất 43379440694; dựng 35 gói 43378133931; kiểm SHA 35 gói 43379440781.

## 10. NỘI DUNG COPY SANG CUỘC TRÒ CHUYỆN MỚI

> Tôi tiếp tục phát triển dự án **NaviTaxiMap** từ bản bàn giao ngày 13/11/2026. Hãy đọc trước tệp `NaviTaxiMap_BAN_GIAO_2026-11-13.md` tại nhánh `develop/phase0-android-maplibre` của `ThanhTaxiXanh/NaviTaxiMap` cùng `PROJECT_STATE.md`, `VIETNAM35_PACKS.md`, `TODO.md`, `ARCHITECTURE.md`. Kiểm tra HEAD thực tế và CI. **Không sửa `main`, không PR, merge hoặc phát hành nếu chưa được tôi cho phép.** Hiện có 35 PMTiles OSM thật (34 tỉnh/thành + 1 toàn Việt Nam), CI tạo và kiểm tra SHA-256 **PASS**, nhưng **chưa được tích hợp vào Android**, chưa có style Shortbread offline, không phải dữ liệu dẫn đường hoặc địa giới pháp lý. Hãy triển khai ngay bước kỹ thuật kế tiếp: hiển thị một gói tỉnh thật trong Android offline bằng style Shortbread phù hợp, kiểm thử emulator, rồi tiếp tục xây danh mục 35 gói. Sau các bước quan trọng, chủ động cảnh báo mất ngữ cảnh và cập nhật bản bàn giao theo quy tắc đã thống nhất.

---

**Ghi chú:** Bản này là checkpoint theo thời điểm, không thay thế GitHub HEAD/CI hiện tại. Nó không tự cấp quyền phát hành hoặc phê duyệt nội dung dữ liệu bản đồ.

## Tiếp tục sau bàn giao 13/11/2026: Android Shortbread
- HEAD đầu phiên: eba19b95b224194c641e529dbff399e877245f35; main không đổi ea76d1c110bd32f3393be7c4dbf24f8424772a2a.
- CI cũ được kiểm tra: tạo 35 gói 43378133931 SUCCESS, xác minh độc lập SHA 43379440781 SUCCESS, Android 43379440694 SUCCESS. PMTiles artifact hết hạn 27/11/2026, manifest 13/12/2026.
- Commit 4a55a873 thêm danh mục 35 SHA ghim, style offline Shortbread 1.0 không font/sprite/địa giới, UI xem sau nhập SAF, bộ test Kotlin/Python và QA emulator Bắc Ninh bằng PMTiles thật. Mẫu Hà Nội cũ được giữ.
- CI feature mới 43385141310 (Android) và 43385141410 (Bắc Ninh offline pixel) chưa có kết quả PASS tại checkpoint. Phải kiểm lại trạng thái và sửa nếu thất bại.
- Chưa có kho HTTPS bền vững, tải 35 gói từ app, Range resume, phê duyệt giấy phép, dẫn đường, ranh giới pháp lý hay thử Xiaomi. Không sửa main, PR, merge, release.


## Bổ sung bằng chứng thực: Shortbread Bắc Ninh 13/11/2026
- **BẮC NINH OFFLINE ANDROID VERIFIED**, CI https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410 SUCCESS; artifact chứng cứ https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410/artifacts/14017389861.
- Tệp gốc 24-bac-ninh.pmtiles: 11.049.420 byte, SHA-256 6edc513adf8b8ad467982e818dc3450fcbbb14076ca2bf4bac352f1b9ca9ee7f. Qua DocumentsUI SAF, kiểm lại byte/SHA và render trong airplane mode; screenshot có 266.196 pixel màu đường Shortbread. Có ghi công OSM/ODbL và KHÔNG dẫn đường.
- Đây là **1/35 gói đã kiểm pixel trên Android**, không phải 35/35. Danh mục SHA 35 gói đã tích hợp nhưng chưa có luồng tải 35 HTTPS; style không font/sprite, không có lớp địa giới pháp lý. Android hồi quy tổng thể 43385141310 còn chờ ở thời điểm bổ sung.


## CHECKPOINT HOÀN TẤT KIỂM THỬ 13/11/2026
- HEAD code tính năng 4a55a8734dff9f75b451cdb29414414c92a64104; các commit sau là cập nhật tài liệu [skip ci]. Toàn bộ 2 workflow của commit tính năng đã SUCCESS.
- Android thường: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141310 PASS (biên dịch, JUnit, Python, emulator; mẫu Hà Nội vẫn 58.015 pixel).
- Bắc Ninh thật: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43385141410 PASS (SAF, SHA-256, airplane mode, Shortbread 1.0 offline 266.196 pixel); ảnh/log trong artifact 14017389861.
- Danh mục 35 tên/SHA trong Android đã được kiểm thử trùng tools/vietnam34/SHA256SUMS.txt; CHỈ BẮC NINH là gói Shortbread mới đã kiểm ảnh Android. KHÔNG được gọi 35/35 APK đã hiển thị, KHÔNG có tải trực tiếp 35 gói hay dẫn đường. Main SHA vẫn phải kiểm mỗi phiên, không push main/PR/merge/release.
- Việc tiếp theo: sao lưu artifact trước 27/11/2026, thiết kế hosting miễn phí bền vững/được phép, exact size + verified download/Range resume; kiểm các gói khác và Xiaomi, ODbL và ranh giới theo nguồn pháp lý.


## CHECKPOINT PHÁT TRIỂN TIẾP THEO (13/11/2026): ghim dung lượng + kiểm nhiều tỉnh
- Đầu phiên, HEAD `91a891079f867bdc0418e538c958455c297665d9`; `main` giữ `ea76d1c110bd32f3393be7c4dbf24f8424772a2a`.
- Commit `7d747d1c64704b4143e4358c00d75724afaea591` bổ sung `sizeBytes` cho 35 entry trong `Vietnam35Catalog`, kiểm preflight DocumentsUI / đủ trống / SHA / size giới hạn streaming trước khi ghi metadata. Tệp `tools/vietnam34/PACK_BYTES_VERIFIED.tsv` lấy từ 35 dòng log kiểm chứng độc lập run 43379440781. Tổng 34 gói = 287.504.805 byte, gói VN = 556.229.998 byte.
- **CI 43387058322 SUCCESS:** Hà Nội `01-ha-noi.pmtiles` 17.562.631 byte/157.112 pixel đường; Hồ Chí Minh `79-ho-chi-minh.pmtiles` 18.217.716 byte/183.085 pixel. SHA và nhập SAF thật, bản đồ Shortbread khi máy bay, cảnh báo KHÔNG dẫn đường. Chứng cứ artifact 14019316787. Cộng Bắc Ninh đã có trước: **3/35 được kiểm pixel trên emulator**.
- **CI 43387058086 FAIL** do tệp không đẩy được vào `/sdcard/Download` trước khi bộ nhớ emulator sẵn sàng; không phải lỗi pixel. Commit `a3e53366e622f3d856bbf17f627edd86aee9ab72` thêm helper retry + kiểm byte, CI mới 43387711796/43387711798/43387711858 đang chạy, phải đối chiếu sau.
- Vẫn CHƯA có kho lưu bền vững được phê duyệt, tải 35 gói HTTPS trong Android, resume, điều hướng, Xiaomi, ODbL phân phối, đường biên pháp lý; không push main, không PR, merge, release.


## Bổ sung kiểm chứng mới 13/11/2026
- Commit 7d747d1 ghim 35 dung lượng byte từ log kiểm SHA; CI Android Kotlin/Python compile của feature PASS.
- Commit a3e53366 sửa race `adb push` ngay sau emulator boot, được chứng minh bằng 2 CI SUCCESS: [Bắc Ninh 43387711796](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43387711796) (266.196 pixel) và [Hà Nội + TP.HCM 43387711798](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43387711798) (157.112 + 183.085 pixel). Đủ **3/35** gói bản đồ lớn kiểm pixel Android; không phải 35/35.
- CI full Android 43387711858 build/JVM PASS nhưng emulator FAIL vì ảnh mẫu Hà Nội 54KB đếm 0 pixel (dù trước đó đã có mốc 58.015 pixel PASS). KHÔNG ghi full CI PASS. Commit 59c2192 thêm chờ/chụp lại có giới hạn, giữ nguyên ngưỡng pixel; CI mới 43388632601 đang chạy.
- Chưa có kho tải hợp pháp bền vững, resumable transfer, nation PMTiles 556MB kiểm emulator, 32 gói khác, Xiaomi, routing hoặc địa giới pháp lý. Giữ main/PR/merge/release không thay đổi.


## CHECKPOINT MỚI 13/11/2026: CI hồi quy pixel chưa đóng
- Các CI Shortbread lớn 43387711796 (Bắc Ninh) và 43387711798 (Hà Nội + Hồ Chí Minh) vẫn SUCCESS với 266.196 / 157.112 / 183.085 pixel đường, bảo toàn 3/35 gói đã chứng minh.
- Android tổng thể 43387711858 FAIL ảnh mẫu Hà Nội 54KB 0 pixel; thử lại 43388632601 FAIL sớm ở ảnh PMTiles tổng hợp 0 pixel. Không khẳng định Android full CI xanh.
- Các commit 59c2192 (real Hanoi) và fddd00b5 (synthetic) sửa đồng bộ CI: chờ thêm/chụp lại nhiều lần, giữ nguyên ngưỡng pixel, không bỏ kiểm thử. Đang chờ run 43389395898. Nếu tiếp tục chat mới phải kiểm run này và sửa/khảo sát renderer nếu còn lỗi.
- Không sửa main/PR/merge/release; không có 35 gói tải trong APK hay dữ liệu dẫn đường.

## CHECKPOINT SAU 15:10 ICT 13/11/2026: hồi quy full đã XANH, tiến hành QA 34 tỉnh
- **Android full CI 43389395898 SUCCESS** ở code fddd00b5, cả biên dịch lẫn emulator. PMTiles tổng hợp 39.985 pixel; đường OSM Hà Nội 54KB có 58.015 pixel; các bước mạng HTTPS ghim hash, SAF, phục hồi, versioned catalog đều PASS. Đóng lỗi ảnh 0 pixel do chờ screenshot không đủ (các lỗi trước được giữ log).
- Bắt đầu commit **32ff213997e35a6a1a8c844083ee6c56dd7bc1b2**: bốn nhóm emulator thực nghiệm 34 tệp PMTiles cấp tỉnh, dùng bản gốc artifact run 43378133931 và ledger SHA+byte, nhập SAF thật, bật chế độ máy bay, đếm pixel, lưu PNG/log; không phát hành dữ liệu. Workflow **43391332272**, build chung PASS, 4 jobs tỉnh đang chạy tại checkpoint; **CHƯA chứng minh 34/34 Android**. Trước đó chỉ **3/35** tệp lớn đã được kiểm ảnh (Bắc Ninh, Hà Nội, HCM).
- Run Android hồi quy mới 43391332249 và Bắc Ninh 43391332168 khởi động theo commit, chưa xác minh kết luận. Gói toàn VN 556.229.998 byte chưa test thiết bị; không có HTTPS URL lâu dài, Range resume, Xiaomi, điều hướng hay ranh giới phê duyệt. Main vẫn giữ an toàn; mỗi phiên phải kiểm lại HEAD.

## BÀN GIAO PHẦN MỞ RỘNG 34 TỈNH (15:27 13/11/2026)
- **Mốc CI hồi quy toàn bộ 43389395898 SUCCESS** được xác minh đầu phiên; 43391332249 tiếp tục SUCCESS và 43391332168 Bắc Ninh SUCCESS ở commit mới.
- Commit 32ff2139 tạo QA 34 tỉnh qua 4 máy ảo song song, nhập file thật SAF, SHA/byte ghim, máy bay, pixel ảnh. Lượt 43391332272 FAIL do bash while bị adb đọc mất stdin sau tỉnh đầu tiên. Tuy vậy **Cao Bằng 04 PASS 20.026 pixel** và **Tuyên Quang 08 PASS 32.639 pixel**, Hà Nội 01 vẫn PASS 157.112 pixel. Điện Biên bị Pixel Launcher ANR trước SAF. Tính tổng bằng chứng cá thể có **5/35 bản đồ lớn đã QA Android**, không phải 34/35.
- Commit 781644d sửa đọc file descriptor 3 nhưng JS template khi sửa đã vô tình biến dòng shell `$'\\t'` thành file lỗi cú pháp. CI 43392189567 FAIL sớm cả 4 emulator ở lỗi shell line 65, không chứng minh dữ liệu hỏng.
- Commit **e015ebf22f042c8a7d548983bc20c1ed4510e4b5** viết lại đúng vòng lặp, thêm `bash -n` ngay trước build/test để chặn lỗi cú pháp. CI **43393047973** compile/script lint SUCCESS và đang thử cả bốn nhóm tại checkpoint. Phải đọc từng `PROVINCE_PASS` và tổng `COVERAGE_SHARD_END`, xử lý FAIL thực tế; tuyệt đối không gọi 34/34 trước khi xong.
- `main` vẫn giữ ea76d1c110bd32f3393be7c4dbf24f8424772a2a (kiểm lại lần tới), chỉ nhánh develop được update. PMTiles VN 556MB chưa test Android, không có host lâu dài/Range resume/Xiaomi/routing/địa giới pháp lý.

## CHECKPOINT 15:40 ICT 13/11/2026: 28/34 ĐÃ KIỂM ANDROID, 6 CHỜ THỬ LẠI
- CI **43393047973** thực sự xử lý đủ 34 tỉnh, file gốc SHA/size, nhập SAF, vẽ trong chế độ máy bay, ngưỡng >=500 pixel. 28 tỉnh hoàn tất PASS. **6 tỉnh chụp 0 pixel** ở góc nhìn mặc định zoom12.3 từ center header: `31-hai-phong`, `46-hue`, `48-da-nang`, `56-khanh-hoa`, `91-an-giang`, `96-ca-mau`. Toàn bộ workflow kết luận FAIL. Không được viết 34/34 đã xong.
- Giả thuyết: tâm bbox vùng rộng có đảo/biển/vùng thưa đường. Commit **ad7a8dde9a39a2338ffeace647de40c65c4ffb02** sửa `MainActivity.kt` mở sáu gói tại điểm đô thị đã đối chiếu từ UNGEGN/GeoNames/UNT: Hải Phòng, Huế, Đà Nẵng, Nha Trang, Rạch Giá, Cà Mau. Không thay đổi PMTiles hay bản đồ địa giới, không nới lỏng pixel.
- CI tái kiểm đủ 34: **43395054211** đang chạy; Android hồi quy 43395054123, Bắc Ninh 43395054234, Hà Nội/TP.HCM 43395054268 cũng chưa có kết luận ở checkpoint này. **28/35** tổng gói lớn có bằng chứng ảnh Android hiện tại, gói VN 556MB chưa kiểm. Chỉ nhánh develop, main an toàn, không PR/merge/release.


## CHECKPOINT 13/11/2026 20:55 ICT: 32/34 tỉnh có bằng chứng Android, kiểm riêng 2 tỉnh và gói toàn quốc
- HEAD khi bắt đầu đợt `df80f47b2e50767a00fced4ad8177a48cbfe9ac2`; `main` vẫn `ea76d1c110bd32f3393be7c4dbf24f8424772a2a`. Chỉ phát triển trên `develop/phase0-android-maplibre`.
- CI [43395054211](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43395054211) **CANCELLED**, không phải SUCCESS: shard 1/2/3 đã PASS tổng cộng 25/25 tỉnh, gồm Huế/Khánh Hòa/An Giang/Cà Mau từng thất bại pixel=0 trước đó, nay ảnh thật PASS sau chỉnh tâm xem. Shard 0 mắc Pixel Launcher ANR, bị hủy sau quá lâu và không nghiệm thu 2 vùng còn thiếu (31 Hải Phòng và 48 Đà Nẵng). Shard 0 của run 43393047973 đã PASS 7/9 vùng còn lại. Bằng chứng kiểm Android theo tỉnh qua nhiều run = **32/34**; không được gọi toàn bộ workflow 34/34 đã PASS.
- CI [43396019905](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43396019905) FAIL do Pixel Launcher ANR dù `ANDROID_DOWNLOAD_SHA256_OK` của Hà Nội đã đúng. CI Android tổng thể [43396020034](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43396020034) SUCCESS cả build/emulator. Không suy đoán sai SHA hay bản đồ gốc hỏng.
- Commit **af38142648ab81c7835d8b242b2396c2cd504ef4** thêm workflow/QA chỉ hai tỉnh 31+48, script có `timeout` 180 giây mỗi tệp, phục hồi launcher ANR tối đa 1 lần, SHA+SAF+offline pixel bắt buộc. CI [43438393919](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438393919) đang chạy tại checkpoint.
- Commit **a9cd6ce0fdeeb4a66286ade8da2a60a621d95cc7** thêm đường QA gói **toàn Việt Nam thật 556.229.998 byte**, SHA `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`: `ci/saf-picker-smoke.py` kiểm readback SHA theo luồng (không buffer >556MB), giao diện nhập cho phép chờ lâu, ảnh đường ở Hà Nội dùng debug-only camera trên gói VN, không thay góc nhìn bản đồ quốc gia bình thường. Workflow [43438780443](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438780443) đang chạy, **chưa xác minh thành công**.
- Chưa có host bền vững được duyệt, Range resume, Xiaomi, kiểm ODbL phân phối, dữ liệu dẫn đường hoặc kiểm soát địa giới theo nguồn pháp lý. Artifacts gốc 34 tỉnh và VN hết hạn 27/11/2026. KHÔNG PR/merge/main/release.


## CHECKPOINT MỚI 13/11/2026 21:xx ICT: 34/34 PMTiles tỉnh/thành đã có bằng chứng offline Android
- CI mục tiêu **43438393919 SUCCESS**, commit `af38142648ab81c7835d8b242b2396c2cd504ef4`, hai tệp gốc qua SAF + SHA byte chính xác + airplane-mode + pixel thực: `31-hai-phong.pmtiles` 7.279.113 byte, SHA `e8de939a...`, **102.298 pixel** (lần đầu Pixel Launcher ANR, retry một lần PASS); `48-da-nang.pmtiles` 11.974.113 byte, SHA `5af82e96...`, **217.958 pixel** (PASS lần đầu). Log kết thúc `TARGETED_COVERAGE_RESULT passed=2 expected=2`.
- Cộng 32 tỉnh/thành đã được xác minh trong các CI 43393047973 + 43395054211, nay **34/34 gói tỉnh/thành có bằng chứng ảnh Android offline** (qua nhiều workflow, không có một full 34 workflow duy nhất SUCCESS). Đây chỉ là hiển thị dữ liệu OSM Shortbread, KHÔNG có điều hướng và không xác nhận địa giới pháp lý.
- Tại checkpoint gói thứ 35 **toàn Việt Nam 556.229.998 byte** CI `43438780443` đang chạy (code a9cd6ce0); **KHÔNG ghi 35/35 PASS** khi chưa có log cuối. Các workflow mới đồng thời có thể FAIL vì Pixel Launcher ANR không ổn định, phải phân biệt lỗi môi trường và dữ liệu. `main` không sửa, không PR/merge/release.


## CHECKPOINT ĐỢT 20:55 ICT 13/11/2026: gói quốc gia qua SHA trên Android, chưa qua SAF/ảnh
- **Đủ 34/34 tỉnh/thành có kiểm chứng offline qua các run khác nhau**. Hai vùng cuối 31 Hải Phòng và 48 Đà Nẵng PASS ở CI [43438393919](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438393919), 102.298 và 217.958 pixel, SHA + SAF hợp lệ; Hải Phòng cần phục hồi Launcher ANR một lần. Một run tổng hợp 34 shard chưa từng đạt all-success do launcher treo, không ngụ ý 34/34 dữ liệu sai.
- **Hà Nội+HCM CI [43438780281](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438780281) SUCCESS**: thêm kiểm SHA ngay trên Android Downloads, sau đó SAF/ảnh: 157.112 / 183.085 pixel. Android thường CI [43438780381](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438780381) đã compile PASS, emulator đang chạy tại checkpoint; Bắc Ninh CI 43438780338 FAIL do Pixel Launcher ANR.
- **Gói toàn Việt Nam 00-viet-nam-all.pmtiles SHA 678704befd7b4b...**, kích thước gốc **556.229.998 byte**: nguồn chuẩn và bản sao trong Android Downloads đều SHA đúng, log CI [43438780443](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438780443). Job **FAIL trước khi chọn file SAF** vì hộp thoại Android `Pixel Launcher isn't responding`; KHÔNG được gọi gói quốc gia đã nhập/hiển thị.
- Commit **aa2f975db49413451d58a5c9836f9783a19d1984** sửa test quốc gia chỉ thử lại một lần sau Launcher ANR; không nới lỏng hash, byte, OSM UI, pixel hoặc bản đồ. CI [43439851626](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43439851626) đang chạy tại checkpoint; phải đọc log `SAF_STREAM_SHA256_OK` + `NATIONAL_556MB_PM_TILES_OFFLINE_PASS` trước khi nâng lên 35/35.
- Đợt này vẫn không cấp quyền phân phối, không mở kho tải bền vững, không kiểm toàn bộ tính chính xác địa giới, không dẫn đường, không thử Xiaomi. Nhánh main vẫn an toàn; không PR/merge/release.


## CHECKPOINT QUAN TRỌNG 13/11/2026: 34/34 CẢ WORKFLOW XANH, quốc gia đang điều tra DocumentsUI
- **CI [43438780272](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438780272) SUCCESS cả 5 jobs**, tệp gốc OSM PMTiles của đủ 34 tỉnh đã qua đúng SHA/kích thước, nhập DocumentsUI/SAF, chụp ảnh offline >=500 pixel và cảnh báo © OSM, KHÔNG dẫn đường. Chi tiết từng shard: 0=9/9, 1=9/9, 2=8/8, 3=8/8; failed=0. Đây là lần đầu full workflow thực sự xanh, không còn chỉ cộng các workflow.
- CI Hà Nội/TP.HCM **43438780281 SUCCESS** cả hai SHA trên Android Downloads, SAF, screenshot 157.112 / 183.085 pixel; Android thường **43438780381 SUCCESS**. Một job Bắc Ninh riêng 43438780338 FAIL vì Pixel Launcher ANR ở emulator, không thay đổi chứng cứ 34/34.
- Gói quốc gia `00-viet-nam-all.pmtiles` 556.229.998 byte, SHA `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`: CI 43438780443 xác nhận SHA nguồn + Android Downloads rồi FAIL do Pixel Launcher; CI thử lại **43439851626** cũng xác nhận SHA trên Android Downloads, nhưng khi chọn SAF báo **`Not a PMTiles v3 archive`**. Giải thích chưa rõ: workflow xác minh đầu vào 43379440781 đã xác nhận header `PMTiles\\x03` và toàn bộ checksum; có thể DocumentsUI truyền URI/stream khác. CHƯA có `SAF_STREAM_SHA256_OK` hay ảnh quốc gia PASS, vì vậy **không gọi 35/35**.
- Commit **02b91f7adf0fbcc26848c501a3e369db6bf1b196** bổ sung *debug-only* `SAF_SELECTED_STREAM name=... pinned=... headerBytes=... isPmtilesV3=...` trên luồng ContentResolver thực tế và log bằng chứng thất bại trong `ci/saf-picker-smoke.py`. Không ghi URI/private byte contents. CI quốc gia chẩn đoán **43441211821** đang chạy tại checkpoint; đọc log để xác minh nguồn lỗi rồi mới sửa tiếp.
- Không động `main`, không PR/merge/release, không cấp quyền phân phối hay dẫn đường; chưa có host lâu dài hoặc resume, Xiaomi, đối chiếu địa giới pháp lý. Artifacts đầu vào hết hạn 27/11/2026.


## CHECKPOINT 13/11/2026 22:34 ICT: HOÀN TẤT KIỂM HIỂN THỊ OFFLINE **35/35** TỆP GỐC
- **Bằng chứng mới quyết định:** GitHub Actions [43441211821](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43441211821) **SUCCESS**, run Android debug emulator gói thật `00-viet-nam-all.pmtiles` **556.229.998 byte**. File nguồn và /sdcard/Download đúng SHA-256 `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`; sau một lần phục hồi Pixel Launcher ANR, chọn file **thật qua DocumentsUI/SAF**, đọc lại đủ 556.229.998 byte từ bộ nhớ riêng theo luồng và `SAF_STREAM_SHA256_OK` đúng SHA; tắt dữ liệu/WiFi/chế độ máy bay, xem đường Hà Nội bằng chính gói VN: `SHORTBREAD_ROAD_PIXELS 157112`; `NATIONAL_556MB_PM_TILES_OFFLINE_PASS`. Đây là bằng chứng gói thứ 35, không phải định tuyến toàn quốc hay chuẩn pháp lý.
- Trước đó [43438780272](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43438780272) cũng **SUCCESS** ở CẢ NĂM JOB, đủ 34 gói tỉnh/thành thực tế: 9/9 + 9/9 + 8/8 + 8/8, SHA/SAF/ảnh pixel offline. **Tổng 35/35 gói khác nhau đã hiển thị offline Android emulator** theo hai workflow.
- CI [43441211805](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43441211805) Android tổng thể SUCCESS; Bắc Ninh [43441211810](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43441211810) SUCCESS và Hà Nội+HCM [43441211781](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43441211781) SUCCESS. Một workflow kiểm lại 34 gói trên cùng SHA [43441211834](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43441211834) **FAIL 1/34** do SAF vô tình nhập `navitaxi-picker.xml` thay cho 08 Tuyên Quang: log debug xác nhận `SAF_SELECTED_STREAM name=navitaxi-picker.xml pinned=none ... isPmtilesV3=false`. Đây là tệp uiautomator ghi vào /sdcard, xuất hiện trong DocumentsUI Recents, không phải dữ liệu 08 bị hỏng; gói 08 đã PASS ở run 43438780272.
- **Commit sửa mới `1ce228290ac4f68d122845e633691fecc291d369`**: đẩy XML nội bộ uiautomator từ /sdcard sang `/data/local/tmp/navitaxi-picker.xml`, xoá legacy XML khỏi /sdcard trước picker, ứng dụng từ chối tài liệu khác đuôi `.pmtiles` trước khi đọc, debug log không ghi tên riêng tệp chưa xác thực, thêm Python unittest mô phỏng snapshot/đường dẫn. CI mới **43454411956** Android, **43454411510** quốc gia, **43454411678** 34 tỉnh, **43454411692** Bắc Ninh, **43454411531** Hà Nội/TP.HCM: **đang chạy** ở checkpoint; không ghi PASS của commit mới trước kết luận.
- **Không thay đổi `main`** (kiểm lúc bắt đầu `ea76d1c110bd32f3393be7c4dbf24f8424772a2a`), không PR/merge/release, không bật dẫn đường; 35/35 chỉ chứng minh nguồn OSM Shortbread đúng SHA và xem điểm đại diện trên emulator, không chứng minh 35/35 hành trình/địa giới pháp lý. Chưa có hosting bền vững, tải có resume, Xiaomi. Artifact nguồn sẽ hết hạn 27/11/2026.


## BỔ SUNG SAU 22:34 ICT 13/11/2026: sửa DocumentsUI XML, thử lại 35 gói
- **Gói quốc gia 556MB ĐÃ PASS LẦN NỮA** tại CI [43454411510](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43454411510) của mã `1ce22829`: lần đầu SAF thành công, đọc lại đủ **556.229.998 byte** SHA `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`, khi offline có **157.112 pixel đường**. Không dựa vào lần PASS cũ duy nhất.
- CI Android đầy đủ [43454411956](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43454411956) SUCCESS cả Kotlin/Python tests lẫn emulator, và Bắc Ninh [43454411692](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43454411692) SUCCESS trên commit `1ce22829`. CI 34 tỉnh [43454411678](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43454411678) build PASS nhưng bốn emulator ĐANG CHẠY ở checkpoint; không ghi kết quả cuối trước khi kết thúc.
- CI Hà Nội/TP.HCM [43454411531](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43454411531) **FAIL do Pixel Launcher ANR trước khi app mở**, source file Android Downloads đúng SHA, không phải do XML test hay dữ liệu PMTiles. Commit **720efe42f28a9c5cc0a58fe0cd8c6f391a4108b9** bổ sung 1 lần phục hồi Launcher ANR trong `ci/shortbread-multi-region.sh`, chỉ khi gặp thông báo hệ thống rõ ràng; thất bại nội dung file/SHA/pixel vẫn báo FAIL. Commit **e05b1edd9dc5a9633ae7866d1ede9e9f73726925** thêm gate `bash -n` + Python compile vào workflow. Rerun multi [43455374999](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43455374999) và Android full [43455375003](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43455375003) đang chạy.
- **35/35 gói gốc đã có bằng chứng thực tế**: 34 gói tỉnh từ workflow toàn xanh 43438780272, 1 gói VN từ 43441211821 và 43454411510. Không suy diễn hoàn thiện hệ thống điều hướng, dữ liệu địa giới pháp lý, phân phối hoặc trình quản lý tải trong app. `main` không được sửa; không PR/merge/release.


## CHECKPOINT 13/11/2026 khoảng 22:55 ICT: đóng CI cũ, tạo lõi tải Range an toàn CHƯA KÍCH HOẠT
- **Toàn bộ CI phiên trước đã kết luận SUCCESS**: full 34 gói qua 43454411678 (5/5 jobs); gói quốc gia 556.229.998 byte 43454411510; Bắc Ninh 43454411692; Hà Nội + TP.HCM 43455502132; Android build+emulator 43455502057 (đều có log GitHub). Mốc 35/35 thực tế giữ nguyên; tránh nhầm với bản đồ dẫn đường hay phê duyệt tái phân phối.
- Commit `389d71bf5296081aa50fb2a19adc89519284cc7e` thêm `PinnedHttpsRangeTransfer.kt` và 3 JUnit tests với HTTP Range giả: chỉ HTTPS và đúng hostname được xét duyệt, không redirect, `206`/Content-Range/length đúng, ghi file .partial riêng để resume, băm SHA-256 và `LocalPackStore` nhập atomic sau xác minh; lỗi mismatch fail closed. **Cờ `Vietnam35Catalog.distributionApproved=false`, không server/URL/manifest được duyệt và không kết nối vào UI**, nên không có chức năng tải dữ liệu hoạt động/không phát hành.
- Tài liệu kỹ thuật mới: `PACK_DISTRIBUTION_GATE.md` về bảo vệ dữ liệu, dung lượng tải + bản sao (gói 556 MB cần khoảng 1,13 GB), điều kiện pháp lý/vận hành và các bài test. CI Android mới [43458014933](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43458014933) đang chạy ở checkpoint này; KHÔNG ghi PASS khi chưa có log.
- Bảo vệ `main` nguyên `ea76d1c110bd32f3393be7c4dbf24f8424772a2a`, chỉ thay đổi `develop/phase0-android-maplibre`; không PR/merge/release, không tự duyệt nguồn OSM/legal/routing.
