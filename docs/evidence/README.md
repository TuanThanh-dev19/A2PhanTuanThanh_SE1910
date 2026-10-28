Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Evidence Index

Thư mục lưu log/report và ảnh thực tế cho Assignment 02. Bổ sung screenshot khi đã thao tác đúng test case; file đề xuất chưa phải evidence đã có.

## Quy tắc đặt tên và ghi nhận

- Log theo thư mục ngày/milestone; ảnh dùng PNG, video MP4/WebM nếu được yêu cầu.
- Mỗi lần chạy ghi ngày/môi trường, người chạy, code revision (hoặc base + working tree), test ID, expected/actual và kết quả.
- Ảnh có ngữ cảnh trang, dữ liệu và kết quả; có thể chứng minh nhiều case nếu quan sát rõ.
- Không để lộ password, token/cookie; giữ nguyên artifact cũ khi có lần chạy mới.

## E01 - Baseline và nền tảng backend

| Evidence | Test ID | Vị trí | Trạng thái |
|---|---|---|---|
| Baseline Boot 4, một context test | TC-BASE-01 | [inspection](2026-10-28-baseline/inspection.md), [report](2026-10-28-baseline/context-test.txt), [manifest](2026-10-28-baseline/manifest.json) | Có, AI/tool chạy |
| Boot 3 build/test, hai tests pass | TC-SETUP-01 | [M1 inspection](2026-10-28-m1/inspection.md), [build](2026-10-28-m1/build-test.txt), [manifest](2026-10-28-m1/manifest.json) | Có, AI/tool chạy |
| SQL Server session và tên DB | TC-SETUP-02-BE-DB, TC-M1-DB, TC-M1-SCHEMA | [SQL session](2026-10-28-m1/sqlserver-application-session.txt) | Có, AI/tool chạy |
| JAR/Tomcat/HTTP startup | TC-M1-HTTP | [runtime smoke](2026-10-28-m1/runtime-smoke.txt) | Có, AI/tool chạy |
| `e01-intellij-boot3.png` (đề xuất) | TC-SETUP-03 | Ảnh IDE/main/log Boot 3 và kết nối SQL | Chưa bổ sung |

M1 chưa có bảng nghiệp vụ. Chi tiết lệnh, môi trường và phạm vi ở inspection; log trên không thay thế minh chứng sinh viên chạy IntelliJ.

## E01B - M2 persistence trên SQL Server

Lần nghiệm thu cuối: **28/10/2026, run-05**, base `ea84544` + working tree M2; 9 tests pass (2 M1 + 7 M2), JAR smoke và hai JVM restart giữ nguyên schema/data. [Inspection M2](2026-10-28-m2/inspection.md) giải thích môi trường, lệnh, expected/actual, lỗi thật và các giới hạn.

| Evidence | Test ID | Vị trí |
|---|---|---|
| Build/test cuối và reports | TC-SETUP-01, TC-M1-DB, TC-DB-01..03 | [Build](2026-10-28-m2/run-05/build-test.txt), [M1 regression](2026-10-28-m2/run-05/fpt.edu.a2phantuanthanh_se1910.A2PhanTuanThanhSe1910ApplicationTests.txt), [M2 tests](2026-10-28-m2/run-05/fpt.edu.a2phantuanthanh_se1910.persistence.NewsPersistenceTests.txt) |
| 5 bảng/26 cột, keys, constraints, Unicode data | TC-DB-01,02 | [Isolated SQL snapshot](2026-10-28-m2/run-05/isolated-schema-data.txt), [DB bài tập](2026-10-28-m2/run-05/assignment-schema-data.txt) |
| Dữ liệu còn qua hai JVM JAR độc lập | TC-DB-02 | [So sánh SHA256](2026-10-28-m2/run-05/process-restart-verification.txt), [snapshot sau restart](2026-10-28-m2/run-05/isolated-after-process-restarts.txt), restart-1/2-start.txt và smoke trong run-05 |
| JAR/TCP/NTLM/encryption | TC-M1-HTTP, TC-M1-DB | [Startup](2026-10-28-m2/run-05/application-start.txt), [HTTP](2026-10-28-m2/run-05/application-smoke.txt), [SQL session](2026-10-28-m2/run-05/sqlserver-application-session.txt) |
| Lỗi thật và retest | DBG-002..005 | run-01 compile; run-02 assertion/identity diagnostic; run-03 SQL keyword; run-04 snapshot nullable sai; run-05 đạt sau sửa. [Debug log](../debug-log.md) |
| Sửa NOT NULL do lỗi M2 | DBG-005 | [SQL có guard](2026-10-28-m2/repair-content-nullability.sql), [output 0 dòng trước/sau](2026-10-28-m2/repair-content-nullability.txt) |

**Provenance/header:** build/startup/SQL output, reports và checksum manifests là dữ liệu ghi lại hoặc sinh bằng Maven/sqlcmd/Java/PowerShell, giữ nguyên byte và không chèn comment Author vào log. `.gitattributes` đặt output TXT/manifest M2 thành `-text` để Git không chuẩn hóa line endings; whitespace gốc trong Maven log được giữ, không tính là lỗi định dạng source. `inspection.md` và script SQL repair do project viết có header. Baseline/M1 và Maven Wrapper giữ nguyên; không sửa author vào output cũ. Fixture chỉ lưu trong DB test mới, không có credential thật trong evidence. Thư viện/DLL và ảnh render PDF phục vụ đọc đề nằm trong tmp bị ignore, không commit. Manifest M2 ghi SHA256 source/evidence; không coi các lần fail/partial là Pass cuối.

## E02 - Login, quyền và public news

| File đề xuất | Test ID | Nội dung cần chụp | Trạng thái |
|---|---|---|---|
| `e02-login-validation.png` | TC-AUTH-02 | Form lỗi khi rỗng/sai credential, không lộ secret | Chưa bổ sung |
| `e02-login-role.png` | TC-AUTH-01, TC-AUTH-04 | Login hai role, menu/quyền và response 403 đúng | Chưa bổ sung |
| `e02-public-active.png` | TC-PUB-01, TC-PUB-02 | Khách chỉ xem được active, URL inactive bị từ chối | Chưa bổ sung |

## E03 - CRUD và Search

| File đề xuất | Test ID | Nội dung cần chụp | Trạng thái |
|---|---|---|---|
| `e03-account-crud-search.png` | TC-ACC-01, TC-ACC-02 | Account Create/Update popup, list và Search | Chưa bổ sung |
| `e03-account-delete.png` | TC-ACC-03, TC-ACC-04, TC-UI-02 | Confirmation/Cancel, xóa hợp lệ và case có bài bị chặn | Chưa bổ sung |
| `e03-category-crud-search.png` | TC-CAT-01, TC-CAT-02 | Category popup, list và Search | Chưa bổ sung |
| `e03-category-delete.png` | TC-CAT-03, TC-CAT-05, TC-UI-02 | Confirmation và rule chặn xóa category có bài | Chưa bổ sung |
| `e03-news-tags.png` | TC-NEWS-01, TC-NEWS-02 | Popup bài có category/status/tags và kết quả lưu | Chưa bổ sung |
| `e03-news-delete-search.png` | TC-NEWS-04, TC-NEWS-05, TC-UI-02 | Search, confirmation và bài/links sau xóa | Chưa bổ sung |
| `e03-profile-history.png` | TC-SELF-01, TC-SELF-03 | Hồ sơ và history riêng của hai Staff | Chưa bổ sung |

## Evidence tài liệu và kiểm tra trước nộp

| Evidence | Vị trí | Trạng thái |
|---|---|---|
| E04 - Test matrix | [test-matrix](../test-matrix.md) | Có kết quả baseline/M1 và persistence M2; M3 API đã chạy; CRUD/UI chưa chạy |
| E05 - Debug log | [debug-log](../debug-log.md) | Có lỗi version và DBG-002..005 với fix/retest thật, warning còn theo dõi |
| E06 - Thiết kế và code-flow | [design](../design.md), [code-flow](../code-flow.md) | Startup/persistence M2 đã triển khai; M3 auth/public đã có; CRUD còn dự kiến |
| E07 - AI usage log | [ai-usage-log](../ai-usage-log.md) | Có |
| E08 - Git/tiến độ | [development-plan](../development-plan.md) | Có baseline/M1, commit quy ước riêng và M2; còn kiểm tra attribution/toàn tiến trình |
| E09 - Hướng dẫn chạy | [README](../../README.md) | Có backend; frontend chưa triển khai |

- [ ] Bổ sung IntelliJ, login và ảnh từng module khi đã kiểm tra.
- [ ] Có cả normal và negative case, popup/confirmation và request axios.
- [ ] Tên file trong index khớp file thật; Actual/Pass chỉ ghi khi đã chạy.
- [ ] Kiểm tra đúng repo/account, test lại từ checkout sạch và loại secret khỏi ảnh/log.


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
