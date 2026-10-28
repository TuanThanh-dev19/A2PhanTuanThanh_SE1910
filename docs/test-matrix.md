# Ma trận kiểm thử

Trạng thái 28/10/2026 sau M1: giữ TC-BASE-01 Boot 4; đã chạy setup Boot 3 và kết nối SQL Server thật như bảng M1 bên dưới. Các case nghiệp vụ và IntelliJ vẫn **NOT RUN**. “Kỳ vọng” là tiêu chí thiết kế, không phải kết quả quan sát.

## 1. Quy ước

- `PASS`: đã thực hiện đủ bước, actual khớp expected và có evidence.
- `FAIL`: đã chạy, có sai lệch; liên kết debug-log.
- `BLOCKED`: đã muốn chạy nhưng có điều kiện cụ thể ngăn cản, ghi rõ điều kiện.
- `NOT RUN`: chưa thực hiện. Không suy diễn từ code hoặc mock rằng end-to-end đã pass.
- Một hàng có nhiều nhánh chỉ được PASS khi đã chạy đủ nhánh; có thể tách thành các case con khi triển khai.

Fixture dự kiến: Admin A; Staff S1/S2; account U chưa tạo bài; category C-used có bài active và inactive; C-free không có bài; category parent/child; N-active và N-inactive do S1 tạo; N-other do S2 tạo; tag tự do và tag đã gắn. Dữ liệu demo được tạo qua repository/API trên SQL Server; chưa có fixture này hiện tại.

## 2. Kết quả baseline thực tế

| ID | Kiểm tra | Actual | Kết quả | Minh chứng |
|---|---|---|---|---|
| TC-BASE-01 | `./mvnw.cmd test` trên skeleton hiện có | Boot 4.1.2, JDK 21.0.8; 1 context test, 0 failure/error/skipped; BUILD SUCCESS | PASS phạm vi skeleton | [Surefire report](evidence/2026-10-28-baseline/context-test.txt), [ghi nhận lần chạy](evidence/2026-10-28-baseline/inspection.md) |

TC-BASE-01 không nghiệm thu REQ-02/03/18. Sai major version được ghi DBG-001. Chưa chạy bài test nghiệp vụ hoặc FE/BE.

### Kết quả M1 thực tế (AI/tool chạy, base `6a26a99` + working tree)

| ID | Expected | Actual | Kết quả / evidence |
|---|---|---|---|
| TC-SETUP-01 | Boot 3, đúng dependencies, build/test pass trên JDK 21 | Boot 3.5.16; Web/JPA/Validation/JDBC 12.10.2.jre11; `mvnw -B clean verify` exit 0, 2 tests, 0 failure/error/skipped, JAR được repackage | PASS; [build-test.txt](evidence/2026-10-28-m1/build-test.txt), [dependencies.txt](evidence/2026-10-28-m1/dependencies.txt), [context-test.txt](evidence/2026-10-28-m1/context-test.txt) |
| TC-SETUP-02-BE-DB | Backend và DB đúng tên | artifact/application/`DB_NAME()` = A2PhanTuanThanh_SE1910 | PASS phần BE/DB; [sqlserver-before.txt](evidence/2026-10-28-m1/sqlserver-before.txt); phần FE NOT RUN |
| TC-M1-DB | App JDBC kết nối SQL Server thật, đúng DB, query thành công | Test assert product Microsoft SQL Server, `SELECT DB_NAME(), 1`; JPA factory mở. SQL session của JAR PID 44728: DB đúng, TCP 1433, NTLM, encrypt TRUE | PASS; [test source](../src/test/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910ApplicationTests.java), [SQL session](evidence/2026-10-28-m1/sqlserver-application-session.txt), [application log](evidence/2026-10-28-m1/application-start.txt) |
| TC-M1-HTTP | Executable JAR khởi động Tomcat, chưa có controller nên GET / trả 404 | `java -jar ... --server.port=0`, Tomcat port 53663; GET / = 404 | PASS phạm vi HTTP startup; [runtime-smoke.txt](evidence/2026-10-28-m1/runtime-smoke.txt) |
| TC-M1-SCHEMA | M1 không tạo bảng nghiệp vụ | sys.tables có 0 user tables trước và sau; `ddl-auto=none` | PASS phạm vi M1; [trước](evidence/2026-10-28-m1/sqlserver-before.txt), [sau](evidence/2026-10-28-m1/sqlserver-application-session.txt) |

[Inspection/manifest M1](evidence/2026-10-28-m1/inspection.md) ghi thời gian + checksum; baseline Boot 4 không bị thay đổi. Các kết quả này không nghiệm thu schema/CRUD/JPA persistence ở M2, SQL login, TLS với chứng chỉ tin cậy, IntelliJ hoặc FE/BE. OBS-001 vẫn có cảnh báo Maven snapshots/Mockito/JVM; build pass, chưa sửa các cảnh báo đó.

## 3. Setup và dữ liệu

| ID | REQ | Bước/đầu vào | Kỳ vọng | Cách kiểm tra | Trạng thái |
|---|---|---|---|---|---|
| TC-SETUP-01 | 02 | Kiểm tra POM/dependency và chạy test sau đổi Boot | Major version 3; context/build pass | Maven + kiểm tra source | PASS, bảng M1 |
| TC-SETUP-02 | 01 | Xem tên BE, folder FE, package npm và DB thực tế | Đúng quy ước trong design | File + SQL metadata | BE/DB PASS theo case con; FE NOT RUN, chưa PASS toàn hàng |
| TC-SETUP-03 | 19 | Sinh viên mở project trong IntelliJ, chạy main | Chạy được bằng JDK cấu hình, có evidence IDE thật | Manual | NOT RUN; điều kiện M1 còn thiếu, CLI không thay thế |
| TC-DB-01 | 03,05 | Khởi động JPA với database rỗng đúng tên | Tạo đủ 5 bảng, fields, PK/FK/unique đúng thiết kế đã chốt | Integration SQL Server | NOT RUN |
| TC-DB-02 | 03 | Tạo dữ liệu tiếng Việt, restart BE và đọc lại | Không mất dữ liệu hoặc lỗi Unicode | API + SQL Server | NOT RUN |
| TC-DB-03 | 05,17 | Lưu FK không tồn tại, duplicate link, trạng thái/role sai | Constraint/validation chặn; không lưu dữ liệu dở dang | Integration SQL Server | NOT RUN |

## 4. Xác thực và public news

| ID | REQ | Bước/đầu vào | Kỳ vọng | Cách kiểm tra | Trạng thái |
|---|---|---|---|---|---|
| TC-AUTH-01 | 07 | Login email/password đúng cho A, S1, S2 | 200, đúng role/identity, không có password/hash | API + React | NOT RUN |
| TC-AUTH-02 | 07,17 | Sai password, email không tồn tại; payload rỗng/sai format | 401 cho thông tin sai; 400 cho payload không hợp lệ; không lộ dữ liệu bí mật | API | NOT RUN |
| TC-AUTH-03 | 07 | Khách gọi từng nhóm API quản trị; token thiếu/sai/hết hạn | 401 | API trực tiếp | NOT RUN |
| TC-AUTH-04 | 07 | Staff gọi account API; Admin gọi module chỉ dành Staff | 403 cho cả read/write không được cấp quyền | API trực tiếp | NOT RUN |
| TC-AUTH-05 | 07 | Đổi role/xóa account khi token còn hạn rồi gọi lại | Token không giữ quyền đã bị thu hồi theo thiết kế | API | NOT RUN |
| TC-PUB-01 | 06 | Không login, list có active và inactive trong DB | Chỉ active xuất hiện | API + React | NOT RUN |
| TC-PUB-02 | 06 | Gọi detail active, inactive, ID không tồn tại | 200 cho active; 404 cho hai trường hợp còn lại | API + URL trực tiếp | NOT RUN |
| TC-PUB-03 | 06 | Bài active trong category inactive | Vẫn xuất hiện theo DEC-10 | API + React | NOT RUN |

## 5. Account và category

| ID | REQ | Bước/đầu vào | Kỳ vọng | Cách kiểm tra | Trạng thái |
|---|---|---|---|---|---|
| TC-ACC-01 | 08 | Admin create/read/update account, thử cả role 1/2 | Lưu đúng; response không có hash; không nhập password khi sửa thì giữ password cũ | API + UI + login lại | NOT RUN |
| TC-ACC-02 | 08,17 | Search tên/email, hoa thường, query rỗng/không khớp; email trùng khi create/update | Search đúng DEC-07; email trùng bị 409, không sinh bản ghi mới | API + UI | NOT RUN |
| TC-ACC-03 | 09 | Xóa account có bài active; lặp lại chỉ có bài inactive | Cả hai trả 409; account/bài không mất | API + SQL + UI | NOT RUN |
| TC-ACC-04 | 09 | Xóa U chưa tạo bài, không có tham chiếu | 204, account biến mất | API + UI | NOT RUN |
| TC-ACC-05 | 09 | Account không tạo bài nhưng có UpdatedByID tham chiếu | Xóa được, updater thành null, bài vẫn còn theo DEC-06 | Integration SQL Server | NOT RUN |
| TC-CAT-01 | 10 | Staff CRUD category, sửa description/parent/status | Giá trị lưu đúng và hiển thị lại | API + UI | NOT RUN |
| TC-CAT-02 | 10 | Search tên/mô tả, query rỗng, không khớp | Kết quả đúng DEC-07 | API + UI | NOT RUN |
| TC-CAT-03 | 11 | Xóa category có bài active; chỉ có bài inactive | 409 ở cả hai; category/bài còn nguyên | API + UI + SQL | NOT RUN |
| TC-CAT-04 | 10,17 | Parent không tồn tại; self-parent; vòng A→B→A | Từ chối rõ ràng, không lưu chu kỳ | API/service test | NOT RUN |
| TC-CAT-05 | 11 | Xóa category chưa có bài: không có con và có con | Xóa thành công; trường hợp có con giữ con và đặt parent null theo DEC-05 | API + SQL Server | NOT RUN |

## 6. News, tags và tài khoản cá nhân

| ID | REQ | Bước/đầu vào | Kỳ vọng | Cách kiểm tra | Trạng thái |
|---|---|---|---|---|---|
| TC-NEWS-01 | 12 | Create/read/update bài đủ field; gửi giả creator/updater/date | CRUD đúng; field audit do server quyết định; một category | API + UI + SQL | NOT RUN |
| TC-NEWS-02 | 12 | Thêm tag mới, sửa name/note, gắn tag tự do, bỏ tag; thử tag của bài khác | Link đúng; tag bài khác bị chặn theo DEC-01; không lưu trùng link | API + UI + SQL | NOT RUN |
| TC-NEWS-03 | 12 | Gửi request sửa bài với một tag/FK không hợp lệ | Bài và toàn bộ links rollback, không cập nhật một phần | Integration SQL Server | NOT RUN |
| TC-NEWS-04 | 12,06 | Search title/headline/content; đổi status active↔inactive | Management search đúng; public phản ánh status mới | API + UI | NOT RUN |
| TC-NEWS-05 | 12 | Xóa bài có nhiều tags | Xóa bài và links; account/category còn; tag có thể còn tự do | API + UI + SQL | NOT RUN |
| TC-SELF-01 | 13 | S1 xem/sửa name/email/password của mình | Lưu đúng; đổi password thì login mới thành công, password cũ thất bại | API + UI | NOT RUN |
| TC-SELF-02 | 13,07 | S1 gửi ID/role của S2 hoặc Admin trong payload profile | Không sửa người khác, không nâng role | API trực tiếp + SQL | NOT RUN |
| TC-SELF-03 | 14 | S1/S2 xem history; có cả active/inactive | Mỗi người chỉ thấy bài mình tạo; danh sách không phải audit revision | API + UI | NOT RUN |

## 7. UI, validation và tích hợp

| ID | REQ | Bước/đầu vào | Kỳ vọng | Cách kiểm tra | Trạng thái |
|---|---|---|---|---|---|
| TC-UI-01 | 15 | Create và update ở account/category/news, tổng 6 luồng | Đều mở popup dialog; lỗi giữ form để sửa | Browser | NOT RUN |
| TC-UI-02 | 16 | Delete ở cả 3 module: chọn Cancel rồi thử Confirm | Cancel không có DELETE; Confirm mới gửi request | Browser + Network | NOT RUN |
| TC-UI-03 | 04,17 | Submit kép, lỗi 400/409, hết phiên 401, mạng lỗi | Loading hợp lý; không gửi kép; thông báo đúng và dữ liệu không lệch | Browser + API | NOT RUN |
| TC-VAL-01 | 17 | Mỗi DTO: null/blank, max/max+1, email sai, password quá 72 byte | FE/BE cùng quy tắc; lỗi theo field, không 500 | Parameterized API/service tests | NOT RUN |
| TC-VAL-02 | 17 | ID chữ/âm/0, role/status ngoài miền; JSON sai kiểu và số thập phân cho integer | 400 hoặc 404 hợp đồng; không bị âm thầm chuyển kiểu/lưu sai | API trực tiếp | NOT RUN |
| TC-VAL-03 | 17 | FK không tồn tại, duplicate tag ID, race xóa/tạo tham chiếu hoặc trùng email | Rollback và 4xx có nghĩa; DB giữ toàn vẹn | Integration SQL Server | NOT RUN |
| TC-E2E-01 | 04,18 | React qua axios tới BE thật: login → từng module → CRUD/search → profile/history → public | Request/response và UI/DB nhất quán; CORS hoạt động | Browser + API + SQL | NOT RUN |
| TC-E2E-02 | 18 | Checkout sạch, cấu hình theo README, khởi động FE/BE với SQL Server và chạy regression | Người khác tái hiện được, không phụ thuộc file local thiếu trong repo | Manual integration | NOT RUN |
| TC-PROC-01 | GIT-01,GIT-02 | Đối chiếu remote owner, author email/profile, diff qua các mốc | Đúng cá nhân và lịch sử thể hiện phát triển thật | git log/show + GitHub | Đã đọc Git local/baseline/origin; GitHub attribution/quyền push NOT RUN, chưa PASS toàn hàng |
| TC-PROC-02 | DOC-01 | Kiểm tra REQ → code → tests → evidence → commit và AI/debug log | Không có PASS thiếu evidence, route/file/SHA giả hoặc mô tả chức năng chưa tồn tại là đã xong | Review docs/code/Git | Đã review phạm vi M1, xem inspection/manifest; full lifecycle M0-M8 NOT RUN |

## 8. Cách chạy khi có implementation

- M1 hiện tại: làm các bước SQL Server/native DLL/biến môi trường trong README rồi chạy `./mvnw.cmd -B clean verify`. Hai tests đều dùng context thật, cần SQL Server, không tự skip nếu thiếu kết nối.
- Unit/service tests: nghiệp vụ xóa bị chặn, profile ownership, query history và transaction boundary cần thiết.
- Controller/security tests: payload validation, response status, 401/403, không lộ password.
- Integration: SQL Server thật hoặc SQL Server test instance biệt lập; không dùng H2 làm bằng chứng thay thế hành vi SQL Server.
- API: Postman collection/CLI hoặc test tương đương có assertions. Postman CLI được khai báo đã cài; chưa chạy collection nào trong baseline.
- Browser: popup, confirmation, search, các role và Network axios. API PASS không tự động làm UI PASS.

Mỗi lần chạy ghi: thời gian, code SHA hoặc working-tree baseline, môi trường, dữ liệu vào, expected, actual, status và đường dẫn evidence. Các case lỗi có liên kết DBG tương ứng. Khi DEC đổi, cập nhật expected trước lần chạy tiếp theo và giữ lại lịch sử kết quả cũ.
