# Phân tích yêu cầu

Ngày lập: 28/10/2026, múi giờ Asia/Bangkok (UTC+07:00).
Trạng thái: baseline phân tích; chưa triển khai nghiệp vụ.

## 1. Nguồn và cách diễn giải

- **S1:** `Assignment 02_Implement CRUD Application with RESTful Web Service and ReactJS.pdf`, 5 trang, tại thư mục `D:/FA2026/SBA301/`. Đã đọc văn bản và xem ảnh từng trang, đặc biệt sơ đồ trang 3.
- **S2:** tiêu chí C2 (2.0 điểm) do sinh viên cung cấp: repository cá nhân/đúng account; lịch sử phát triển thật; có tiến hóa tạo bài → chức năng → sửa lỗi/cải tiến; không đánh giá máy móc theo số commit. C2 không xuất hiện trong PDF 5 trang đã đọc.
- **S3:** ảnh sinh viên cung cấp yêu cầu `docs/evidence/`, `ai-usage-log.md`, `code-flow.md`, `debug-log.md`, `design.md`, `requirements.md`, `test-matrix.md`. Ảnh chỉ thể hiện cấu trúc, không quy định nội dung từng file.
- **S4:** yêu cầu trực tiếp của sinh viên: tài liệu, code và commit phải tương ứng; cấu trúc chuẩn Spring Boot cho SBA301.

Phần Introduction mô tả bối cảnh hệ thống tin tức. Approval workflow, scheduling, analytics và publishing đa kênh được nhắc như khả năng tổng quát, không phải danh sách chức năng bắt buộc của phần Main Functions. Không đưa những chức năng đó vào phạm vi triển khai nếu chưa có yêu cầu bổ sung.

## 2. Danh mục bắt buộc và tiêu chí nghiệm thu

| ID | Yêu cầu | Nguồn | Tiêu chí nghiệm thu |
|---|---|---|---|
| REQ-01 | Tên BE, project React và database theo `A2StudentName_ClassCode` | S1 tr.5 | Dùng `A2PhanTuanThanh_SE1910`; tên folder React giữ mẫu, tên package npm dùng chữ thường do quy tắc npm |
| REQ-02 | Spring Boot **3**, Spring REST API, MVC, ba lớp với Repository | S1 tr.1,2,5 | POM dùng 3.x; HTTP qua Controller → Service → Repository; controller không trực tiếp truy vấn DB |
| REQ-03 | SQL Server và Spring Data JPA tạo cấu trúc lưu trữ | S1 tr.1,2,4 | Dữ liệu tồn tại trên SQL Server; JPA/Hibernate sinh bảng/quan hệ; có minh chứng khởi tạo và lưu dữ liệu |
| REQ-04 | ReactJS gọi API bằng axios | S1 tr.1,2,4 | Màn hình hoạt động với BE thật; có request/response quan sát được |
| REQ-05 | Mô hình dữ liệu theo sơ đồ | S1 tr.3 | Có SystemAccount, Category, NewsArticle, Tag, NewsTag và đủ các trường trong sơ đồ |
| REQ-06 | Khách xem tin không cần đăng nhập; chỉ tin active | S1 tr.4 | List/detail công khai không trả tin inactive, kể cả gọi URL trực tiếp |
| REQ-07 | Đăng nhập email/password; Admin=1, Staff=2 | S1 tr.3,4 | Đăng nhập đúng/sai được xử lý; BE kiểm tra quyền, không chỉ ẩn nút trên FE |
| REQ-08 | Admin quản lý account: Read/Create/Update/Delete/Search | S1 tr.4 | Đủ 5 thao tác trên API và React; Staff/khách không truy cập được |
| REQ-09 | Cấm xóa account đã tạo bất kỳ bài nào | S1 tr.4 | Bài active hoặc inactive đều chặn xóa; account chưa tạo bài có thể xóa khi không có vấn đề khác |
| REQ-10 | Staff quản lý category: Read/Create/Update/Delete/Search | S1 tr.4 | Đủ thao tác API/React, kể cả CategoryDescription, ParentCategoryID, IsActive |
| REQ-11 | Cấm xóa category đang được bất kỳ bài nào sử dụng | S1 tr.4 | Bài active hoặc inactive đều chặn xóa; không cascade xóa bài |
| REQ-12 | Staff quản lý news gồm tags: Read/Create/Update/Delete/Search | S1 tr.4 | Lưu/sửa/xóa bài và liên kết tag nhất quán; bài thuộc đúng một category |
| REQ-13 | Staff quản lý hồ sơ của mình | S1 tr.4 | Xem/sửa hồ sơ cá nhân; không sửa người khác hoặc tự nâng role qua payload |
| REQ-14 | Staff xem lịch sử bài do mình tạo | S1 tr.4 | Lọc theo CreatedByID của tài khoản đăng nhập; có cả active/inactive; không lẫn bài người khác |
| REQ-15 | Create/Update account, category, news bằng popup dialog | S1 tr.4 | Cả 6 luồng tạo/sửa dùng dialog trên React; lỗi validation hiển thị tại form |
| REQ-16 | Delete luôn có confirmation | S1 tr.4 | Xóa account/category/news yêu cầu xác nhận; Cancel không gửi lệnh xóa |
| REQ-17 | Validate kiểu dữ liệu cho mọi trường | S1 tr.2 | DTO/BE kiểm tra type, giá trị và FK; FE hỗ trợ nhập đúng; payload sai không gây lỗi 500 |
| REQ-18 | Chạy và kiểm thử tương tác FE/BE | S1 tr.2 | Có kết quả API, thao tác trình duyệt và minh chứng SQL Server, không chỉ test mock |
| REQ-19 | Tạo Spring Initializr project và dùng IntelliJ IDEA | S1 tr.2 | Mở/chạy project trong IntelliJ và lưu minh chứng thực tế; không suy luận công cụ khởi tạo chỉ từ tên file |
| GIT-01 | Repository cá nhân, đúng Git account | S2 | Owner remote, account đăng nhập và author email được kiểm tra; commit trên GitHub gắn đúng account |
| GIT-02 | Lịch sử phản ánh tiến trình phát triển thật | S2 | Có baseline, chức năng tăng dần, kiểm thử và sửa lỗi/cải tiến có căn cứ; giữ timestamp thực tế |
| DOC-01 | Đủ bộ docs theo ảnh và đồng bộ với code/Git | S3,S4 | Mỗi yêu cầu truy vết được đến code/test/evidence/commit; trạng thái chưa làm thể hiện rõ |

Tất cả REQ hiện ở trạng thái **chưa nghiệm thu**. REQ-01 mới có tên backend đúng mẫu; không đồng nghĩa frontend/database đã tồn tại. Test skeleton PASS không đóng REQ-02 vì đang dùng Boot 4.

## 3. Ma trận quyền dự kiến

| Chức năng | Khách | Admin (1) | Staff (2) |
|---|---|---|---|
| Xem tin active công khai | Có | Có | Có |
| Quản lý account | Không | Có | Không |
| Quản lý category | Không | Không | Có |
| Quản lý news/tags | Không | Không | Có |
| Hồ sơ Staff của mình | Không | Không áp dụng trong phạm vi | Có |
| Lịch sử bài do mình tạo | Không | Không áp dụng trong phạm vi | Có |

Đề không cấp Admin mọi quyền của Staff. Không mặc định role 1 kế thừa role 2. Đề cũng không nói Staff chỉ được sửa bài của chính mình; thiết kế ban đầu cho Staff quản lý news chung, còn history bắt buộc chỉ của mình.

## 4. Điểm mơ hồ và quyết định dự kiến

| ID | Vấn đề | Hướng xử lý có thể kiểm tra |
|---|---|---|
| DEC-01 | Tr.3 có bảng nối NewsTag; mô tả chữ nói một tag thuộc 0 hoặc 1 bài, trong khi sơ đồ không thể hiện unique TagID | Giữ cả 5 bảng. Dự kiến dùng khóa ghép NewsArticleID+TagID và unique TagID để giữ giới hạn trong phần chữ. Cần xác nhận giảng viên trước khi chốt mapping; nếu cho phép tái sử dụng tag thì bỏ unique và cập nhật test |
| DEC-02 | PDF không nêu kiểu, chiều dài, nullable, chiến lược ID | Các kiểu/giới hạn trong design là lựa chọn triển khai, không phải trích dẫn đề; cần đồng bộ entity, DTO, SQL và React |
| DEC-03 | “Use Spring Data JPA to create the database” | Dự kiến tạo database rỗng đúng tên trong SQL Server, sau đó JPA/Hibernate tạo bảng, FK và constraint. Ghi rõ hai bước; nếu giảng viên yêu cầu tự động CREATE DATABASE thì bổ sung bootstrap được kiểm chứng |
| DEC-04 | Admin cần truy cập nhưng phần đăng nhập chỉ ghi Member (Staff) | Một API đăng nhập cho cả hai role; khởi tạo admin bằng cấu hình demo, không hard-code mật khẩu trong source |
| DEC-05 | Category có parent; đề chỉ nêu chặn xóa khi được bài sử dụng | Category chưa có bài nhưng có category con: dự kiến cho xóa và chuyển parent của con thành null trong transaction; giữ nguyên các category con |
| DEC-06 | News có UpdatedByID; điều kiện cấm xóa account chỉ dựa vào người tạo | Khi account không tạo bài nhưng có UpdatedByID tham chiếu: dự kiến gỡ tham chiếu updater thành null trước khi xóa, không xóa bài |
| DEC-07 | Không định nghĩa search | Dự kiến search chuỗi không phân biệt hoa thường: account theo tên/email; category theo tên/mô tả; news theo title/headline/content. Query rỗng trả toàn bộ danh sách được phép xem |
| DEC-08 | “News history” không định nghĩa audit/versioning hay thùng rác | Triển khai danh sách bài còn tồn tại với CreatedByID=current account; có cả hai status. Audit revision và lịch sử bài đã xóa không tự thêm vào phạm vi |
| DEC-09 | Không quy định cơ chế auth, hết phiên, password hashing | Chọn Spring Security + JWT bearer và BCrypt; là quyết định thiết kế, không phải yêu cầu JWT bắt buộc trong PDF |
| DEC-10 | Không nói category inactive có ẩn bài active không | Public chỉ áp dụng NewsStatus=1 theo đề; không tự thêm điều kiện IsActive của category để ẩn bài |

Ưu tiên làm rõ DEC-01 và DEC-03 trong giai đoạn mô hình dữ liệu. Tài liệu không tuyên bố đã được giảng viên chấp thuận các quyết định này.

## 5. Phạm vi hoàn thành

Hoàn thành khi toàn bộ REQ được kiểm chứng trên ứng dụng thật, các DEC ảnh hưởng nghiệp vụ đã được chốt/ghi nhận, bộ docs có số liệu thực tế, và repo cá nhân lưu đầy đủ tiến trình. Không đặt mục tiêu số commit tối thiểu; không dùng tính năng ngoài đề để thay thế chức năng bắt buộc còn thiếu.
