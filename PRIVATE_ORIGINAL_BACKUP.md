# NaviTaxiMap · hướng dẫn giữ bản sao GỐC PMTiles riêng tư

**Cảnh báo:** Công cụ kiểm chứng dưới đây KHÔNG tạo bản sao lưu. Một biên bản
JSON chỉ lưu SHA và kích thước không thể dùng để phục hồi các tệp PMTiles.

## 1. Hai bản lưu nguồn còn hạn tại GitHub Actions

Nguồn gốc: https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43378133931

| Dữ liệu | Tên artifact gốc | Artifact ID | Hết hạn UTC |
| --- | --- | --- | --- |
| 34 tỉnh/thành, 287.504.805 byte PMTiles | `NaviTaxiMap-34-Provinces-OSM-UNREVIEWED` | 14015875271 | 2026-11-27 06:25:12 |
| Toàn Việt Nam, 556.229.998 byte PMTiles | `NaviTaxiMap-Vietnam-Whole-Country-OSM-UNREVIEWED` | 14015113186 | 2026-11-27 06:25:08 |

Tổng cộng 843.734.803 byte dữ liệu nguồn PMTiles (chưa tính vỏ ZIP).
Cả hai artifact này là **tạm thời**, không phải hosting tải gói lâu dài.
Không đưa URL artifact vào ứng dụng Android.

## 2. Sao lưu bằng hành động thủ công, không công bố dữ liệu

1. Đăng nhập tài khoản GitHub được phép xem repository.
2. Mở workflow nguồn ở trên và tải **cả hai** artifact riêng biệt về ổ lưu trữ
   hoặc vùng sao lưu **riêng tư được chủ dự án phê duyệt** trước khi hết hạn.
3. Giữ nguyên ZIP gốc hoặc giải nén vào hai thư mục cục bộ khác nhau.
   **Không upload PMTiles hoặc ZIP lên GitHub Releases, repository, CDN hoặc
   dịch vụ chia sẻ công khai** khi chưa có xác nhận ODbL và quyền phân phối.
4. Chạy lệnh trong thư mục mã nguồn NaviTaxiMap, thay hai đường dẫn bằng ZIP
   gốc bạn đã tải hoặc thư mục riêng chứa các file:
   
       python3 tools/vietnam34/verify_private_preservation.py \
         --provinces "/duong-dan-rieng/34-tinh.zip" \
         --national "/duong-dan-rieng/toan-quoc.zip" \
         --receipt "/duong-dan-rieng/bien-ban-kiem-chung.json"

5. Chỉ chấp nhận thông báo:

       PRIVATE_PRESERVATION_VERIFY_PASS files=35 bytes=843734803

6. Giữ **cả hai ZIP/thư mục dữ liệu gốc** cùng biên bản, trong ổ riêng tư có
   khả năng phục hồi. Kiểm thử phục hồi sau này cần lấy đủ 35 tệp đã sao lưu.

Công cụ đọc dữ liệu bằng luồng 1 MiB, đối chiếu đúng **35 mã/tên, byte count,
SHA-256 toàn tệp, 8 byte magic PMTiles v3**, và từ chối tệp thiếu, trùng, dư,
symlink, ZIP traversal hay hash sai. Không yêu cầu thư viện ngoài Python chuẩn.
Không upload, không giải nén ZIP, không sao chép bản đồ.

## 3. Biên bản CI mới: kiểm chứng lại nguồn, nhưng KHÔNG có backup

- Workflow [43526911330](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43526911330)
  **SUCCESS**: tải lại hai artifact nguồn vào **runner tạm**, đối chiếu đủ 35
  file và 843.734.803 byte, xác thực SHA-256 v3 magic theo ledger, chỉ upload
  artifact **`NaviTaxiMap-35-original-local-receipt-NO-DATA`** với kích thước
  ZIP **2.872 byte**, ID 14085237703.
- Artifact biên bản có hạn 2026-11-27 23:25:25 UTC; đây **không** phải tệp
  PMTiles được sao lưu. Runner GitHub Actions là tạm thời.
- Việc chạy lại kiểm chứng không làm gia hạn hai artifact dữ liệu gốc.

## 4. Ràng buộc pháp lý và sản phẩm

Các tệp Geofabrik OSM Shortbread 1.0 là dữ liệu thử nghiệm xem offline;
`Vietnam35Catalog.distributionApproved=false`,
`Vietnam35Catalog.navigationApproved=false`, nguồn HTTPS được duyệt vẫn rỗng.
Không suy ra dữ liệu địa giới Việt Nam, bản đồ dẫn đường hoặc quyền phân phối.
Mọi hosting, mở tải trong ứng dụng, sao lưu lên cloud hay phát hành cần
quyết định của chủ dự án và đánh giá ODbL/nguồn dữ liệu trước.


## Kết quả sau kiểm thử nguồn và Android
- Full-original verification `43526911330`: SUCCESS, 35/35 file, 843.734.803 byte, biên bản metadata-only ID 14085237703.
- Android workflow `43527115725`: SUCCESS hai jobs, gồm 7 bài kiểm thử verifier và test bản đồ/SHA SAF/emulator. **Hai ZIP chứa PMTiles vẫn chưa được bảo quản tại nơi lưu trữ riêng tư lâu dài** bằng công cụ này. Giữ nguyên hành động tải hai ZIP gốc thủ công trước 27/11/2026.


## Cảnh báo an toàn mới (14/11/2026)
Từ commit `9935d982` (test chốt bằng `a1926367`), công cụ kiểm chứng không còn dùng Python `assert` để xác minh SHA, byte count, magic PMTiles hoặc ZIP path. Chạy với `python -O` cũng **không** bỏ qua kiểm tra nguồn. Workflow GitHub [43530370885](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43530370885) SUCCESS 8 bài kiểm thử ở cả chế độ thường và `-O`, 35 tệp gốc đọc đủ 843.734.803 byte. **Vẫn cần tải và giữ hai ZIP gốc ở nơi riêng tư đã được đồng ý** trước 27/11/2026, receipt không là backup.


## Thêm lớp xác thực với danh mục Android (14/11/2026)
Công cụ `verify_private_preservation.py` ngoài so sánh `PACK_BYTES_VERIFIED.tsv` và `SHA256SUMS.txt` còn đối chiếu đúng 35 mục SHA/kích thước/tên/mã với `Vietnam35Catalog.kt`, kiểm cờ không cấp phép phân phối/điều hướng. Nếu thay nhầm cùng một SHA ở cả hai tệp văn bản, công cụ phải từ chối do khác catalog. CI 43530846327 **SUCCESS** 9 test ở cả Python thường và `-O`, kiểm 35 tệp nguồn thật 843.734.803 byte. Vẫn chỉ tạo receipt, không sao lưu ZIP gốc dài hạn; cần nơi riêng tư đã phê duyệt trước 27/11/2026.


## 5. Sao chép hai ZIP vào thư mục **riêng tư đã được chủ dự án chấp thuận** (tùy chọn)

Bên cạnh công cụ **kiểm tra chỉ đọc** ở mục 2, từ commit `40c6ffd` có công cụ **sao chép thủ công** vào thư mục cục bộ. Không có thao tác tự động nào từ ChatGPT hoặc CI tạo bản backup riêng tư thực tế.

Chỉ dùng trên Linux/macOS có POSIX file permissions; tạo thư mục trên ổ riêng tư đã được chủ dự án phê duyệt rồi khóa quyền mode 0700. Ví dụ minh họa bằng placeholder, không phải đường dẫn đã được duyệt:
```bash
mkdir -m 700 -p "/THU_MUC_RIENG_TU_DA_DUOC_DUYET"
python3 tools/vietnam34/copy_private_originals.py \
  --provinces "/TAP_TIN_GOC/34-tinh.zip" \
  --national "/TAP_TIN_GOC/toan-quoc.zip" \
  --destination "/THU_MUC_RIENG_TU_DA_DUOC_DUYET"
```

Điều kiện PASS bắt buộc: `PRIVATE_OFFLINE_BACKUP_COPY_PASS files=35 pmtiles_bytes=843734803`. Công cụ KHÔNG tải qua Internet, chỉ đọc hai ZIP **đã tải thủ công** và kiểm 35 PMTiles thật bên trong. Kiểm SHA toàn ZIP nguồn, sao chép theo luồng tới temp 0600, kiểm lại ZIP đích, tạo tệp đích bằng hard-link atomic no-overwrite để không ghi đè bản cũ, và xuất `NaviTaxiMap-private-backup-receipt.json` metadata-only trong cùng thư mục. Nếu bị gián đoạn sau ZIP đầu, lần chạy lại nhận ra ZIP đầu đã kiểm đúng và chép ZIP thứ hai; nếu có file trùng tên nhưng sai byte, từ chối ghi đè. Thư mục không được dùng symlink hoặc có quyền group/others.

**QUAN TRỌNG:** Chưa có thư mục thực được chủ dự án cung cấp, nên công cụ này chưa được dùng để sao lưu 843.734.803 byte. GitHub Actions chỉ dùng **ZIP giả** trong unit tests, không sao lưu hai artifact nguồn ra nơi mới. Khuyến nghị giữ thêm một bản độc lập ngoài thiết bị và thử phục hồi sau khi có quyền lưu trữ rõ ràng. Không upload ZIP vào GitHub Releases, CDN, URL phân phối hoặc bên thứ ba khi chưa được duyệt ODbL và chính sách lưu trữ.


## 6. Sao lưu trực tiếp lên Backblaze B2 và Cloudflare R2 từ iPhone/Android qua GitHub Actions

Từ code `108e3ee5`, branch `develop/phase0-android-maplibre` có workflow `.github/workflows/private-original-cloud-backup.yml` và script `ci/private-original-cloud-backup.sh`. Workflow nhận Secrets: `B2_KEY_ID`, `B2_APPLICATION_KEY`, `B2_BUCKET`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_ACCOUNT_ID`, `R2_BUCKET`. Không nhập API key vào mã/ChatGPT. User đã tạo hai bucket và GitHub preflight **43554159334 PASS** xác nhận **7/7 tên Secrets đã được cấu hình**, chưa xác minh tính hợp lệ của token hoặc bucket privacy.

**Kiểm trước khi bật:** Backblaze B2 bucket phải là **Private**; Cloudflare R2 Public Development URL **OFF**, không có public Custom Domain; tokens chỉ được quyền đọc/ghi bucket của NaviTaxiMap. Không bật mặc định public ACL hay dùng account-wide admin credentials.

**Chốt cách kích hoạt không sửa main:** GitHub `workflow_dispatch` đòi file YAML có trên default branch. Vì không được sửa `main`, workflow này có `push` chỉ ở `develop/phase0-android-maplibre`, và job upload chỉ chạy khi commit được user phê duyệt có marker `[private-backup-go]` trong message. Commit tạo workflow **không có marker**, nên chạy kiểm tra preflight, upload **SKIPPED**. Sau khi chủ dự án nhắn rõ yêu cầu sao lưu, trợ lý tạo commit kích hoạt trên develop, theo dõi job. Tuyệt đối không công khai/PR/merge.

**Luồng khi được phê duyệt:** GitHub Actions runner tải nguyên ZIP chứa 34 PMTiles gốc (artifact ID `14015875271`) và ZIP chứa 1 PMTiles VN (artifact ID `14015113186`) từ run 43378133931, dùng `verify_private_preservation.py` full SHA-256 35 PMTiles, kiểm byte và PMTiles v3, tính SHA cả 2 ZIP; chỉ sao chép dưới `originals/github-run-43378133931/` đến hai bucket PRIVATE bằng `rclone --immutable` (không xóa, không ghi đè); **đọc lại byte từ mỗi kho** bằng `rclone check --download` để nghiệm thu. Chỉ upload biên bản metadata lên GitHub sau khi hai kho PASS; dữ liệu gốc ở trong hai kho, không vào GitHub Releases hoặc artifacts mới. Phí lưu trữ/tải xuống và quyền bucket phải được theo dõi.

**Tại thời điểm kiểm tra:** không có hai ZIP gốc nào được sao lưu mới trên B2/R2 từ workflow này. Biên bản cũ 35/35 SHA không phải là bản copy. Hạn nguồn gốc vẫn 27/11/2026 06:25 UTC.

## Chủ dự án cho phép chuyển hai ZIP nguồn vào B2/R2 Private (14/11/2026 11:05 ICT)

Chủ dự án nhắn rõ **"Bắt đầu sao lưu riêng tư B2 và R2"**, sau khi đã tạo hai bucket, khóa truy cập và GitHub Secrets. Commit đánh dấu `[private-backup-go]` **chỉ chạy trên** `develop/phase0-android-maplibre` để kích hoạt job cloud copy. Không cho phép công khai bucket, không giải quyết quyền tái phân phối, không bật tải bản đồ từ app, không sửa `main`. Chỉ xác nhận bảo toàn nguồn nếu đủ hai bản ZIP ở hai kho và cả hai remote byte-readback SHA đều PASS. Nếu chỉ có một kho PASS, ghi trạng thái **PARTIAL**, không nói đã hoàn tất.

## 14/11/2026 ~11:08 ICT: lần kích hoạt đầu và chuẩn bị bản sao B2 trước
- Chủ dự án phê duyệt upload private B2/R2. Workflow [43554976535](https://github.com/ThanhTaxiXanh/NaviTaxiMap/actions/runs/43554976535) preflight PASS, nhưng uploader FAIL **trước khi tải dữ liệu** vì `R2_ACCOUNT_ID` trong GitHub Secret không theo định dạng 32 ký tự hex. Không log giá trị secret. Chưa có ZIP nào được xác nhận trên B2 hoặc R2 trong lượt đó.
- Script đổi thứ tự: xác minh đủ 35 PMTiles nguồn rồi copy và **readback B2** trước khi kiểm R2_ACCOUNT_ID; cho phép Cloudflare ID hoặc official R2 endpoint, từ chối domain bất kỳ. Ghi biên bản `b2-confirmation.txt` metadata-only nếu B2 PASS dù R2 FAIL, không nói đủ 2 kho khi R2 chưa xong. Không sửa main, app download flags, không public ZIP.


## 7. Sửa lỗi cấu hình Secrets trước khi có thể sao lưu hai kho (14/11/2026)

Hai run có sự cho phép chủ sở hữu nhưng chưa chuyển dữ liệu thành công: `43554976535` dừng trước bước tải dữ liệu vì `R2_ACCOUNT_ID` không đúng định dạng 32 ký tự hex; `43555218616` đã đọc đủ 35 PMTiles nguồn và kiểm SHA nhưng B2 từ chối do `B2_BUCKET` khác bucket mà Application Key cho phép. **Không có bản B2 hoặc R2 nào được xác nhận.** Các lỗi này không hàm ý PMTiles hỏng và không được xử lý bằng cách bỏ kiểm tra checksum.
- Trên iPhone/Android: GitHub → repo NaviTaxiMap → Settings → Secrets and variables → Actions → sửa `B2_BUCKET` theo đúng tên bucket Backblaze mà scoped key được phép, không thêm URL/prefix/ dấu cách. Tên có phân biệt chữ hoa-thường.
- Sửa `R2_ACCOUNT_ID` bằng đúng **Cloudflare Account ID dạng 32 ký tự hex** trong Cloudflare Dashboard, KHÔNG phải tên bucket, không phải Access Key ID, không phải Secret Access Key hoặc token; không dán bất kỳ khóa nào vào chat.
- Sau khi chủ dự án xác nhận đã sửa hai Secrets, mới chạy lại phát lệnh sao lưu có marker trên develop và kiểm byte-readback đủ hai dịch vụ. Hạn tải ZIP nguồn GitHub vẫn 27/11/2026. Không đụng `main` hay bật public bucket.
