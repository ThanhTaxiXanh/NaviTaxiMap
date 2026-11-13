# Bộ 35 PMTiles Việt Nam: 34 tỉnh/thành + gói toàn quốc

**Ngày dựng và kiểm thử: 13/11/2026.** Trạng thái: **35/35 GÓI ĐƯỢC TẠO VÀ KIỂM TRA SHA-256; DỮ LIỆU CHƯA ĐƯỢC DUYỆT ĐỂ DẪN ĐƯỜNG HOẶC XÁC ĐỊNH ĐỊA GIỚI PHÁP LÝ.**

## Tải gói

- [34 gói PMTiles tỉnh/thành](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931/artifacts/14015875271) (artifact GitHub, ZIP khoảng **287,5 MB** dữ liệu PMTiles).
- [01 gói PMTiles toàn Việt Nam](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931/artifacts/14015113186) (artifact GitHub, ZIP khoảng **556,2 MB** dữ liệu PMTiles).
- [Manifest và bảng SHA-256 được kiểm tra độc lập](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43379440781/artifacts/14015264272).
- Manifest đã đóng trong cả hai artifact nguồn; bảng checksum dạng văn bản: [SHA256SUMS](tools/vietnam34/SHA256SUMS.txt).
- Artifact GitHub Actions có thời hạn lưu trữ theo cấu hình workflow (gói dữ liệu 14 ngày, manifest kiểm tra 30 ngày). Cần lưu bản sao ở nơi lưu trữ phù hợp nếu muốn phân phối lâu dài.

## Kết quả và phương pháp xác thực

- Tác vụ dựng gói [43378133931](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931): **PASS**. Tổng **35 gói PMTiles v3/MVT**, **567,706** ô vector trong bộ toàn quốc, và 34 gói tỉnh/thành xuất từ cùng nguồn.
- Tác vụ [43379440781](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43379440781): **PASS**, đọc lại độc lập toàn bộ 35 tệp, đối chiếu kích thước và SHA-256, cấu trúc đầu PMTiles, mã tỉnh và danh sách chính thức.
- Toàn quốc: **556,229,998 byte**, SHA-256 `678704befd7b4b42726199994671ddccb1c2c029daab40f53098709cd5b12d55`, zoom 0 đến 14.
- 34 tỉnh/thành: tổng **287,504,805 byte**, zoom 8 đến 14 (có thể overzoom khi hiển thị).
- Nguồn Shortbread MBTiles Geofabrik: `https://download.geofabrik.de/asia/vietnam-shortbread-1.0.mbtiles`, bản chụp **686.698.496 byte**, SHA-256 `492402e2ebc49516ba72ff90bb0212eba29fab3cc2a9addcb95bb39d6b1089d5`.
- Bộ ranh giới kỹ thuật: trích từ OSM admin_level=4, dữ liệu Geofabrik PBF **332.558.874 byte**, SHA-256 `ddb9155f112af521d0674e4f40b87173f92954e5b8a3f71445d3008b506ef9ed`. Do OSM PBF không dựng được Khánh Hòa thành đa giác, đã lấy riêng quan hệ OSM `1887959`, bản XML 4.647.689 byte, SHA-256 `2b94689adcc3bf8fbfffe83387655421598b08dae3b32a908791e1d995c595b1`.
- Mã và tên 34 đơn vị hành chính đối chiếu với **Quyết định 19/2025/QĐ-TTg** có hiệu lực từ ngày 01/07/2025. Danh mục nguồn: `tools/vietnam34/provinces_34.json`.
- Bản nguồn và script: `tools/vietnam34/build_national_and_provinces.py`, `.github/workflows/vietnam34-build-35-once.yml`, `.github/workflows/vietnam34-final-verify.yml`. Các phép tính không tự tạo đường; toàn bộ ô vector bắt nguồn từ bộ Shortbread OSM.
- Cần biểu diễn trên MapLibre bằng style tương thích với **Shortbread 1.0**, khác style vector ba lớp của mẫu 54 KB Hà Nội trước đây.

## Danh sách 35 tệp

| Mã | Tỉnh/thành | Tệp | MB | Số ô |
|---|---|---|---:|---:|
| VN | Toàn Việt Nam | `00-viet-nam-all.pmtiles` | 556.23 | 567,706 |
| 01 | Hà Nội | `01-ha-noi.pmtiles` | 17.56 | 1,071 |
| 04 | Cao Bằng | `04-cao-bang.pmtiles` | 3.44 | 2,001 |
| 08 | Tuyên Quang | `08-tuyen-quang.pmtiles` | 11.29 | 3,964 |
| 11 | Điện Biên | `11-dien-bien.pmtiles` | 5.12 | 2,714 |
| 12 | Lai Châu | `12-lai-chau.pmtiles` | 6.09 | 2,646 |
| 14 | Sơn La | `14-son-la.pmtiles` | 7.32 | 3,849 |
| 15 | Lào Cai | `15-lao-cai.pmtiles` | 9.14 | 3,786 |
| 19 | Thái Nguyên | `19-thai-nguyen.pmtiles` | 6.90 | 2,363 |
| 20 | Lạng Sơn | `20-lang-son.pmtiles` | 5.34 | 2,396 |
| 22 | Quảng Ninh | `22-quang-ninh.pmtiles` | 5.63 | 3,603 |
| 24 | Bắc Ninh | `24-bac-ninh.pmtiles` | 11.05 | 1,433 |
| 25 | Phú Thọ | `25-phu-tho.pmtiles` | 12.76 | 2,765 |
| 31 | Hải Phòng | `31-hai-phong.pmtiles` | 7.28 | 2,175 |
| 33 | Hưng Yên | `33-hung-yen.pmtiles` | 6.33 | 1,325 |
| 37 | Ninh Bình | `37-ninh-binh.pmtiles` | 6.25 | 2,365 |
| 38 | Thanh Hóa | `38-thanh-hoa.pmtiles` | 7.13 | 3,778 |
| 40 | Nghệ An | `40-nghe-an.pmtiles` | 8.77 | 5,199 |
| 42 | Hà Tĩnh | `42-ha-tinh.pmtiles` | 6.13 | 2,811 |
| 44 | Quảng Trị | `44-quang-tri.pmtiles` | 8.45 | 5,587 |
| 46 | Huế | `46-hue.pmtiles` | 4.95 | 2,631 |
| 48 | Đà Nẵng | `48-da-nang.pmtiles` | 11.97 | 4,203 |
| 51 | Quảng Ngãi | `51-quang-ngai.pmtiles` | 8.98 | 5,500 |
| 52 | Gia Lai | `52-gia-lai.pmtiles` | 10.95 | 6,443 |
| 56 | Khánh Hòa | `56-khanh-hoa.pmtiles` | 5.40 | 10,302 |
| 66 | Đắk Lắk | `66-dak-lak.pmtiles` | 8.32 | 5,409 |
| 68 | Lâm Đồng | `68-lam-dong.pmtiles` | 13.09 | 12,052 |
| 75 | Đồng Nai | `75-dong-nai.pmtiles` | 12.10 | 3,368 |
| 79 | Hồ Chí Minh | `79-ho-chi-minh.pmtiles` | 18.22 | 9,096 |
| 80 | Tây Ninh | `80-tay-ninh.pmtiles` | 8.09 | 2,351 |
| 82 | Đồng Tháp | `82-dong-thap.pmtiles` | 7.39 | 1,729 |
| 86 | Vĩnh Long | `86-vinh-long.pmtiles` | 6.20 | 2,633 |
| 91 | An Giang | `91-an-giang.pmtiles` | 8.24 | 6,726 |
| 92 | Cần Thơ | `92-can-tho.pmtiles` | 6.66 | 2,538 |
| 96 | Cà Mau | `96-ca-mau.pmtiles` | 4.94 | 6,822 |

## Những giới hạn bắt buộc không được hiểu sai

1. **Đây là gói dữ liệu bản đồ vector, không phải phần mềm điều hướng.** Không có graph định tuyến, phân làn, luật rẽ, đường cấm, chỉ dẫn bằng giọng nói, giao thông trực tuyến hay địa chỉ POI đầy đủ.
2. Các gói tỉnh được trích bằng điều kiện **đa giác hành chính OSM giao với hình chữ nhật ô bản đồ**, chứ **không cắt chi tiết từng đối tượng MVT** tại đường biên. Vì vậy các ô gần biên có thể chứa đường của tỉnh lân cận hoặc một phần vùng biển.
3. Quan hệ hành chính OSM có một số vùng ven biển bao gồm mặt nước lớn hoặc đảo xa. Không dùng các gói này làm bản đồ ranh giới pháp lý, hải đảo, Hoàng Sa/Trường Sa hay bản đồ chủ quyền. Bộ quy tắc thẩm định địa lý riêng vẫn cần được xử lý trước khi hiển thị các lớp đó.
4. Gói toàn quốc là **phạm vi dữ liệu trích xuất Geofabrik**, không tự động chứng minh bao quát đủ mọi đảo, vùng biển, đường nội bộ hoặc lãnh thổ được quản lý theo pháp luật Việt Nam.
5. Giấy phép: **© OpenStreetMap contributors, ODbL 1.0**; tiles do Geofabrik xử lý theo lược đồ Shortbread. Việc kiểm toán quyền phân phối lại thành phẩm, ghi công hiển thị và các nghĩa vụ share-alike còn phải hoàn tất trước khi phát hành production.
6. Chưa được kiểm thử tải và hiển thị đồng thời toàn bộ gói quốc gia trên Xiaomi thực tế. APK hiện chỉ tích hợp mẫu Hà Nội 54 KB. **35 gói dữ liệu hiện là artifact tải riêng, chưa được thêm vào danh mục tải của ứng dụng.**
7. Đừng dùng dữ liệu này để lái taxi hoặc quyết định tuyến đường trước khi được kiểm chứng và tích hợp thành bản dẫn đường an toàn.

## Đường dẫn mã nguồn và kiểm thử
- [Danh mục tỉnh/thành chính thức](https://baochinhphu.vn/bang-danh-muc-va-ma-so-cua-34-tinh-thanh-moi-3321-don-vi-hanh-chinh-cap-xa-moi-102250704153652947.htm).
- [Nguồn OSM Geofabrik](https://download.geofabrik.de/asia/vietnam.html).
- [Shortbread vector schema 1.0](https://shortbread-tiles.org/schema/1.0/).
- [Bản ghi audit ranh giới 33 vùng từ Geofabrik](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43376868151).
- [Bổ sung quan hệ Khánh Hòa](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43377276622).
