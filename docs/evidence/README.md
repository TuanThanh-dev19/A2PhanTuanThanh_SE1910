# Quy ước lưu minh chứng

Chỉ lưu artifact từ hành động đã thực hiện. File kế hoạch hoặc ảnh UI mockup không phải bằng chứng ứng dụng chạy. Tránh đưa raw token, password, cookie hay connection string có secret vào evidence.

## Baseline hiện có

- [Ghi nhận khảo sát ngày 28/10/2026](2026-10-28-baseline/inspection.md).
- [Bản sao báo cáo Surefire gốc](2026-10-28-baseline/context-test.txt).
- [Manifest checksum nguồn và code baseline](2026-10-28-baseline/manifest.json).

Baseline chỉ chứng minh trạng thái skeleton đã kiểm tra; chưa chứng minh SQL Server, UI, API nghiệp vụ, Git history hay quyền sở hữu remote.

## Cấu trúc cho các lần chạy sau

Mỗi lần chạy dùng thư mục theo ngày và mục đích, ví dụ `YYYY-MM-DD-category-delete/`. Tên thư mục ví dụ này chưa phải evidence đã tồn tại.

Mỗi thư mục cần ghi:

| Trường | Nội dung |
|---|---|
| Thời gian | Thời gian thực, timezone rõ ràng |
| Code | SHA đã test; nếu working tree có sửa, ghi base SHA và file diff/checksum |
| Phạm vi | REQ và test ID liên quan |
| Môi trường | Java/Boot/Node, DB instance/test DB phù hợp; không có credential |
| Dữ liệu | Fixture hoặc bước tạo dữ liệu có thể tái hiện |
| Thực hiện | Lệnh/API hoặc bước thao tác UI |
| Kết quả | Expected, actual, PASS/FAIL/BLOCKED; không bỏ lỗi khỏi report |
| Artifact | Test report, response đã loại secret, screenshot UI/DB hoặc log liên quan |
| Người xác minh | AI/tool hoặc sinh viên tự thực hiện, ghi đúng thực tế |

## Evidence cần có ở các mốc

- Setup: phiên bản Boot 3 và IntelliJ chạy project.
- Database: bảng/FK do JPA tạo trên SQL Server; dữ liệu Unicode và tồn tại qua restart.
- Auth: login hai role, truy cập bị 401/403; không chụp token chưa che.
- CRUD: thao tác create/read/update/delete/search, kết quả API và dữ liệu lưu thật.
- Delete guards: account/category có bài active và inactive đều bị chặn.
- React: popup tạo/sửa, confirmation với Cancel/Confirm, form lỗi và network request axios.
- Cá nhân: hai Staff có history riêng, profile không tự nâng quyền.
- Git: owner remote, author attribution, commit graph và diff qua mốc; chỉ lấy sau khi đã có repository thật.

Trước nộp, người đọc phải lần theo `requirements → code-flow → test-matrix → evidence → Git commit` mà không gặp artifact thiếu hoặc tuyên bố quá phạm vi kết quả đã chạy.
