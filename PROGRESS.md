# PROGRESS
2026-11-13
- Nhánh develop/phase0-android-maplibre đã tạo từ main, main không thay đổi.
- Đã đọc master prompt và ZIP validator, rà soát 9 ảnh tham chiếu.
- Đã triển khai app Android/Kotlin, MapLibre renderer demo, UI cơ bản và GPS khi người dùng yêu cầu.
- Đã thêm verifier SHA-256, Python validation harness, test Android, docs và CI chỉ trên nhánh develop.
- CI lần 1: phát hiện SDK tools package lỗi, đã sửa.
- CI lần 2: phát hiện Kotlin metadata 2.2 và compiler 2.0 không tương thích, đã sửa.
- CI lần 3 (43352195061): PASS Android assembleDebug, testDebugUnitTest và Python unit tests.
- Đã xác minh APK tải về là ZIP không hỏng, chứa các ABI Android.
- Đã thêm smoke test dùng Android emulator để kiểm chứng khởi chạy và thu screenshot/logcat; chờ kết quả thực tế.
- Chưa kết nối được thiết bị Xiaomi; không nhận thành công khi chưa kiểm thử thật.
