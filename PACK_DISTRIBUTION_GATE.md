# NaviTaxiMap: Gate phân phối và tải gói PMTiles qua HTTPS Range (chưa kích hoạt)

## Trạng thái tại 13/11/2026

- **Đã có bằng chứng Android offline 35/35 gói dữ liệu thật**: 34 gói cấp tỉnh/thành qua workflow 43454411678 (và 43438780272); một gói toàn Việt Nam 556.229.998 byte qua 43441211821 và 43454411510.
- Bản đồ được dùng để *xem thử offline*, không có quyền duyệt định tuyến, ranh giới quốc gia/hành chính hoặc phân phối dữ liệu.
- **Chưa có máy chủ lưu trữ bền vững, tên miền được thẩm định, HTTPS URL được duyệt hoặc xác nhận phân phối ODbL**. Các file GitHub Actions gốc chỉ là artifact ngắn hạn và không phải địa chỉ tải công khai trong sản phẩm. Artifact 43378133931 hết hạn khoảng 27/11/2026.
- `Vietnam35Catalog.distributionApproved == false` được giữ nguyên; ứng dụng không gắn nút tải gói, không lên lịch tải hoặc tự mở kết nối mạng từ mã mới.

## Lõi tạm thời (commit 389d71bf)

`app/src/main/java/vn/navitaximap/data/PinnedHttpsRangeTransfer.kt` là mã nội bộ, chưa được sử dụng từ `MainActivity` hay khởi chạy tự động:

1. `ApprovedCatalogPackDownloader` kiểm tra cờ phê duyệt phân phối **trước mọi request**. Với cấu hình hiện tại: chặn tải. Định danh gói được lấy từ catalog gồm tên chính xác, số byte và SHA-256 của dữ liệu Shortbread.
2. `PinnedHttpsRangeTransfer` buộc `https://`, đúng hostname đã thẩm định, cổng HTTPS thông thường, tên file nguyên văn; chặn query, fragment, credentials, ký tự đường dẫn đã mã hóa, HTTP và redirect. Không có hostname/URL mặc định.
3. Chỉ chấp nhận HTTP **206** với `Content-Range: bytes OFFSET-(SIZE-1)/SIZE` đúng offset và tổng byte ghim. Gửi `Accept-Encoding: identity`, chặn compressed response. Mỗi lần gọi chỉ thực hiện một request.
4. Ghi tối đa 64 KiB mỗi khối vào `shortbread-<sha256>.partial` trong thư mục staging app-private. Lần gọi lại dùng `Range` nối tiếp. Có fsync, giới hạn kích thước và kiểm tra huỷ luồng. Lỗi mạng giữ tệp .partial để thử lại khi người dùng yêu cầu.
5. Khi tải đủ mới băm SHA-256 toàn bộ tệp theo luồng. Sai checksum sẽ xoá file staging và **không** tạo indexed archive. Sau đó mới gọi `LocalPackStore.importPack` để kiểm SHA/kích thước và PMTiles v3 thêm lần nữa, commit metadata bằng atomic move. Không bỏ qua các kiểm tra hiện có.
6. Để giữ an toàn cho bước chuyển vào app-private store, cần sẵn dung lượng cho phần còn phải tải **cộng với một bản sao hoàn chỉnh và 16 MiB dư**. Vì vậy gói toàn quốc 556 MB cần khoảng 1,13 GB trống nếu tải từ đầu. Đây là đánh đổi tạm thời, chưa tối ưu cho điện thoại dung lượng thấp.

## Cần phê duyệt trước khi biến thành tính năng sử dụng

- Xác minh quyền tái phân phối theo ODbL, lời ghi công nguồn, dữ liệu sai khác so với nguồn và trách nhiệm cập nhật.
- Phê duyệt đích lưu trữ ổn định, danh sách hostname HTTPS, quyền sở hữu, an toàn TLS, chi phí và chính sách hết hạn/số phiên bản.
- Xác nhận manifest có SHA-256 và kích thước của **35 tệp đang dùng**, nguồn phiên bản, khả năng phát hiện phiên bản cũ; không tải manifest/tệp không được tin cậy.
- Thiết kế giao diện yêu cầu người dùng đồng ý từng gói, hiển thị dung lượng còn trống, tiến trình, huỷ/lặp lại, lỗi mạng và lựa chọn tiết kiệm dữ liệu di động.
- Thử trên máy Xiaomi thực tế, thiết bị ít RAM/bộ nhớ, mạng mất đột ngột, Range server không hỗ trợ, 206 body không đầy đủ, SHA sai, lỗi giải nén hoặc provider.
- Kiểm thêm cơ chế chống nhiều tiến trình ghi staging cùng lúc, quản lý file .partial cũ, riêng tư, chế độ proxy mạng và chứng chỉ nếu có yêu cầu.

## Những điều cố ý chưa làm

- Không gắn URL mẫu, URL giả hoặc URL artifact CI vào ứng dụng.
- Không thay cờ `distributionApproved`; không bật route/traffic/voice-navigation.
- Không tải gói qua HTTP, không tải nền khi người dùng chưa đồng ý, không chạy trên UI thread.
- Không coi kết quả 35/35 bản đồ hiển thị là xác nhận pháp lý cho địa giới hoặc khả năng định tuyến.

## Kiểm thử mã hiện tại

`PinnedHttpsRangeTransferTest.kt` dùng `FakeRange` không gọi Internet để kiểm: resume sau body bị đứt, header Range chính xác, SHA đúng, từ chối HTTP/host lạ/URL mã hoá/query/fragment, từ chối redirect/200/416, sai Content-Range hoặc body bị thay và chặn download mặc định vì catalog chưa được phê duyệt.

Chạy bằng `gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug` trong CI; xem run của commit feature để xác nhận kết quả thực tế. Khi phê duyệt về sau, phải bổ sung thử end-to-end với host được chấp thuận, không dùng test giả làm bằng chứng phân phối hoạt động.


## Cập nhật bảo vệ quyền cấp nguồn (13/11/2026 23:58 ICT)
- **Đã đóng nguy cơ duyệt tên máy chủ bằng tham số:** Bản đầu của `ApprovedCatalogPackDownloader.fetchApproved(code,url,reviewedHost)` cho phép caller tự cung cấp chuỗi `reviewedHost`. Commit **a0a0be16** chuyển thành `fetchApproved(code)`. Danh sách nguồn đã được xem xét được lưu duy nhất ở `ReviewedShortbreadSources.byCode`; hiện cố ý `emptyMap()`, không có máy chủ nào được cấp quyền. Muốn bật sau này cần tối thiểu **hai thay đổi được duyệt độc lập**: nguồn pin trong mã và cờ `Vietnam35Catalog.distributionApproved` (hiện false).
- `PinnedHttpsRangeTransfer.stage` là primitive nội bộ để kiểm thử, vẫn yêu cầu exact HTTPS host, Range=206, SHA và dung lượng; không được gọi trực tiếp từ giao diện hoặc entrypoint ngoài `ApprovedCatalogPackDownloader`.
- Kiểm thử độc lập bổ sung: file partial đủ byte sai SHA, resume với prefix đã hỏng, phản hồi gzip, thiếu dung lượng phải fail-closed, không có network thật. Android CI sau sửa `43468092472` chưa hoàn tất tại checkpoint.


## Bổ sung 14/11/2026: tiến độ chưa tin cậy và Content-Length nghiêm ngặt
- Lõi kiểm thử `PinnedHttpsRangeTransfer.inspectProgress(pin)` chỉ đọc kích thước tệp `shortbread-<sha>.partial`, không truy cập Internet hay Hash toàn file; kết quả `StagingProgress.completeButUnverified` **không phải dấu đã nhập hoặc file hợp lệ**, kể cả khi nhận đủ byte. `stage` vẫn bắt buộc SHA-256 toàn bộ và `LocalPackStore.importPack` kiểm lại trước khi hiện trong catalog.
- Không theo symbolic link của thư mục staging hoặc .partial, không chấp nhận kích thước lớn hơn số byte pin. HTTP có `Content-Length` nhưng sai cú pháp, âm, vượt Long hoặc khác byte Range bị từ chối trước khi ghi. Nếu header không có, stream vẫn phải đọc chính xác số byte Range để nghiệm thu.
- Tổng 8 JUnit simulated Range (không Internet), CI 43471067313 đang thử. Registry nguồn tải vẫn rỗng và `distributionApproved=false`; không dùng mock làm bằng chứng có máy chủ thật.


## CI xác nhận 14/11/2026 ~00:24 ICT
- `43471067313` **SUCCESS**: compile/JUnit với 8 phép thử mô phỏng Range, Android emulator offline PMTiles status/geometry + SAF SHA fixture + OSM attribution PASS. Đây chỉ là nghiệm thu core và hồi quy, KHÔNG phải kiểm thử tải 35 tệp trên máy chủ HTTPS thực.
- Nguồn `ReviewedShortbreadSources.byCode` vẫn rỗng, `Vietnam35Catalog.distributionApproved=false`; muốn bật cần xét duyệt host, quyền phân phối ODbL, manifest, tích hợp UI opt-in và thử mạng thật/thiết bị thật.


## 2026-11-14 backup verification is not distribution approval
- `PRIVATE_ORIGINAL_BACKUP.md` provides only an owner-run **private** offline preservation workflow. Python verifier reads actual 35 files or original ZIP artifacts, checks full SHA256/byte size/header and writes JSON receipt containing NO PMTiles data. CI 43526911330 successful; original source GitHub Actions artifact still expires Nov 27.
- **No private durable backup has yet been made by this CI.** The successful metadata-only receipt (artifact 14085237703, 2872 bytes) cannot recreate the original 843734803 bytes.
- `Vietnam35Catalog.distributionApproved=false` and empty reviewed host registry unchanged. Public cloud upload, product downloading, legal borders/routing approval remain forbidden until explicit approval.


## 2026-11-14 local preservation is not public distribution
- `copy_private_originals.py` is strictly operator-invoked for **local owner-approved private POSIX directory** and two already-downloaded original ZIPs. It is not called by Android or the CI to save real PMTiles; only synthetic ZIP test fixtures are used. The source 35-pack CI still uploads ONLY receipt metadata. No preconfigured private destination or Internet service is authorized.
- `distributionApproved=false` and `ReviewedShortbreadSources.byCode=emptyMap()` remain; no uploads, releases, public map download, border/routing approvals.


## 2026-11-14 two external PRIVATE cloud backups are separate from public distribution
- New opt-in cloud workflow backs up ORIGINAL two GitHub Actions artifact ZIPs only after owner authorization `[private-backup-go]` on develop. GitHub preflight 43554159334 shows all 7 Secrets configured but upload job skipped; no cloud copy or valid public distribution URL exists.
- On owner approval: full 35 original SHA-256+size+PMTiles v3 before upload, both private B2 & R2 readback after upload, metadata-only receipt. Cloud backups must remain Private and cannot be repurposed as app sources. DistributionApproved still false, reviewed HTTPS source registry empty, navigation/border review absent.


## 2026-11-14 original source now stored privately in B2 and R2; PUBLIC DISTRIBUTION REMAINS CLOSED
- Owner-approved source preservation run 43567301480 **SUCCESS**: SHA verified 35 original PMTiles 843734803 bytes before immutable uploads, two original artifact ZIPs copied separately into B2 and R2, full bytes readback from both cloud remotes PASS. Metadata receipt 14103007744 contains no map data. Private storage is for disaster recovery only.
- Private bucket/object public exposure settings still require owner dashboard confirmation. This does NOT create approved HTTPS map source, ODbL redistribution clearance, traffic routing permissions or legal boundary validation. `distributionApproved=false`, `navigationApproved=false`, reviewed Shortbread HTTPS sources empty, main branch unchanged; do NOT create public links.
