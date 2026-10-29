Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Test Matrix

## Trạng thái

M8 ngày 29-10-2026: source xuất từ HEAD `ec94a5a` kèm sửa cấu hình M8, không dùng target/node_modules cũ; `clean verify` **57 tests pass**, `npm ci`, lint/build đạt. HTTP bổ sung và Chrome trên React production build + JAR + SQL Server riêng đạt. Còn sinh viên chạy IntelliJ/chụp ảnh; README và evidence index vẫn mô tả M3, chưa được cập nhật trong lượt này.

M3 ngày 28-10-2026: AI/tool chạy `scripts/test-m3.ps1` trên SQL Server thật, **20 tests pass (2 M1 + 7 M2 + 11 M3), 0 failure/error/skipped**. JAR HTTP và restart persistence đạt; hai lần seed demo giữ counts 2/1/2. React/CRUD/profile/history chưa chạy. Report local ở `target/surefire-reports`; ảnh sinh viên tự chụp theo evidence index.

**Pass:** đã chạy đủ và đạt; **Fail:** đã chạy nhưng sai; **Not run:** chưa chạy; **Partial:** mới đạt một phần; **Blocked:** bị chặn, cần ghi nguyên nhân. Một hàng nhiều nhánh chỉ Pass khi đủ các nhánh.

## Baseline và nền tảng M1

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-BASE-01 | Context skeleton trước M1 | Chạy `mvnw.cmd test` trên baseline | Context khởi tạo không lỗi | Boot 4.1.2, 1 test pass; chỉ xác nhận skeleton | Pass (baseline) |
| TC-SETUP-01 | Boot/dependency đúng; REQ-02 phần nền tảng | Xem POM, chạy `mvnw.cmd -B clean verify` | Boot 3/JDK 21, Web/JPA/Validation/JDBC; build/test thành công | Boot 3.5.16, 2 tests pass, 0 failure/error/skipped | Pass |
| TC-SETUP-02-BE-DB | Tên BE và DB | Xem artifact/application name và query DB_NAME() | A2PhanTuanThanh_SE1910 | BE/DB đều đúng tên; chưa có React | Pass phần BE/DB |
| TC-M1-DB | JDBC kết nối SQL thật | Chạy JDBC test; đối chiếu session JAR | Product Microsoft SQL Server, đúng DB và query thành công | Assertions đạt; session đúng DB, TCP/NTLM/mã hóa | Pass |
| TC-M1-HTTP | JAR khởi động | Chạy JAR với `--server.port=0`, GET `/` | Tomcat chạy; `/` trả 404 do chưa controller | Startup đạt, GET `/` = 404 | Pass (startup) |
| TC-M1-SCHEMA | Chưa tạo bảng ở M1 | Đọc ddl-auto, đếm user tables trước/sau | `none`, 0 bảng nghiệp vụ | Đúng, chưa nghiệm thu JPA tạo bảng | Pass phạm vi M1 |

Minh chứng: [baseline report](evidence/2026-10-28-baseline/context-test.txt), [M1 inspection](evidence/2026-10-28-m1/inspection.md), [build](evidence/2026-10-28-m1/build-test.txt), [SQL session](evidence/2026-10-28-m1/sqlserver-application-session.txt), [HTTP smoke](evidence/2026-10-28-m1/runtime-smoke.txt).

Kết quả lịch sử M1 ở commit `db2e8e6`; không dùng hàng TC-M1-SCHEMA (0 bảng lúc M1) để mô tả schema hiện tại.

M2 đã chạy lại 2 tests M1; JAR vẫn khởi động và kết nối SQL Server. IntelliJ còn chờ ảnh sinh viên tự chạy.

## Setup và database

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-SETUP-02 | Quy ước tên; REQ-01 | Xem BE, React folder/npm package và DB | Đúng tên theo requirements | BE/DB A2PhanTuanThanh_SE1910; frontend/A2PhanTuanThanh_SE1910, npm a2phantuanthanh-se1910 | Pass |
| TC-SETUP-03 | IntelliJ; REQ-19 | Sinh viên mở project và chạy main bằng JDK 21 | Chạy được, có ảnh/log IDE thật | Chưa có evidence sinh viên chạy | Not run |
| TC-DB-01 | JPA tạo schema; REQ-03,05 | Khởi động DB test rỗng, xem bảng/khóa và kiểm tra lại sau restart | Đủ 5 bảng và đúng trường/ràng buộc | JPA tạo đúng schema; NOT NULL vẫn đúng sau restart | Pass |
| TC-DB-02 | Unicode/persistence; REQ-03 | Lưu dữ liệu qua repository, khởi động lại rồi đọc | Tiếng Việt, quan hệ và dữ liệu còn đúng | Dữ liệu đọc đúng sau mở lại context và hai lần chạy JAR | Pass |
| TC-DB-03 | Constraint/rollback; REQ-05,17 phần persistence | Thử FK/link/email/role/status/field sai; giao dịch có một bước lỗi | Dữ liệu sai bị chặn; không lưu một phần | Constraint/validation đạt; title và tag mới được rollback khi link lỗi | Pass |

Minh chứng kỹ thuật đã lưu: [kết quả M2](evidence/2026-10-28-m2/inspection.md), [report 7 tests persistence](evidence/2026-10-28-m2/run-05/fpt.edu.a2phantuanthanh_se1910.persistence.NewsPersistenceTests.txt). Các lỗi đã sửa ghi ngắn trong [debug-log](debug-log.md); không cần chép lại các lần chạy vào bảng này.

## Login và phân quyền

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-AUTH-01 | Login hợp lệ; REQ-07 | Login Admin và Staff | 200, đúng identity/role, không lộ password/hash | 200; đúng ID/role, JWT decode đúng, không hash/password | Pass |
| TC-AUTH-02 | Credential/payload sai; REQ-07,17 | Sai email/password, thử form rỗng/sai format | Credential sai: 401; payload sai: 400 | 401 credential sai; 400 blank/email/JSON sai và quá 72 byte UTF-8 | Pass |
| TC-AUTH-03 | Token không hợp lệ; REQ-07 | Gọi API quản trị không token/sai/hết hạn | 401 | 401 khi thiếu/hỏng/hết hạn/sai chữ ký/sai issuer | Pass |
| TC-AUTH-04 | Sai role; REQ-07 | Staff gọi account; Admin gọi module chỉ Staff | 403 cho read/write không được cấp quyền | 403 sai role cho GET/POST/PUT/DELETE; đúng role GET qua filter, 404 vì chưa controller | Pass |
| TC-AUTH-05 | Thu hồi quyền; REQ-07 | Đổi role/xóa account rồi dùng token cũ | Không giữ quyền đã mất | Đổi role trong DB: áp quyền mới; xóa account test: token cũ 401 | Pass |

## Tin công khai

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-PUB-01 | Public list; REQ-06 | Không login, list với DB có cả hai status | Chỉ trả bài active | 200; chỉ active; search title/headline/content, trim/case/literal wildcard đạt | Pass |
| TC-PUB-02 | Public detail; REQ-06 | Mở URL active, inactive, không tồn tại | 200 active; 404 hai trường hợp còn lại | 200 active có content/tags; 404 inactive và ID không tồn tại; ID sai 400 | Pass |
| TC-PUB-03 | Category inactive; REQ-06 | Xem bài active trong category inactive | Vẫn công khai theo DEC-10 | Bài active trong category inactive vẫn list/detail 200 | Pass |

## Account CRUD và Search

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-ACC-01 | Create/Read/Update; REQ-08 | Admin tạo/sửa account cả role 1/2; thử sửa không gửi password | Lưu đúng, không trả hash; giữ password cũ khi không đổi | API/React tạo, đọc và sửa đạt; response chỉ DTO; không gửi/null/empty password giữ hash, mật khẩu cũ vẫn login được | Pass |
| TC-ACC-02 | Search/unique email; REQ-08,17 | Search tên/email, hoa thường/trống/no-result; tạo/sửa email trùng | Search đúng DEC-07; trùng email: 409 | Search tên/email, hoa thường/trống/no-result đạt; Create/Update email trùng đều 409, không lưu sai | Pass |
| TC-ACC-03 | Delete bị chặn; REQ-09 | Xóa account có bài active, rồi chỉ có bài inactive | Cả hai 409, account/bài còn | SQL/API: có active hoặc chỉ còn inactive đều 409; account, bài và updater còn nguyên; React giữ popup và báo lý do | Pass |
| TC-ACC-04 | Delete hợp lệ; REQ-09 | Xóa account chưa tạo bài, không tham chiếu | 204, account biến mất | React Hủy: 0 DELETE; Confirm: 1 DELETE và 204; SQL account biến mất, bài vẫn còn | Pass |
| TC-ACC-05 | Updater reference; REQ-09 | Xóa account không tạo bài nhưng có UpdatedByID | Xóa được, updater null, bài còn theo DEC-06 | React/API xóa updater-only thành công; SQL UpdatedByID của cả bài active/inactive thành NULL, CreatedByID và bài còn nguyên | Pass |

## Category CRUD và Search

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-CAT-01 | CRUD; REQ-10 | Staff tạo/đọc/sửa category, description/parent/status | Đúng record và field | API/Chrome/SQL: DTO, prefill, tạo/sửa và các field đúng; ID giữ nguyên | Pass |
| TC-CAT-02 | Search; REQ-10 | Tìm tên/mô tả với hoa thường, trống và no-result | Đúng DEC-07, source không bị sửa | Regression API/Chrome đạt; tạo/sửa/xóa tải lại theo keyword đang Search | Pass |
| TC-CAT-03 | Delete bị chặn; REQ-11 | Xóa category có bài active, rồi chỉ inactive | 409, category/bài còn | API: từng status đều 409, category/bài và parent của con nguyên vẹn; Chrome giữ confirmation và báo lỗi | Pass |
| TC-CAT-04 | Parent sai; REQ-10,17 | Parent giả, self-parent, vòng A→B→A | Bị từ chối, không lưu chu kỳ | API trả 400 cho parent giả/self/vòng 2 và 3 cấp; Chrome lỗi vòng giữ form, sửa lại lưu được | Pass |
| TC-CAT-05 | Delete hợp lệ; REQ-11 | Xóa category chưa có bài, thử có/không category con | Xóa được; nếu có con, giữ con và parent null theo DEC-05 | 204; API giữ cả con/cháu/bài của con; Chrome/SQL xác nhận con còn, parent null; Cancel 0 DELETE | Pass |

## News, Tags và Search

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-NEWS-01 | Create/Read/Update; REQ-12 | Lưu đủ field, gửi giả creator/updater/date | Một category; audit do server; update giữ creator/ngày tạo | API/Chrome/SQL: tạo/sửa đủ field, cả status; audit server UTC, giữ creator/ngày tạo; giả audit không ảnh hưởng | Pass |
| TC-NEWS-02 | Tags; REQ-12 | Thêm/sửa/gắn/bỏ tag; thử tag bài khác/link trùng | Links đúng; chặn xung đột theo DEC-01 | API/Chrome: thêm/sửa/gắn/bỏ tags đạt; tag bài khác 409, ID trùng 400; tháo tag giữ bản ghi | Pass |
| TC-NEWS-03 | Transaction; REQ-12 | Sửa bài với một tag/FK không hợp lệ | Rollback cả bài và links | SQL riêng: lỗi tag/FK không đổi dữ liệu; CHECK tạm buộc lỗi sau flush ở Create/Update đều rollback bài, tags, links và audit | Pass |
| TC-NEWS-04 | Search/status; REQ-12,06 | Tìm title/headline/content; đổi active↔inactive | Search đúng; public phản ánh status mới | Search title/headline/content, trim/hoa thường/wildcard đạt; Chrome đổi active/inactive phản ánh ở public list/detail | Pass |
| TC-NEWS-05 | Delete; REQ-12 | Xóa bài có nhiều tags | Xóa bài/links; giữ account/category/Tag | API/Chrome/SQL: 204 xóa bài/links; account/category/Tag còn; tag trở lại danh sách chọn; Cancel không DELETE | Pass |

## Profile và History

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-SELF-01 | Profile; REQ-13 | Sửa tên/email/password của mình, login lại | Lưu đúng; password mới được, cũ bị từ chối | HTTP/Chrome/SQL: tên/email khớp sau reload; password trống giữ hash, đổi password lưu BCrypt, login cũ 401/mới 200 | Pass |
| TC-SELF-02 | Ownership/role; REQ-13,07 | Gửi ID người khác hoặc role giả qua profile | Không sửa người khác/tự nâng quyền | PUT nhận ID/role giả vẫn chỉ sửa Staff trong JWT, role giữ 2; SQL account khác không đổi; khách 401/Admin 403 | Pass |
| TC-SELF-03 | History; REQ-14 | Hai Staff xem bài mình tạo, cả hai status | Không lẫn bài người khác | HTTP/Chrome/SQL: hai Staff chỉ thấy bài theo CreatedByID, cả active/inactive; bài chỉ sửa không lọt vào history, bài của mình do người khác sửa vẫn còn | Pass |

## Dialog, Validation và tích hợp

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-UI-01 | Popup; REQ-15 | Create và Update ở cả ba module | Cả 6 luồng dùng popup; lỗi giữ form | M8 Chrome: 6 dialog Create/Update account/category/news đạt; field rỗng/409/400 giữ form; Cancel sửa account không lưu | Pass |
| TC-UI-02 | Confirmation; REQ-16 | Ba module Delete: Cancel rồi Confirm | Cancel không có DELETE; Confirm mới gửi | M8 Chrome + HTTP/SQL: từng module Cancel 0 DELETE, dblclick Confirm 1 DELETE; đúng record bị xóa | Pass |
| TC-UI-03 | UI error/loading; REQ-04,17 | Submit kép, lỗi 400/409/401, mất mạng | Loading/feedback đúng; không ghi kép/lệch dữ liệu | M8: Create/Update ba module mỗi lần 1 request; FK bị xóa sau khi chọn trả 400 giữ popup, 409 giữ confirmation; xóa account làm JWT 401 về login; loading/lỗi mạng mô phỏng rồi retry đạt | Pass |
| TC-VAL-01 | Field validation; REQ-17 | Null/blank, max/max+1, email sai, password quá 72 byte | Lỗi theo field, FE/BE thống nhất, không 500 | M8 HTTP: field tối đa lưu được; 47 ca field/kiểu sai gồm null/blank/max+1/email/password UTF-8 trả 400, counts 5 bảng không đổi; FE báo field trống/giữ form | Pass |
| TC-VAL-02 | Kiểu/miền; REQ-17 | ID chữ/âm/0, role/status sai, JSON sai kiểu, integer nhận thập phân | Không lưu hoặc âm thầm chuyển kiểu sai | M8 HTTP: ID 0/âm/chữ/thập phân/tràn long, role/status/FK sai kiểu và JSON lỗi đều 400; ID hợp lệ không tồn tại 404; không lưu sai | Pass |
| TC-VAL-03 | Constraint/concurrency; REQ-17 | FK/tag sai; hai request cùng vi phạm FK/unique | Rollback, 4xx phù hợp, DB giữ toàn vẹn | M8 HTTP: hai request trùng email và hai request tranh tag đều 201/409, SQL đúng 1 record/link; 57 regression tests gồm CHECK sau flush xác nhận rollback bài/tag/link/audit | Pass |
| TC-E2E-01 | Tích hợp; REQ-04,18 | React/axios gọi BE/SQL: login, CRUD/Search, profile/history, public | UI/request/DB nhất quán, CORS hoạt động | M8 Chrome trên production build + JAR/SQL riêng: đủ luồng 3 role; tiếng Việt/tags/audit/profile/history khớp SQL; public đổi theo status/xóa; CORS đúng origin, sai origin 403; restart validate giữ dữ liệu/JWT | Pass |
| TC-E2E-02 | Chạy lại; REQ-18 | Checkout sạch, cấu hình README, regression trên SQL Server | Tái hiện được từ môi trường sạch | M8: export HEAD ec94a5a + sửa config M8, không target/node_modules cũ; Windows Auth/DLL theo README trên cùng máy; clean verify 57 tests, npm ci/lint/build đạt; JAR chạy và restart validate đạt | Pass |

## Git và tài liệu

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-PROC-01 | Git cá nhân; GIT-01,GIT-02 | Đối chiếu remote, author/profile và diff qua milestone | Đúng account, lịch sử phát triển thật | M8: origin TuanThanh-dev19/A2PhanTuanThanh_SE1910; HEAD local/remote ec94a5a trùng; GitHub attribution và tài khoản kết nối TuanThanh-dev19; author/email local khớp profile, lịch sử M0–M7 theo phần việc | Pass |
| TC-PROC-02 | Docs; DOC-01 | Đối chiếu REQ → code → test → ảnh/commit | Tài liệu đúng code; ảnh đúng thao tác; chỉ Pass khi đã kiểm tra | M8: chức năng/API/React và test đã đối chiếu; README/design/evidence index còn nội dung tiến độ cũ; chưa có ảnh sinh viên trong screenshots, IntelliJ còn Not run | Partial |

## Ghi kết quả cho các mốc tiếp theo

Fixture repository đã có trong test source và dùng trên DB test riêng; chưa phải account đăng nhập/UI. Chuẩn bị dữ liệu demo phù hợp khi có chức năng.

Sau khi chạy, ghi Actual và trạng thái ngắn trong hàng tương ứng. Kiểm tra Service/API và browser khi có chức năng; lỗi thật mới thêm debug log. Ảnh do sinh viên tự chụp, không đánh dấu có ảnh khi chưa có file.

## Kiểm tra bổ sung M3

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M3-SEED | Seed hai lần, dữ liệu cũ đã sửa | Thêm thiếu, BCrypt, không ghi đè/trùng | Test SQL rollback đạt; JAR chạy hai lần, counts 2 account/1 category/2 news giữ nguyên | Pass |
| TC-M3-CORS | Origin 5173 và origin khác | Cho phép origin cấu hình, chặn origin khác | Preflight 200 đúng origin, 403 origin khác | Pass |
| TC-M3-KEY | JWT key sai Base64/quá ngắn | Không cho khởi tạo key | IllegalStateException ở cả hai nhánh | Pass |

Các kết quả M3 ở `api/M3ApiTests.java`, chạy SQL Server DB biệt lập; kiểm tra token bằng JWT thật, không mock auth/controller CRUD. Chưa kiểm tra UI/Postman do sinh viên thao tác; HTTP thật đã thử bằng PowerShell. Test profile/history/CRUD ở các hàng trên vẫn Not run.

## M4 - Phần 1: danh sách account và React

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M4-API | GET accounts với Admin/Staff/khách | 200 DTO không password/hash; 403; 401 | SQL Server thật: 21 tests pass; so khớp đủ 4 field với repository, đúng quyền; mvnw verify thành công | Pass |
| TC-M4-HTTP | JAR thật trên DB SQL Server riêng | Admin 200, Staff 403, khách 401; DTO đúng DB | HTTP lần lượt 200/403/401; 2 account, đủ 4 field khớp SQL, không password/hash | Pass |
| TC-M4-FE-AUTH | Chrome: login sai/đúng, logout, URL trực tiếp | Báo sai credential; Admin vào accounts; Staff bị chặn; khách về login | Sai password báo lỗi; Admin /accounts, Staff /staff; reload giữ phiên; logout và khách mở /accounts về /login | Pass |
| TC-M4-FE-SESSION | 401 và token hết hạn | Xóa phiên, về login kèm thông báo | Đổi key backend rồi thử lại: 401 về login; token thật hạn 15 giây tự kết thúc phiên | Pass |
| TC-M4-FE-LIST | React gọi axios tới backend thật | ID/tên/email/role khớp; loading và lỗi rõ | 2 dòng khớp API/SQL; chờ SQL hiện loading; dừng backend hiện lỗi và nút Thử lại | Pass |
| TC-M4-FE-EMPTY | UI khi API trả [] (mô phỏng) | Thông báo rỗng, không hiện bảng | Proxy tạm trả []; React hiện 0 tài khoản và Chưa có tài khoản nào; login vẫn qua backend thật | Pass |
| TC-M4-FE-BUILD | Cài theo lockfile, lint/build | Cài/build thành công, không lỗi lint | npm ci, npm run lint (--max-warnings 0), npm run build đạt; npm audit 0 lỗ hổng | Pass |

## M4 - Phần 2: Account CRUD và Search

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M4P2-BASE | Chạy lại Phần 1 trước khi sửa | Login và bảng account hoạt động | Chrome/BE thật: khách về login, Admin đọc đúng 2 account SQL | Pass |
| TC-M4P2-SEARCH | Tên/email, hoa thường, trống, không khớp | Lọc đúng; xóa keyword trả đủ | 13 API tests pass trên SQL; Chrome tìm ADMIN.M3 được 1 dòng, no-match hiện thông báo, xóa ô trả 2 dòng; FE lint/build đạt | Pass |
| TC-M4P2-CREATE | Popup, field sai, trùng email, role 1/2 và BCrypt | 201 DTO an toàn; lỗi 400/409 giữ form; hash trong DB | 15 API tests pass; Chrome form trống báo field, email trùng giữ nội dung, sửa email tạo thành công và bảng thêm dòng; SQL xác nhận BCrypt; lint/build đạt | Pass |
| TC-M4P2-UPDATE | Detail/prefill, sai field/email trùng, giữ/đổi password | DTO không hash; lỗi giữ form; password trống giữ hash, mới login được | 17 API tests pass; Chrome prefill đúng, ô password rỗng, lỗi tên/trùng email giữ popup; sửa tên/email khớp SQL và login mật khẩu cũ được; đổi password qua React: cũ bị từ chối, mới login Staff thành công; lint/build đạt | Pass |
| TC-M4P2-DELETE | Cancel/Confirm, creator và updater-only | Cancel không DELETE; 204 hợp lệ; 409 creator; DEC-06 giữ bài | Chrome/SQL thật: counter 0 khi Hủy, 1 khi Confirm; creator bị chặn và giữ popup; xóa updater đặt NULL cho 2 bài còn nguyên | Pass |
| TC-M4P2-VERIFY | Regression, quyền và build | Backend/FE chạy; khách 401, Staff 403 mọi thao tác | mvnw verify: 29 tests pass, 0 fail/error/skipped; lint/build FE đạt; HTTP thật list/detail/POST/PUT/DELETE đúng 401/403, Admin list 200. Delete UI thử cổng 5174, proxy chỉ đếm request rồi chuyển nguyên tới BE | Pass |

## M5 - Phần 1: đọc và tìm kiếm Category (28-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M5P1-API | List/detail và quyền | Staff 200 DTO phẳng đủ 6 field, cả active/inactive; Admin 403; khách 401; ID thiếu/sai 404/400 | SQL Server riêng: assertions khớp repository, parent có/null, mô tả null; HTTP JAR list/detail 200/403/401 và thiếu ID 404 đạt | Pass |
| TC-M5P1-SEARCH | Tên/mô tả, hoa thường, trim, trống/no-match | Lọc đúng; trống trả đủ; không khớp trả []; không sửa dữ liệu | API đạt cả literal wildcard và q > 200 trả 400; Chrome tên tiếng Việt/mô tả khác hoa thường đúng, clear trả đủ 3 dòng | Pass |
| TC-M5P1-UI | Menu/route Staff, loading/empty/no-result/error | Staff xem bảng; Admin bị chặn; khách về login; feedback và retry đúng | Chrome + JAR/SQL thật: đủ field; Admin URL về /accounts, khách về /login; chờ khóa SQL hiện loading; DB test rỗng/no-match đúng thông báo; dừng/chạy lại BE và Thử lại đạt | Pass |
| TC-M5P1-BUILD | Backend regression và frontend | Build/test/lint thành công | mvnw verify: 32 tests, 0 failure/error/skipped; npm run lint và npm run build đạt | Pass |

## M5 - Phần 2: tạo, sửa và xóa Category (28-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M5P2-BASE | Chạy lại Phần 1 | Staff đọc/search được | 23 API tests đạt; Chrome/JAR/SQL riêng: bảng và search DEMO đúng | Pass |
| TC-M5P2-CREATE | Tạo hợp lệ/sai field/parent, quyền và search | 201 DTO; sai 400; khách 401/Admin 403; lỗi giữ form; lưu giữ search | 3 Category API tests đạt, 23 regression API đạt; Chrome form rỗng báo lỗi, tạo root/child inactive khớp SQL, parent đủ dù search không khớp, lưu đóng popup và giữ keyword; lint/build đạt | Pass |
| TC-M5P2-UPDATE | Prefill, sửa hợp lệ/sai field, parent giả/self/vòng, ID và search | 200 giữ ID; sai 400/404, dữ liệu không đổi; quyền 401/403 | 6 Category API tests đạt cả vòng 2/3 cấp; Chrome prefill đúng inactive/parent, tên rỗng/vòng báo lỗi giữ form; sửa tên/cha/status khớp SQL, giữ search và loại dòng không còn khớp; lint/build đạt | Pass |
| TC-M5P2-DELETE | Cancel/Confirm, có bài active/inactive và DEC-05 | Cancel không DELETE; 204 hợp lệ; 409 có bài; giữ con/cháu/bài | 9 Category API tests đạt; Chrome + JAR/SQL: Cancel 0 DELETE, xóa leaf/parent 204, con còn và parent null; category có bài 409 giữ popup, SQL bài active/inactive còn nguyên | Pass |
| TC-M5P2-SUBMIT | Nhấn kép Create/Update/Delete và Search | Mỗi thao tác 1 request; giữ keyword sau lưu/xóa | Proxy tạm chuyển nguyên request: dblclick lần lượt 1 POST 201, 1 PUT 200, 1 DELETE 204; SQL không trùng; search tạo/sửa/xóa và danh sách parent độc lập đạt | Pass |
| TC-M5P2-VERIFY | Regression, HTTP quyền và build | Test/build/lint đạt; mọi thao tác chỉ Staff | mvnw verify: 41 tests, 0 failure/error/skipped; lint/build đạt; HTTP POST/PUT/DELETE khách 401/Admin 403. Test dùng DB riêng, không reset dữ liệu có sẵn | Pass |

## M6 - Phần 1: API đọc và tìm kiếm News (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P1-READ | Staff list/detail, cả status và creator khác | DTO đúng DB; mới trước, cùng ngày ID giảm; chi tiết đủ tags/audit, xử lý null | MockMvc/JWT thật + SQL Server riêng: 10 field list và 15 field detail khớp DB; tags có note/null, bài không tags và updater null đạt; không trả password/hash/email | Pass |
| TC-M6P1-SEARCH | Title/headline/content, hoa thường/trim, wildcard, trống/no-match và giới hạn | Search literal; trống trả đủ, không khớp []; q > 200 trả 400; không sửa bài/links | Test SQL đạt tiếng Việt và % _ [ !; tìm được inactive; 200 ký tự hợp lệ, 201 trả 400; dữ liệu giữ nguyên | Pass |
| TC-M6P1-AUTH | Khách/Admin, ID sai/thiếu và public | Khách 401/Admin 403; ID sai 400, thiếu 404; public chỉ active | List/detail đúng 401/403; ID 0/âm/chữ/thập phân/tràn long 400, không tồn tại 404; public list chỉ active, detail inactive 404 | Pass |
| TC-M6P1-VERIFY | Backend regression và build | Các test cũ/mới đạt, đóng gói thành công | 4 NewsReadApiTests đạt; cập nhật kỳ vọng quyền M3 từ 404 sang 200 cho API News đã có; mvnw verify: 45 tests, 0 failure/error/skipped, BUILD SUCCESS | Pass |

## M6 - Phần 2: React đọc/tìm News (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P2-UI | Staff đọc, mở chi tiết, tìm không khớp và clear | Bảng đủ status; chi tiết tags/audit; clear trả đủ | Chrome + JAR/SQL riêng: 2 bài, dialog đúng, no-result và clear đạt; npm lint/build đạt | Pass |

## M6 - Phần 3: API chọn tags (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P3-TAGS | Tag tự do/thuộc bài đang sửa; search và quyền | Không trả tag bài khác; q/ID sai bị chặn | SQL Server + JWT: đủ nhánh chọn/search tiếng Việt/wildcard/empty; 400/404/401/403 đúng; 28 tests đạt | Pass |

## M6 - Phần 4: tạo News và tags (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P4-API | Tạo cả status, gắn/sửa tag tự do, tag mới và audit | 201/Location; audit từ server; sai field/FK/tag/quyền không lưu | SQL Server riêng: 3 write tests đạt, trim/Unicode/audit đúng; 400/409/401/403 giữ dữ liệu; mvnw verify 49 tests đạt | Pass |
| TC-M6P4-UI | Popup trống, tạo có tag và nhấn kép | Lỗi giữ popup; lưu xong đóng và reload; một POST | Chrome + JAR/SQL riêng: validation đạt, bài/tag/note hiện ở detail, dblclick đúng 1 POST; npm lint/build đạt | Pass |

## M6 - Phần 5: sửa News và tags (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P5-API | Staff khác sửa bài; đổi category/status; giữ audit tạo; gắn/sửa/bỏ tag; request sai | 200; creator/ngày tạo giữ nguyên; tag tháo còn tồn tại; lỗi không đổi dữ liệu | 5 write tests SQL đạt; 400/404/409/401/403 đúng; public phản ánh đổi status. Audit UTC chuẩn microsecond để response khớp datetime2(6) | Pass |
| TC-M6P5-UI | Prefill, lỗi giữ form, sửa tags/status và search | Dữ liệu đúng, một PUT khi nhấn kép, giữ keyword | Chrome/JAR/SQL: prefill và validation đạt; rename/thêm tag, inactive, 1 PUT và giữ search; lint/build đạt | Pass |

## M6 - Phần 6: xóa News (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P6-DELETE | Xóa bài cả hai status, có nhiều tags; quyền/ID và confirmation | 204; chỉ xóa bài/links; Cancel không DELETE | 6 write tests SQL đạt, account/category/tags còn; 401/403/404/400 đúng. Chrome Cancel 0 DELETE; dblclick Confirm 1 DELETE; tag được chọn lại trong form tạo; lint/build đạt | Pass |

## M6 - Phần 7: giao diện public (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P7-PUBLIC | Khách đọc/tìm tin, URL inactive/sai, màn hình hẹp, token cũ | Chỉ active; public không gửi Bearer; giao diện đọc được | Chrome/JAR/SQL: list/detail/search/clear đạt; inactive/ID chữ báo không tìm thấy; viewport 390px không tràn; token giả cũ không gắn vào cả list/detail, vẫn đọc được; lint/build đạt | Pass |

## M6 - Phần 8: tích hợp và regression (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M6P8-ATOMIC | Hai request tranh tag; lỗi DB sau flush | Chỉ một bài nhận tag; Create/Update lỗi rollback mọi thay đổi | 8 write tests SQL đạt: concurrent 201/409, chỉ thêm một bài; CHECK tạm gây lỗi link sau flush, cả Create/Update rollback bài/tag/link/audit; dữ liệu đúng ở giới hạn field tối đa | Pass |
| TC-M6P8-E2E | React tạo/sửa/status/xóa, khách đọc và mất mạng | UI/API/DB nhất quán; text hiển thị an toàn; retry được | Chrome với hai phiên: active đọc được, inactive/đã xóa không đọc được, tag sửa đúng; nội dung script chỉ là text; lỗi mạng mô phỏng rồi retry đạt; SQL xác nhận tags còn, links null, account/category nguyên vẹn | Pass |
| TC-M6P8-VERIFY | Backend regression, frontend và Author | Test/build/lint đạt; file sửa có Author; không thêm evidence | mvnw verify: 54 tests, 0 failure/error/skipped; frontend lint/build đạt; Author kiểm tra ở các file M6; chỉ cập nhật test-matrix trong docs | Pass |

## M7 - Phần 1: API đọc hồ sơ (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M7P1-READ | Hai Staff đọc GET /api/profile, gửi ID người khác qua query | Chỉ trả hồ sơ theo principal; DTO 4 field, không password/hash; no-store | MockMvc/JWT thật + SQL Server riêng: từng Staff nhận đúng ID/tên/email/role trong DB; accountId/id giả không đổi hồ sơ; đủ 4 field và Cache-Control: no-store | Pass |
| TC-M7P1-AUTH | Khách, token sai/hết hạn, Admin; account đổi thông tin/role hoặc bị xóa | 401/403 đúng; cùng token đọc dữ liệu hiện tại, mất quyền khi role đổi/account bị xóa | Khách/token sai/hết hạn 401, Admin 403; tên/email mới hiện với token cũ; đổi role sang Admin 403, xóa account 401 | Pass |
| TC-M7P1-VERIFY | Backend regression và đóng gói | Test/build đạt; kiểm tra mới giữ local | mvnw verify: 57 tests, 0 failure/error/skipped, BUILD SUCCESS; 3 test profile mới đạt. Chưa chạy UI hoặc sửa profile/history | Pass |

## M7 - Phần 2: giao diện xem hồ sơ (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M7P2-READ | Staff mở My profile, reload; hai Staff ở hai phiên | Axios gửi Bearer; đủ ID/tên/email/role từ API, đúng người đăng nhập | Chrome + JAR/SQL Server riêng: 200, 4 field khớp SQL; menu active, reload giữ phiên; hai Staff nhận đúng hồ sơ riêng | Pass |
| TC-M7P2-STATE | Loading, mất mạng và backend 403 | Có loading/lỗi rõ; Try again tải lại, không hiện dữ liệu cũ khi lỗi | Trì hoãn request thật hiện loading/aria-busy; mô phỏng lỗi mạng hiện lỗi, retry đọc BE; đổi role trong SQL nhận 403, khôi phục role rồi retry đạt | Pass |
| TC-M7P2-AUTH | Khách/Admin mở URL; JWT hết hạn và timer phiên | Khách về login, Admin về accounts; 401/hết phiên xóa session và thông báo | Chrome: khách/Admin không gửi GET profile, menu Admin không có My profile; JWT ký thật hết hạn nhận 401 và timer đều đưa về login kèm notice | Pass |
| TC-M7P2-LAYOUT | Email dài và màn hình nhỏ | Email xuống dòng, trang không tràn ngang | Hai phiên Chrome: email 254 ký tự khớp SQL, xuống dòng; viewport 390px/320px không tràn; không có lỗi JavaScript chưa xử lý | Pass |
| TC-M7P2-BUILD | Frontend lint/build | Không lỗi, đóng gói thành công | npm run lint (--max-warnings 0) và npm run build đạt; kiểm tra UI chạy local, không tạo file test/evidence | Pass |

## M7 - Phần 3: sửa hồ sơ (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M7P3-API | PUT profile hợp lệ/sai, trùng email, giả ID/role và đổi password | Chỉ sửa mình; 400/409 không lưu; password trống giữ hash, mới lưu BCrypt | HTTP/JAR/SQL riêng: normal/trim/giới hạn field, password ngắn/blank/73 byte và email trùng đúng; password 72 byte UTF-8 hợp lệ; ID/role giả không ảnh hưởng account khác/quyền; cũ 401/mới 200 | Pass |
| TC-M7P3-UI | Popup, Cancel, lỗi, nhấn kép, session/header và mobile | Prefill không password; lỗi giữ form; một PUT; tên/email mới giữ sau reload | Chrome: Cancel không lưu, field trống/409 giữ popup; dblclick 1 PUT; header/profile/session khớp SQL, đổi password đạt; popup 390px không tràn; lint/build đạt | Pass |

## M7 - Phần 4: history và kiểm tra hoàn tất (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M7P4-API | History theo creator, search, status và sort | Chỉ bài mình tạo, cả status; mới trước/cùng ngày ID giảm; q tối đa 200, wildcard literal | HTTP/JAR/SQL riêng: ID/order khớp SQL; creator khác dù mình sửa bị loại; người khác sửa bài mình vẫn giữ; tìm title/headline/content, trim/hoa thường/%/_/[/! đạt; q 201 trả 400, 200 hợp lệ; ID giả không đổi owner | Pass |
| TC-M7P4-UI | Menu/route, hai Staff, rỗng, search/clear và detail | Danh sách đúng người; rỗng/no-result rõ; detail có tags; lỗi retry được | Chrome: hai phiên khớp SQL, reload giữ phiên; empty/no-result/search/clear đạt; detail bài inactive có tags; loading/lỗi mạng mô phỏng rồi retry đạt; viewport 390px cuộn bảng bên trong, không tràn trang | Pass |
| TC-M7P4-AUTH | Khách/Admin, hết phiên và bài bị xóa | 401/403 đúng; route UI chặn; 401 về login; bài xóa không còn history | API khách 401/Admin 403; Chrome khách về login/Admin về accounts, menu ẩn; JWT ký thật hết hạn xóa phiên và báo notice; detail bài đã xóa báo 404, reload bỏ dòng, SQL giữ tag | Pass |
| TC-M7-FINAL | Backend regression và frontend | Test/build/lint đạt; chỉ cập nhật test-matrix, test giữ local | mvnw verify chạy tuần tự: 57 tests, 0 failure/error/skipped, BUILD SUCCESS; npm run lint/build đạt; các kiểm tra HTTP/Chrome/SQL M7 đạt; không tạo evidence hay file test mới | Pass |

## M8 - Kiểm thử tích hợp và bàn giao (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-M8-CLEAN | Source sạch, dependency và build | Không phụ thuộc target/node_modules cũ | Export HEAD ec94a5a, áp dụng sửa config M8; clean verify 57 tests, 0 failure/error/skipped; npm ci/lint/build đạt, audit 0; chạy production build bằng Vite preview | Pass |
| TC-M8-CONFIG | Password DB từ môi trường | Source không có password mặc định; Windows Auth vẫn chạy | Đổi fallback SQLSERVER_PASSWORD thành rỗng; clean verify/JAR kết nối SQL Server bằng Windows Authentication đạt | Pass |
| TC-M8-API | Boundary, kiểu, quyền, CORS, đồng thời và ownership | 4xx đúng; không lưu sai, không nâng quyền | HTTP thật: 47 ca field/kiểu sai, max hợp lệ, ID/JSON/q sai đạt; quyền GET/POST/PUT/DELETE đúng; email/tag concurrent 201/409; giả ID/role không đổi owner; profile/history/public khớp SQL | Pass |
| TC-M8-UI | Luồng Admin → Staff → khách → profile/history → xóa | React/HTTP/SQL nhất quán; dialog/confirmation đúng | Chrome/JAR/SQL riêng: Create/Update/Search ba module, tags, profile/password/history/public đạt; guards xóa cả status; Cancel 0 DELETE, Confirm 1; tiếng Việt kiểm tra byte SQL, script chỉ hiện text; mobile 390px không tràn | Pass |
| TC-M8-RECOVERY | Loading, 400/409/401, mạng và restart | Lỗi rõ; dữ liệu giữ nguyên, retry/reload hoạt động | FK stale 400 giữ popup; 409 giữ form/confirmation; deleted-account JWT 401 về login; mô phỏng mất mạng rồi retry đạt; JVM mới validate giữ DTO từ SQL, JWT cũ và React hoạt động | Pass |
| TC-M8-HANDOVER | IntelliJ, ảnh sinh viên và tài liệu khớp tiến độ | Đủ thông tin và ảnh thật trước nộp | Chưa có ảnh sinh viên; TC-SETUP-03 chưa chạy. README/design/evidence index còn mô tả M3; lượt này chỉ sửa test-matrix theo AGENTS.md, chưa nghiệm thu bàn giao toàn bộ | Partial |

## Sửa cấu hình khởi động local (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-LOCAL-CONFIG | Nạp host, SQL login và JWT từ file local | Kết nối SQL Server; secret không nằm trong source Git | application.local.properties được git check-ignore xác nhận; mvnw -DskipTests package đạt; JAR chạy Java 24 không đặt SQLSERVER/JWT trong môi trường, validate schema đạt, khởi động cổng 8080 | Pass |
| TC-LOCAL-HTTP | Gọi API sau khởi động | Public 200; account cần đăng nhập 401 | GET /api/public/news trả 200 và JSON hợp lệ; GET /api/accounts không token trả 401 | Pass |

## Đơn giản hóa lưu news/tags (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-SIMPLE-NEWS | Đọc, tạo, sửa, xóa news/tags; validation và quyền | Giữ hành vi CRUD, audit, status, tags và 400/401/403/404/409 | mvnw -Dtest=NewsWriteApiTests,NewsReadApiTests test trên SQL Server riêng: 13 tests, 0 failure/error/skipped; BUILD SUCCESS | Pass |
| TC-SIMPLE-TAG | Hai request tranh cùng tag; lỗi DB sau khi lưu | Unique TagID chặn hai bài cùng tag; lỗi rollback bài/tag/link/audit | Test hiện có đạt: concurrent 201/409, chỉ thêm một bài; lỗi CHECK khi Create/Update giữ nguyên dữ liệu | Pass |

## Giao diện nút điều hướng (29-10-2026)

| ID | Trường hợp | Expected | Actual | Trạng thái |
|---|---|---|---|---|
| TC-NAV-BUTTON | Public news, Management/Sign in, Read article, All news, Read public news | Nền/viền/bo góc đồng bộ, hover/focus rõ, chuyển trang đúng | Chrome: kiểm tra CSS, hover và Tab/Shift+Tab đạt; đăng nhập Admin/Staff, đọc bài/quay lại, chuyển public/management đúng; không lỗi JavaScript | Pass |
| TC-NAV-MOBILE | Login, public list/detail, accounts và staff ở 320px; lint/build | Không tràn ngang; frontend kiểm tra đạt | Chrome: scrollWidth = viewport 320px ở cả 5 trang; npm run lint và npm run build đạt | Pass |
