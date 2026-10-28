Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Development Plan

Cập nhật theo code/Git ngày 28/10/2026. Chi tiết kết quả chạy nằm trong [test-matrix](test-matrix.md) và [evidence](evidence/README.md).

## 1. Tiến độ hiện tại

- **M0:** có skeleton, phân tích yêu cầu và bộ docs. Commit `6a26a99`.
- **M1:** Boot 3.5.16/JDK 21, dependency Web/JPA/Validation/JDBC, cấu hình môi trường và kết nối SQL Server thật. Hai test pass; JAR chạy được. Commit `db2e8e6`.
- **M1 còn thiếu:** minh chứng sinh viên mở/chạy project trong IntelliJ, test `TC-SETUP-03`.
- **Chuẩn bị M2:** review/lưu riêng docs/header/quy ước code có sẵn vào `ea84544`, giữ nguyên baseline/M1 và Wrapper; không chạy lại ứng dụng trong commit docs này.
- **M2 đạt phạm vi kỹ thuật yêu cầu:** entity/repository/constraint và fixture test biệt lập; TC-DB-01..03 pass, 9/9 tests gồm regression M1, hai JVM JAR restart giữ schema/dữ liệu. Kết quả thực chạy, lỗi/retest ở [evidence M2](evidence/2026-10-28-m2/inspection.md); code/evidence nằm cùng commit tính năng này. DEC-01/03 vẫn cần xác nhận giảng viên.
- **M3–M8:** chưa triển khai. Chưa có Controller, Service nghiệp vụ, REST CRUD, auth/JWT hoặc React.

Repository: [TuanThanh-dev19/A2PhanTuanThanh_SE1910](https://github.com/TuanThanh-dev19/A2PhanTuanThanh_SE1910). Author local: `Phan Tuan Thanh <phanthanh19112005@gmail.com>`. Việc kiểm tra local không thay thế kiểm tra author gắn đúng profile trên GitHub.

Khảo sát trước khi có Git và trước khi đổi Boot được giữ tại [baseline](evidence/2026-10-28-baseline/inspection.md); kết quả M1 ở [evidence M1](evidence/2026-10-28-m1/inspection.md).

## 2. Milestone triển khai

| Mốc | Công việc | Hoàn thành khi |
|---|---|---|
| M2 | Entity, repository và database; REQ-03,05,17 phần persistence | Áp dụng tạm DEC-01/03 theo yêu cầu sinh viên, chưa xác nhận giảng viên; JPA tạo đủ 5 bảng; kiểm chứng Unicode, restart, FK/unique/rollback theo TC-DB-01..03 |
| M3 | Đăng nhập, phân quyền, API public; REQ-06,07 | Hai role đúng; kiểm tra 401/403; public chỉ trả tin active |
| M4 | Account và khởi tạo React/axios; REQ-04,08,09 | Admin CRUD/Search qua API và UI; có popup, xác nhận xóa và chặn account đã tạo bài |
| M5 | Category; REQ-10,11 | Staff CRUD/Search; parent hợp lệ; chặn xóa category có bài; popup/confirmation hoạt động |
| M6 | News/tags và giao diện public; REQ-06,12 | CRUD/Search bài cùng tags nhất quán; active/inactive đúng; popup/confirmation hoạt động |
| M7 | Profile và history; REQ-13,14 | Không sửa người khác/tự nâng role; mỗi Staff chỉ thấy history của mình |
| M8 | Tích hợp và bàn giao; REQ-18 | Chạy lại đầy đủ từ checkout sạch; sửa lỗi thật; docs, evidence và hướng dẫn chạy khớp code |

REQ-15,16,17 được kiểm tra ngay trong từng module. Sửa lỗi khi phát hiện, không cần chờ đến M8.

## 3. Quy trình cho mỗi tính năng

Mỗi file source, cấu hình, SQL và tài liệu mới hoặc được sửa đều có dòng `Author: Phan Tuan Thanh - CE190595` ở đầu, dùng cú pháp comment phù hợp. Giữ nguyên log/evidence lịch sử và Maven Wrapper; quy ước cho các chat tiếp theo nằm trong [AGENTS.md](../AGENTS.md).

1. Đọc yêu cầu và kiểm tra code/Git hiện tại.
2. Làm một phần có thể kiểm tra được: backend và frontend tương ứng nếu có UI.
3. Chạy test phù hợp; ghi expected, actual và kết quả.
4. Cập nhật docs liên quan, debug-log/AI log khi có nội dung mới; lưu evidence.
5. Review diff, kiểm tra code rõ tên/đúng trách nhiệm/dễ giải thích theo quy ước trong AGENTS.md rồi commit theo thay đổi thật. Sinh viên tự chạy, đọc và giải thích code trước khi nộp.

Ví dụ message khi thực sự có thay đổi tương ứng: `feat: add category management`, `fix: prevent deleting categories used by news`.

Một mốc có thể có nhiều commit. Không tạo lỗi, commit rỗng, đổi ngày hoặc chia ngược code hoàn chỉnh để giả tiến trình. Giữ các commit có giá trị thể hiện quá trình phát triển.

## 4. Git và tiêu chí bàn giao

Ghi mã REQ/test và đường dẫn evidence trong phần việc tương ứng. Với commit chưa tạo, dùng “cùng commit này”; ở lần cập nhật sau có thể điền SHA thật. Nếu test chạy trên working tree, ghi base commit và các file đã sửa.

Trước nộp, kiểm tra: đúng repo/author; đã push các commit cần nộp; đủ chức năng và test; không có PASS thiếu evidence; hướng dẫn chạy tái hiện được trên SQL Server.
