Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Test Matrix

## Trạng thái

Mới kiểm tra baseline và nền tảng M1 bằng AI/tool. Có **2 automated tests pass ở M1** cùng kiểm tra startup/kết nối; các ca nghiệp vụ bên dưới là kế hoạch, chưa chạy. Không lấy kết quả Assignment 01 làm kết quả Assignment 02.

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

Test M1 chạy trên base `6a26a99` + working tree, sau đó lưu ở `db2e8e6`. Lần chỉnh docs này không chạy lại ứng dụng. Cảnh báo còn lại xem OBS-001 trong [debug-log](debug-log.md).

## Setup và database

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-SETUP-02 | Quy ước tên; REQ-01 | Xem BE, React folder/npm package và DB | Đúng tên theo requirements | BE/DB đạt; React chưa tạo | Partial |
| TC-SETUP-03 | IntelliJ; REQ-19 | Sinh viên mở project và chạy main bằng JDK 21 | Chạy được, có ảnh/log IDE thật | Chưa có evidence sinh viên chạy | Not run |
| TC-DB-01 | JPA tạo schema; REQ-03,05 | Khởi động với DB rỗng và entity | Đủ 5 bảng, fields, PK/FK/unique | Chưa chạy | Not run |
| TC-DB-02 | Unicode/persistence; REQ-03 | Lưu tiếng Việt, restart BE, đọc lại | Đúng tiếng Việt và dữ liệu còn | Chưa chạy | Not run |
| TC-DB-03 | Constraint; REQ-05,17 | Gửi FK sai, link trùng, role/status sai | Chặn lưu, không có dữ liệu dở dang | Chưa chạy | Not run |

## Login và phân quyền

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-AUTH-01 | Login hợp lệ; REQ-07 | Login Admin và hai Staff | 200, đúng identity/role, không lộ password/hash | Chưa chạy | Not run |
| TC-AUTH-02 | Credential/payload sai; REQ-07,17 | Sai email/password, thử form rỗng/sai format | Credential sai: 401; payload sai: 400 | Chưa chạy | Not run |
| TC-AUTH-03 | Token không hợp lệ; REQ-07 | Gọi API quản trị không token/sai/hết hạn | 401 | Chưa chạy | Not run |
| TC-AUTH-04 | Sai role; REQ-07 | Staff gọi account; Admin gọi module chỉ Staff | 403 cho read/write không được cấp quyền | Chưa chạy | Not run |
| TC-AUTH-05 | Thu hồi quyền; REQ-07 | Đổi role/xóa account rồi dùng token cũ | Không giữ quyền đã mất | Chưa chạy | Not run |

## Tin công khai

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-PUB-01 | Public list; REQ-06 | Không login, list với DB có cả hai status | Chỉ trả bài active | Chưa chạy | Not run |
| TC-PUB-02 | Public detail; REQ-06 | Mở URL active, inactive, không tồn tại | 200 active; 404 hai trường hợp còn lại | Chưa chạy | Not run |
| TC-PUB-03 | Category inactive; REQ-06 | Xem bài active trong category inactive | Vẫn công khai theo DEC-10 | Chưa chạy | Not run |

## Account CRUD và Search

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-ACC-01 | Create/Read/Update; REQ-08 | Admin tạo/sửa account cả role 1/2; thử sửa không gửi password | Lưu đúng, không trả hash; giữ password cũ khi không đổi | Chưa chạy | Not run |
| TC-ACC-02 | Search/unique email; REQ-08,17 | Search tên/email, hoa thường/trống/no-result; tạo/sửa email trùng | Search đúng DEC-07; trùng email: 409 | Chưa chạy | Not run |
| TC-ACC-03 | Delete bị chặn; REQ-09 | Xóa account có bài active, rồi chỉ có bài inactive | Cả hai 409, account/bài còn | Chưa chạy | Not run |
| TC-ACC-04 | Delete hợp lệ; REQ-09 | Xóa account chưa tạo bài, không tham chiếu | 204, account biến mất | Chưa chạy | Not run |
| TC-ACC-05 | Updater reference; REQ-09 | Xóa account không tạo bài nhưng có UpdatedByID | Xóa được, updater null, bài còn theo DEC-06 | Chưa chạy | Not run |

## Category CRUD và Search

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-CAT-01 | CRUD; REQ-10 | Staff tạo/đọc/sửa category, description/parent/status | Đúng record và field | Chưa chạy | Not run |
| TC-CAT-02 | Search; REQ-10 | Tìm tên/mô tả với hoa thường, trống và no-result | Đúng DEC-07, source không bị sửa | Chưa chạy | Not run |
| TC-CAT-03 | Delete bị chặn; REQ-11 | Xóa category có bài active, rồi chỉ inactive | 409, category/bài còn | Chưa chạy | Not run |
| TC-CAT-04 | Parent sai; REQ-10,17 | Parent giả, self-parent, vòng A→B→A | Bị từ chối, không lưu chu kỳ | Chưa chạy | Not run |
| TC-CAT-05 | Delete hợp lệ; REQ-11 | Xóa category chưa có bài, thử có/không category con | Xóa được; nếu có con, giữ con và parent null theo DEC-05 | Chưa chạy | Not run |

## News, Tags và Search

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-NEWS-01 | Create/Read/Update; REQ-12 | Lưu đủ field, gửi giả creator/updater/date | Một category; audit do server; update giữ creator/ngày tạo | Chưa chạy | Not run |
| TC-NEWS-02 | Tags; REQ-12 | Thêm/sửa/gắn/bỏ tag; thử tag bài khác/link trùng | Links đúng; chặn xung đột theo DEC-01 | Chưa chạy | Not run |
| TC-NEWS-03 | Transaction; REQ-12 | Sửa bài với một tag/FK không hợp lệ | Rollback cả bài và links | Chưa chạy | Not run |
| TC-NEWS-04 | Search/status; REQ-12,06 | Tìm title/headline/content; đổi active↔inactive | Search đúng; public phản ánh status mới | Chưa chạy | Not run |
| TC-NEWS-05 | Delete; REQ-12 | Xóa bài có nhiều tags | Xóa bài/links; giữ account/category/Tag | Chưa chạy | Not run |

## Profile và History

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-SELF-01 | Profile; REQ-13 | Sửa tên/email/password của mình, login lại | Lưu đúng; password mới được, cũ bị từ chối | Chưa chạy | Not run |
| TC-SELF-02 | Ownership/role; REQ-13,07 | Gửi ID người khác hoặc role giả qua profile | Không sửa người khác/tự nâng quyền | Chưa chạy | Not run |
| TC-SELF-03 | History; REQ-14 | Hai Staff xem bài mình tạo, cả hai status | Không lẫn bài người khác | Chưa chạy | Not run |

## Dialog, Validation và tích hợp

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-UI-01 | Popup; REQ-15 | Create và Update ở cả ba module | Cả 6 luồng dùng popup; lỗi giữ form | Chưa chạy | Not run |
| TC-UI-02 | Confirmation; REQ-16 | Ba module Delete: Cancel rồi Confirm | Cancel không có DELETE; Confirm mới gửi | Chưa chạy | Not run |
| TC-UI-03 | UI error/loading; REQ-04,17 | Submit kép, lỗi 400/409/401, mất mạng | Loading/feedback đúng; không ghi kép/lệch dữ liệu | Chưa chạy | Not run |
| TC-VAL-01 | Field validation; REQ-17 | Null/blank, max/max+1, email sai, password quá 72 byte | Lỗi theo field, FE/BE thống nhất, không 500 | Chưa chạy | Not run |
| TC-VAL-02 | Kiểu/miền; REQ-17 | ID chữ/âm/0, role/status sai, JSON sai kiểu, integer nhận thập phân | Không lưu hoặc âm thầm chuyển kiểu sai | Chưa chạy | Not run |
| TC-VAL-03 | Constraint/concurrency; REQ-17 | FK/tag sai; hai request cùng vi phạm FK/unique | Rollback, 4xx phù hợp, DB giữ toàn vẹn | Chưa chạy | Not run |
| TC-E2E-01 | Tích hợp; REQ-04,18 | React/axios gọi BE/SQL: login, CRUD/Search, profile/history, public | UI/request/DB nhất quán, CORS hoạt động | Chưa chạy | Not run |
| TC-E2E-02 | Chạy lại; REQ-18 | Checkout sạch, cấu hình README, regression trên SQL Server | Tái hiện được từ môi trường sạch | Chưa chạy | Not run |

## Git và tài liệu

| ID | Trường hợp | Các bước | Kết quả mong đợi | Actual | Trạng thái |
|---|---|---|---|---|---|
| TC-PROC-01 | Git cá nhân; GIT-01,GIT-02 | Đối chiếu remote, author/profile và diff qua milestone | Đúng account, lịch sử phát triển thật | Local repo/author/origin đã kiểm tra; còn attribution và toàn tiến trình | Partial |
| TC-PROC-02 | Docs; DOC-01 | Đối chiếu REQ → code → test → evidence → commit | Không có PASS thiếu minh chứng hoặc mô tả sai code | Đã review M1/docs; còn toàn bộ nghiệp vụ | Partial |

## Ghi kết quả cho các mốc tiếp theo

Chuẩn bị Admin, hai Staff, account/category có và không có bài, bài active/inactive và tag tự do/đã gắn. Fixture nghiệp vụ chưa tồn tại.

Ghi actual, ngày/môi trường, revision và evidence sau khi chạy. Test service/API kiểm tra rule, validation và quyền; dùng SQL Server thật cho FK/transaction/Unicode/persistence. Kiểm tra browser riêng cho popup/confirmation/axios; mock/H2 hoặc API Pass không thay thế kiểm tra tích hợp/UI. Có thể dùng Postman CLI khi có endpoint; lỗi thực tế liên kết debug-log.
