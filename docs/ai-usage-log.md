Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - AI Usage Log

Tài liệu ghi cách dùng AI, phần đã áp dụng và cách kiểm chứng. Sinh viên vẫn chịu trách nhiệm đọc, hiểu, chạy thử và giải thích code trước khi nộp.

| # | Prompt/Mục đích | Gợi ý hoặc phần AI hỗ trợ | Cách kiểm chứng | Phần sinh viên cần hiểu/chỉnh sửa |
|---|---|---|---|---|
| 1 / AI-001 | Đọc Assignment 02, phân tích yêu cầu và tiến trình Git | Requirement matrix, thiết kế, milestone, code-flow, test/evidence và nhật ký | Đọc đủ 5 trang PDF; kiểm tra source/Git/account; context test baseline 1 pass | Chốt DEC-01/03; phân biệt yêu cầu đề với lựa chọn thiết kế |
| 2 / AI-002 | Triển khai nền tảng M1 | Boot 3, dependency, datasource, JDBC test, script DB, mẫu môi trường và docs | AI/tool chạy `clean verify`: 2 pass; JAR/Tomcat chạy; SQL session đúng DB, TCP/NTLM/mã hóa, 0 bảng | Giải thích cấu hình và JDBC test; tự chạy IntelliJ/build; sinh viên đã chọn Windows Authentication trong chat |
| 3 / AI-003 | Đơn giản hóa docs đang quá phức tạp | Rút gọn README/docs, bỏ phần lặp và hướng dẫn Git đã cũ, cập nhật SHA M1 | Kiểm tra link, giữ REQ/DEC/test ID; checksum xác nhận code và evidence không đổi | Đọc lại nội dung rút gọn; xác nhận vẫn đủ để trình bày |
| 4 / AI-004 | Cập nhật docs theo hình thức/hướng của Assignment 01 | Đồng bộ tiêu đề/mục; code-flow theo Purpose → Input → Trigger → Transform → Output; bảng AI/debug; test có Steps/Expected/Actual; evidence index | Đối chiếu bộ docs tham chiếu, code/Git hiện tại, link/ID và checksum; thay đổi chỉ ở tài liệu | Hiểu khác biệt A1 mock storage và A2 API/SQL; review bản docs mới |
| 5 / AI-005 | Xác định bước tiếp theo và thêm tên tác giả đầu file | Ghi quy ước trong AGENTS.md; thêm author header vào source/config/SQL/docs; xác định M2 là persistence | Kiểm tra header, cú pháp XML, liên kết và diff; đối chiếu checksum để giữ nguyên evidence/Wrapper | Review thay đổi; M2 và DEC-01/03 vẫn chưa được triển khai/chốt |
| 6 / AI-006 | Quy ước code dễ hiểu, tường minh, tránh phức tạp không cần thiết | Bổ sung quy tắc tên/method, control flow, mapping, abstraction và React state trong AGENTS.md; đồng bộ design và bước review | Kiểm tra header, liên kết và diff; chỉ thay đổi hướng dẫn | Áp dụng khi làm M2 trở đi; giải thích được lý do nếu chọn cách làm phức tạp hơn |
| 7 / AI-007 | Triển khai M2 theo PDF và kiểm chứng SQL Server thật | Review/commit riêng docs có sẵn; 5 entity, composite key, 5 repository, validation/constraint, fixture repository test, runner build/SQL/JAR restart; đồng bộ docs/evidence | Đọc đủ 5 trang PDF và hình tr.3; SQL2022 Windows auth; test/retest có log thật, gồm lỗi import, assertion, alias SQL và nullability sau update (DBG-002..005). Kết quả cuối: [inspection M2](evidence/2026-10-28-m2/inspection.md) | Giải thích @MapsId, lazy/no cascade, unique TagID tạm, Bean Validation/SQL constraint, transaction rollback, length vật lý của nvarchar(max) và test DB biệt lập. Tự chạy IntelliJ; hỏi giảng viên DEC-01/03 |

## Phạm vi và minh chứng

| Mục | Ghi nhận |
|---|---|
| Công cụ / ngày | Codex và terminal; 28/10/2026, UTC+07:00 |
| Baseline | Khảo sát lúc chưa có Git; skeleton/docs được lưu sau đó ở `6a26a99`. [Evidence](evidence/2026-10-28-baseline/inspection.md) |
| M1 | Commit `db2e8e6`; test chạy trên base `6a26a99` + working tree. [Evidence](evidence/2026-10-28-m1/inspection.md) |
| Quyết định sinh viên đã xác nhận | Dùng Windows Authentication cho SQL Server; AI/tool tạo DB rỗng và tải DLL vào thư mục tmp bị ignore |
| Chưa có bằng chứng sinh viên tự làm | Chạy IntelliJ/build/demo và giải thích code; không chuyển kết quả AI/tool thành xác nhận sinh viên đã chạy |
| Chưa kiểm chứng ở M1 | SQL login, TLS chứng chỉ tin cậy, checkout máy khác; entity/CRUD/auth/React; DEC-01/03 chưa chốt |
| Các lần chỉnh docs/header 3–6 | Đã review/lưu riêng trước M2 ở ea84544; không chạy lại ứng dụng trong commit docs; giữ log/evidence lịch sử. Phiên 5 chỉ thêm comment tác giả vào source/config, phiên 6 bổ sung quy ước code |
| M2 / DEC | Áp dụng tạm unique TagID và script DB rỗng + JPA tạo bảng/FK theo yêu cầu sinh viên trong chat; chưa có xác nhận giảng viên. DEC-02 là lựa chọn triển khai; SQL login/TLS tin cậy/máy khác chưa kiểm chứng |
| M2 / fixture và dữ liệu | Fixture chỉ ở test source, password ngẫu nhiên không phải credential thật; không seed startup, không drop/reset DB. DB test được giữ lại; sửa một lần nullability DB bài tập do lỗi M2 tạo ra, 0 dòng trước/sau |
| M2 / kết quả cuối | Run-05: clean verify 9 tests pass, schema/nullable đúng sau context và hai JVM restart, HTTP 404 và session thật; tất cả thao tác do AI/tool chạy. Chưa có xác nhận sinh viên tự chạy/giải thích; chưa push |

## Cách ghi các lần tiếp theo

Thêm một hàng với mục đích, phần AI hỗ trợ, kết quả đã kiểm tra và phần sinh viên cần hiểu. Ghi rõ gợi ý đã sửa/bỏ và lý do; liên kết debug-log nếu AI tạo lỗi. Không lưu secret hoặc tự ghi sinh viên đã chạy/hiểu khi chưa có xác nhận.

## AI-009 — M3 (28-10-2026)

AI triển khai login BCrypt/JWT bằng Spring Security/Nimbus, quyền role riêng, public news và seed demo chỉ thêm thiếu; giữ riêng thay đổi có sẵn. Kiểm chứng: 20 tests pass trên SQL Server (9 cũ + 11 M3), JAR HTTP/restart đạt, seed hai lần không thêm trùng; lỗi JPQL NCLOB đã sửa và retest. Sinh viên cần giải thích token/role DB hiện tại, 401/403/404, DTO không hash và tự thao tác/chụp Postman theo evidence index. Chưa làm CRUD/profile/history/React, chưa push.
