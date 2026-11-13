# NaviTaxiMap

Android/Kotlin, MapLibre Native, ưu tiên dữ liệu OSM và offline, không dùng Google Maps SDK.

**Trạng thái:** Bộ khung đang phát triển. Chưa có sản phẩm đủ điều kiện dẫn đường thực tế.

## Nhánh và an toàn phát hành
- Phát triển tại `develop/phase0-android-maplibre`.
- Không merge/pull request vào `main` khi chưa có sự đồng ý của chủ dự án.
- GitHub Actions chỉ build `app-debug.apk` trên nhánh `develop/**`; không phát hành.

## Chạy và kiểm thử
Máy phát triển cần Android SDK 35, JDK 17 và Gradle 8.9.

```sh
gradle :app:testDebugUnitTest :app:assembleDebug
python3 -m unittest discover -s tools/vietnam-sovereignty-map/tests -v
```

Ứng dụng dùng MapLibre Android OpenGL ES và **demo style công khai chỉ để thử nghiệm**:
`https://demotiles.maplibre.org/style.json`. Khi không có kết nối, dữ liệu bản đồ demo không đảm bảo xuất hiện.
Không được xem demo style là nguồn production hoặc offline. Cần phê duyệt dữ liệu, license và phương án phân phối.

## Mã chức năng hiện tại
- MapLibre MapView và vòng đời Android.
- UI tiếng Việt với bản đồ lớn, nút bấm dễ nhìn và giao diện chrome sáng/tối.
- GPS theo yêu cầu người dùng, quyền cấp lúc thao tác và hiển thị giá trị thực.
- Trình xác minh SHA-256 cho tệp offline (chưa phải bộ đọc PMTiles).
- Bộ công cụ kiểm tra nguồn dữ liệu vùng biển Việt Nam dưới `tools/vietnam-sovereignty-map`.

Các mục tìm kiếm, POI, định tuyến, dẫn đường, traffic, PMTiles, bản đồ offline 34 tỉnh, 3D và hồ sơ Hoàng Sa/Trường Sa hiện **chưa hoàn thành**. Ảnh thiết kế `IMG_4470.png` đến `IMG_4478.png` chỉ là tham chiếu giao diện, không phải ảnh chụp phần mềm hoạt động.

Đọc `ARCHITECTURE.md`, `DATA_SOURCES.md`, `BUILD_STATUS.md` và `PROJECT_STATE.md` trước khi triển khai tiếp.
