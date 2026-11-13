# Kiến trúc NaviTaxiMap

## Kiến trúc mục tiêu
`UI -> Map Renderer -> MapData/PMTiles -> Offline packages`
`UI -> GPS -> Navigation -> Routing -> offline route graph`
`UI -> Search/POI -> SQLite`
`UI -> Road Intelligence/Traffic -> nguồn được xác minh`
`UI -> Vietnam Geographic Engine -> hồ sơ tư liệu có nguồn`

Chỉ `app` là module Android hiện đã khởi tạo. Các ranh giới trên là thiết kế module mục tiêu, KHÔNG phải module hoàn thành.

## Phase 0/1/2
- Android app: Kotlin, minSdk 26, target 35, compile 35.
- Map renderer: MapLibre Native Android OpenGL ES 13.5.4.
- Demo map style: truy cập Internet, dùng làm bản đồ thử nghiệm, không có SLA.
- GPS: Android LocationManager theo thao tác người dùng. Không gửi vị trí lên máy chủ ứng dụng.
- OfflinePackVerifier: kiểm tra SHA-256 trước kích hoạt. Phải bổ sung schema/version/atomic activation.
- Hiện không có PMTiles decoder, không được nhận là offline navigation.

## Các quy tắc dữ liệu
Tách biệt dữ liệu địa lý, tên nhãn, diễn giải chủ quyền, và đường ranh giới pháp lý. Chỉ tích hợp dữ liệu được chấp thuận có xuất xứ và quyền phân phối được xác minh. Không suy đoán tọa độ hoặc đường ranh giới.

## Kiểm thử
GitHub Actions trên nhánh develop: kiểm thử đơn vị + biên dịch debug + upload artifact. Chưa có thiết bị Android điều khiển từ xa để kiểm thử cài đặt, tương tác, GPU hay GNSS.
