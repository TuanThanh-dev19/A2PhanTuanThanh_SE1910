Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Evidence Index

## 1. Cách sử dụng

Sinh viên tự thao tác và chụp ảnh. Khi triển khai từng chức năng, chuẩn bị sẵn màn hình chạy được, dữ liệu và trường hợp dưới đây. Chỉ đổi thành “Đã chụp” khi có ảnh thật; tên ảnh là đề xuất.

Lưu ảnh vào `docs/evidence/screenshots/`. Một ảnh có thể chứng minh nhiều case nếu đủ ngữ cảnh. Không chụp password, token hoặc thông tin riêng ngoài bài làm.

## 2. Nền tảng và database

| Ảnh đề xuất | Màn hình / thao tác cần chuẩn bị | Nội dung cần thấy | Trạng thái |
|---|---|---|---|
| `e01-intellij-boot3.png` | IntelliJ: mở project, chạy main | Boot 3 khởi động và kết nối SQL Server | Chờ tự chụp |
| `e01-database-tables.png` | SQL Server Management Studio: mở DB bài tập | Đúng tên DB và 5 bảng | Chờ tự chụp |
| `e01-test-result.png` | IntelliJ/terminal: chạy tests | Test hoàn thành và tổng số pass | Chờ tự chụp |
| `e01-unicode-data.png` | DB test đã có fixture: query news/category | Tiếng Việt và các quan hệ lưu đúng | Chờ tự chụp |

Hiện đã có backend/database M2 và script `scripts/test-persistence.ps1`; chưa có giao diện React. Các ảnh phía dưới chụp sau khi chức năng tương ứng được triển khai.

## 3. Login và tin công khai

| Ảnh đề xuất | Màn hình / dữ liệu cần chuẩn bị | Nội dung cần thấy | Trạng thái |
|---|---|---|---|
| `e02-login-validation.png` | Login: để trống hoặc nhập sai | Lỗi form/credential | Chưa triển khai |
| `e02-login-role.png` | Login lần lượt Admin/Staff | Menu và quyền đúng role | Chưa triển khai |
| `e02-public-active.png` | Public news với bài active/inactive | Khách chỉ thấy active; URL inactive bị chặn | Chưa triển khai |

## 4. CRUD, Search và cá nhân

| Ảnh đề xuất | Màn hình / dữ liệu cần chuẩn bị | Nội dung cần thấy | Trạng thái |
|---|---|---|---|
| `e03-account-crud-search.png` | Admin account list, Create/Edit popup và Search | Tạo/sửa/tìm đúng account | Chưa triển khai |
| `e03-account-delete.png` | Account mới và account đã tạo bài | Confirm/Cancel; xóa hợp lệ và chặn xóa | Chưa triển khai |
| `e03-category-crud-search.png` | Staff category list, popup và Search | Name/description/parent/status đúng | Chưa triển khai |
| `e03-category-delete.png` | Category tự do và category có bài | Confirmation và thông báo chặn xóa | Chưa triển khai |
| `e03-news-tags.png` | News popup với category/status/tags | Tạo/sửa bài cùng tags | Chưa triển khai |
| `e03-news-delete-search.png` | News list với bài test | Search, Confirm/Cancel và kết quả xóa | Chưa triển khai |
| `e03-profile-history.png` | Hai Staff và bài do mỗi người tạo | Hồ sơ và history riêng | Chưa triển khai |

Sau khi code UI, ghi thêm route hoặc thao tác mở màn hình thực tế vào hàng tương ứng. Không dựng màn hình giả, không tự đánh dấu đã chụp.

## 5. Log kỹ thuật đã có

Giữ nguyên để tra cứu, không cần trình bày hết với giảng viên: [baseline](2026-10-28-baseline/inspection.md), [M1](2026-10-28-m1/inspection.md), [M2](2026-10-28-m2/inspection.md). Các log này không thay thế ảnh sinh viên tự chụp. Những mốc sau ưu tiên ảnh và kết quả ngắn, không tự tạo bộ log/manifest dài nếu không cần.


## M3 — Postman và SQL Server

Backend có API thật, chưa có React. Chạy theo README, seed demo rồi mở Postman với `baseUrl=http://localhost:8080`. Lưu email/password trong environment local, không export secret vào Git. Mỗi login lấy `accessToken` dùng ở tab Authorization → Bearer Token; che token/password khi chụp.

| Ảnh đề xuất / request | Kết quả cần thấy |
|---|---|
| `e02-api-login.png`: POST `{{baseUrl}}/api/auth/login`, Body raw JSON `{"email":"{{adminEmail}}","password":"{{adminPassword}}"}`; lặp lại Staff | 200, đúng accountRole=1/2; không password/hash |
| `e02-api-validation.png`: cùng POST với `{}`, email sai hoặc password rỗng; rồi credential sai | 400 với lỗi payload; 401 với credential sai |
| `e02-api-authorization.png`: GET `/api/accounts` không bearer, bearer hỏng/hết hạn, bearer Staff | 401, 401, 403; Admin GET `/api/news` cũng 403 |
| `e02-api-public.png`: GET `/api/public/news?q=M3`, GET detail active và inactive | List chỉ active; active=200; inactive=404 |
| `e02-api-sql-demo.png`: SSMS query dưới đây, chạy seed lại rồi query | 2 demo accounts (role 1/2), 1 demo category, 2 demo news (status 1/0), không thêm trùng |

```sql
SELECT AccountID, AccountName, AccountEmail, AccountRole
FROM dbo.SystemAccount WHERE AccountEmail IN ('admin.m3@example.com', 'staff.m3@example.com');
SELECT NewsArticleID, NewsTitle, NewsStatus, CategoryID, CreatedByID
FROM dbo.NewsArticle WHERE NewsTitle LIKE N'M3 Demo - %';
```

Ảnh trên **chờ sinh viên tự chụp**. Đúng role trên route quản trị hiện là 404 vì chưa có controller; không tạo endpoint giả. Token hết hạn có thể thử sau 1 giờ hoặc chạy local với `--app.jwt.lifetime=PT5S`, login rồi đợi trên 5 giây. Đổi role/xóa account để thử token cũ chỉ thực hiện trên DB test; không xóa account demo đang tham chiếu news.

AI/tool đã chạy ngày 28-10-2026: `scripts/test-m3.ps1` đạt 20 tests (2 M1 + 7 M2 + 11 M3), 0 failure/error/skipped; packaged JAR/public smoke và hai JVM restart persistence đạt. Hai lần chạy JAR demo qua HTTP thật đều đúng login/401/403/public active/inactive; số record sau hai lần seed giữ 2 account, 1 category, 2 news. Report hiện ở `target/surefire-reports`, output local trong `tmp/m2-results/` và `tmp/m3-http-result.txt` (không commit). Không tạo ảnh/log/manifest minh chứng mới trong Git.
