Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - AI Usage Log

Ghi ngắn phần AI hỗ trợ và cách kiểm tra. Sinh viên vẫn cần đọc, chạy và giải thích code; kết quả AI/tool chạy không có nghĩa sinh viên đã tự chạy.

| Lần | Mục đích / phần AI hỗ trợ | Kiểm chứng | Sinh viên cần hiểu |
|---|---|---|---|
| AI-001 | Đọc đề, phân tích yêu cầu và lập docs | Đối chiếu 5 trang PDF, skeleton test pass | Phạm vi A2 và điểm chưa rõ |
| AI-002 | M1: Boot 3, datasource và test JDBC | 2 tests pass, JAR kết nối SQL thật | Cấu hình môi trường/kết nối |
| AI-003 | Rút gọn docs | Kiểm tra link và yêu cầu còn đủ | Đọc lại tài liệu ngắn |
| AI-004 | Theo cách trình bày project trước | Đối chiếu bố cục, giữ nội dung A2 | Khác biệt mock A1 và API/SQL A2 |
| AI-005 | Header Author và quy ước cho chat sau | Kiểm tra header/cú pháp | Áp dụng cho file do project viết |
| AI-006 | Quy ước code dễ hiểu | Review hướng dẫn, chưa đổi nghiệp vụ | Tên rõ, xử lý tường minh, tránh phức tạp thừa |
| AI-007 | M2: entity, repository và kiểm tra persistence | Lần cuối 9 tests pass; Unicode, constraint, rollback, restart đạt | Quan hệ, khóa ghép và repository; DEC-01/03 chưa được giảng viên xác nhận |
| AI-008 | Tài liệu ngắn, plan ổn định, ảnh do sinh viên tự chụp | Kiểm tra link, mã test và giữ nguyên source/log cũ | Dùng README xem tiến độ; tự thao tác/chụp theo evidence index |

M1 thuộc `db2e8e6`, docs/header trước M2 thuộc `ea84544`, M2 thuộc `ec46274`. Kết quả kỹ thuật cũ giữ tại [evidence](evidence/README.md); lần chỉnh docs này không chạy lại ứng dụng và chưa tạo commit.

Sinh viên đã chọn Windows Authentication và cách áp dụng tạm DEC-01/03. Chưa có ảnh/xác nhận sinh viên tự chạy IntelliJ. Chỉ thêm hàng mới khi có phần hỗ trợ đáng kể, ghi kết quả thật; không kể lại từng lệnh hoặc thao tác nhỏ.

## AI-009 — M3 (28-10-2026)

AI triển khai login BCrypt/JWT bằng Spring Security/Nimbus, quyền role riêng, public news và seed demo chỉ thêm thiếu; giữ riêng thay đổi có sẵn. Kiểm chứng: 20 tests pass trên SQL Server (9 cũ + 11 M3), JAR HTTP/restart đạt, seed hai lần không thêm trùng; lỗi JPQL NCLOB đã sửa và retest. Sinh viên cần giải thích token/role DB hiện tại, 401/403/404, DTO không hash và tự thao tác/chụp Postman theo evidence index. Chưa làm CRUD/profile/history/React, chưa push.
