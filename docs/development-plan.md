Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Development Plan

## 1. Mục tiêu và cách dùng

Kế hoạch tổng thể cho toàn bộ Assignment 02: Spring Boot 3, SQL Server và ReactJS/axios; hoàn thành đúng chức năng, có kiểm thử và lịch sử Git thể hiện quá trình phát triển thật.

Bảng dưới là lộ trình chung, không phải bảng kết quả. Giữ ổn định qua các milestone; chỉ điều chỉnh khi phạm vi/thứ tự công việc thay đổi. Tiến độ thực tế ở [README](../README.md), kết quả ở [test-matrix](test-matrix.md).

## 2. Lộ trình từ đầu đến cuối

| Mốc | Nội dung | Đầu ra cần đạt |
|---|---|---|
| M0 | Phân tích đề, thiết kế và Git cá nhân | Requirements/design rõ; repository và author đúng; có baseline |
| M1 | Nền tảng backend | Boot 3/JDK 21, dependency cần thiết; kết nối SQL Server; chạy được trong IntelliJ |
| M2 | Entity, repository và database | Đủ 5 bảng/quan hệ; JPA tạo bảng; lưu tiếng Việt; giữ dữ liệu sau restart; kiểm tra constraint |
| M3 | Đăng nhập, phân quyền và API tin công khai | Email/password cho Admin/Staff; quyền ở BE; khách chỉ xem tin active |
| M4 | Account và khởi tạo React/axios | Admin CRUD/Search account; giao diện login; popup/confirmation; chặn xóa account đã tạo bài |
| M5 | Category | Staff CRUD/Search; parent hợp lệ; popup/confirmation; chặn xóa category có bài |
| M6 | News/tags và giao diện public | Staff CRUD/Search bài cùng tags; status và audit đúng; khách xem tin active |
| M7 | Hồ sơ và history | Staff sửa hồ sơ của mình; xem bài mình tạo; không tự nâng quyền |
| M8 | Kiểm thử tích hợp và bàn giao | React/API/SQL chạy cùng nhau; sửa lỗi thật; ảnh tự chụp, docs và hướng dẫn chạy đầy đủ |

CRUD gồm Read, Create, Update và Delete. Account/category/news đều có Search, popup Create/Update và confirmation Delete. Các điểm chưa rõ của đề ghi trong [requirements](requirements.md), không tự coi là đã được giảng viên duyệt.

## 3. Cách làm mỗi tính năng

1. Đọc yêu cầu và kiểm tra code hiện tại.
2. Code từng phần rõ ràng, đúng ba lớp; giữ header Author và quy ước trong [AGENTS.md](../AGENTS.md).
3. Chạy normal/negative test phù hợp, ghi Actual ngắn trong test-matrix.
4. Cập nhật code-flow/design khi luồng hoặc thiết kế thay đổi. Chỉ thêm debug log nếu thực sự gặp lỗi.
5. Chuẩn bị màn hình/luồng và dữ liệu để sinh viên tự chụp; ghi tên ảnh trong evidence index.
6. Review diff, commit phần việc có ý nghĩa; sinh viên tự chạy và giải thích code.

## 4. Git và bàn giao

Commit theo công việc thực tế, không theo chỉ tiêu số lượng. Một mốc có thể có nhiều commit; không tạo lỗi, đổi ngày hoặc chia ngược code hoàn chỉnh để giả tiến trình.

Trước nộp: đúng repository/account; đủ chức năng; test đã kiểm tra; ảnh khớp ứng dụng; README chạy được; code, tài liệu và lịch sử Git tương ứng. Kế hoạch này không cần sửa mỗi khi hoàn thành một mốc.
