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
