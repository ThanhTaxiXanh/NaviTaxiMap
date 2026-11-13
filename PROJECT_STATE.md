# PROJECT_STATE
Ngày khởi tạo: 2026-11-13 (Asia/Bangkok)
Nhánh làm việc: develop/phase0-android-maplibre
Nhánh main: phải giữ nguyên, chưa được phép merge.

## Trạng thái hiện tại
- PHASE 0: IN PROGRESS. Đã kiểm tra repo GitHub gần trống; thiết bị Xiaomi/Termux chưa kết nối.
- PHASE 1: IN PROGRESS. Đã soạn dự án Android Kotlin với MapView, UI và GPS theo yêu cầu.
- PHASE 2: IN PROGRESS. Đã khai báo MapLibre, cần kết quả build/debug thực tế.
- PHASE 3–18: NOT STARTED, trừ bộ công cụ kiểm tra dữ liệu được nhập sớm.
- APK debug: chưa xác nhận biên dịch ở thời điểm khởi tạo commit.
- Android device debugging: BLOCKED do không có kết nối thiết bị/ADB.

## Những điều tuyệt đối không giả định
Không có PMTiles offline đã chạy; không có routing, speed limit, traffic thật, danh mục offline hoàn chỉnh hoặc lớp dữ liệu chủ quyền đã được duyệt.
Không khẳng định UI theo ảnh mẫu đã triển khai trọn vẹn.

## Đợt tiếp
Đọc kết quả GitHub Actions; sửa lỗi biên dịch nếu có; mở rộng cấu trúc renderer, nguồn và offline đúng kiến trúc; kiểm thử trên Android thật khi có kênh kết nối.
