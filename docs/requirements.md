Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Requirement Specification

## 1. Thông tin tài liệu

| Thuộc tính | Giá trị |
|---|---|
| Bài tập | SBA301 Assignment 02 - CRUD Application with RESTful Web Service and ReactJS |
| Project / database | `A2PhanTuanThanh_SE1910` |
| Công nghệ bắt buộc | Spring Boot 3, SQL Server, Spring Data JPA, ReactJS và axios |
| Tiến độ thực hiện | Xem [README](../README.md) và [test-matrix](test-matrix.md) |
| Nguồn yêu cầu | Assignment 02 (5 trang); C2 và ảnh cấu trúc docs do sinh viên cung cấp trong chat |
| Cách trình bày | Theo bộ docs Assignment 01 của sinh viên; nội dung nghiệp vụ theo Assignment 02 |

## 2. Mục tiêu

Xây dựng hệ thống quản lý tin tức có backend REST API ba lớp và React client. Khách đọc tin active; Admin quản lý account; Staff quản lý category, news/tags, hồ sơ và bài do mình tạo.

Bài làm cần có quá trình phát triển cá nhân rõ ràng. Code, tài liệu, test và minh chứng phải khớp với từng phần đã thực hiện.

## 3. Giả định và điểm cần làm rõ

Các quyết định dưới đây giữ mã DEC để đối chiếu với thiết kế/test. Không mặc định chúng đã được giảng viên xác nhận.

| ID | Nội dung | Trạng thái |
|---|---|---|
| DEC-01 | Giữ NewsTag với PK ghép NewsArticleID+TagID và unique TagID: một tag thuộc 0 hoặc 1 bài theo phần chữ tr.3. | Đã áp dụng tạm ở M2 theo yêu cầu sinh viên; chưa có xác nhận giảng viên |
| DEC-02 | PDF không nêu kiểu/độ dài/nullable. Entity áp dụng giới hạn ở design; DTO/React sau này phải đồng bộ. | Lựa chọn triển khai M2, không phải thông số bắt buộc của đề |
| DEC-03 | Giữ script tạo database rỗng; JPA/Hibernate update tạo bảng/FK, không reset dữ liệu. | Đã áp dụng ở M2 theo yêu cầu sinh viên; cách hiểu chưa được giảng viên xác nhận |
| DEC-04 | Admin và Staff dùng chung API đăng nhập; admin demo lấy từ cấu hình. | Dự kiến |
| DEC-05 | Category chưa có bài nhưng có con: cho xóa và đặt parent của con thành null trong transaction. | Dự kiến |
| DEC-06 | Account chưa tạo bài nhưng từng sửa bài: đặt UpdatedByID thành null rồi xóa account, giữ bài. | Dự kiến |
| DEC-07 | Search account theo tên/email; category theo tên/mô tả; news theo title/headline/content. Không phân biệt hoa thường; query trống trả toàn bộ danh sách được phép xem. | Dự kiến |
| DEC-08 | History là bài còn tồn tại do mình tạo, gồm active/inactive; không phải lịch sử phiên bản. | Dự kiến |
| DEC-09 | Chọn Spring Security + JWT và BCrypt; PDF không bắt buộc JWT. | Chưa triển khai auth |
| DEC-10 | Public lọc NewsStatus = 1; category inactive không tự làm ẩn bài active. | Dự kiến |

## 4. Phạm vi chức năng

### 4.1 Requirement Traceability Matrix

| ID | Yêu cầu | Tiêu chí nghiệm thu tối thiểu |
|---|---|---|
| REQ-01 | Tên project BE, React và DB đúng mẫu đề (tr.5) | `A2PhanTuanThanh_SE1910`; package npm dùng tên chữ thường hợp lệ |
| REQ-02 | Boot 3, REST API, ba lớp với Repository (tr.1,2,5) | Controller → Service → Repository; controller không truy vấn DB trực tiếp |
| REQ-03 | SQL Server và Spring Data JPA (tr.1,2,4) | JPA tạo bảng/quan hệ; lưu và đọc dữ liệu trên SQL Server |
| REQ-04 | ReactJS gọi API bằng axios (tr.1,2,4) | UI và request/response chạy với backend thật |
| REQ-05 | Mô hình dữ liệu theo sơ đồ (tr.3) | Đủ SystemAccount, Category, NewsArticle, Tag, NewsTag và các trường |
| REQ-06 | Xem tin không cần đăng nhập (tr.4) | Public list/detail chỉ trả tin active, kể cả truy cập URL trực tiếp |
| REQ-07 | Đăng nhập email/password, Admin = 1, Staff = 2 (tr.3,4) | Đúng/sai credential được xử lý; backend kiểm tra role |
| REQ-08 | Admin account CRUD + Search (tr.4) | Đủ thao tác ở API và React; Staff/khách không có quyền |
| REQ-09 | Chặn xóa account đã tạo bài (tr.4) | Bài active và inactive đều chặn xóa |
| REQ-10 | Staff category CRUD + Search (tr.4) | Đủ thao tác, description, parent và status |
| REQ-11 | Chặn xóa category đang được bài sử dụng (tr.4) | Không xóa category hoặc cascade xóa bài tham chiếu |
| REQ-12 | Staff news/tags CRUD + Search (tr.4) | Một category/bài; bài và links tag được lưu nhất quán |
| REQ-13 | Staff quản lý hồ sơ của mình (tr.4) | Không sửa người khác hoặc tự nâng role |
| REQ-14 | Staff xem history của mình (tr.4) | Lọc CreatedByID từ account đăng nhập; gồm active/inactive |
| REQ-15 | Create/Update bằng popup (tr.4) | Cả 6 luồng của account/category/news đều dùng dialog |
| REQ-16 | Delete có confirmation (tr.4) | Cancel không xóa; Confirm mới gửi request |
| REQ-17 | Validate mọi trường (tr.2) | BE kiểm tra kiểu/giá trị/FK; FE hỗ trợ nhập đúng, báo lỗi rõ |
| REQ-18 | Kiểm thử FE/BE (tr.2) | Có kết quả thao tác React, API và dữ liệu SQL Server thật |
| REQ-19 | Spring Initializr và IntelliJ IDEA (tr.2) | Sinh viên mở/chạy project trong IntelliJ và lưu minh chứng |

### 4.2 Tiến trình cá nhân và tài liệu

| ID | Yêu cầu | Tiêu chí nghiệm thu |
|---|---|---|
| GIT-01 | Repository cá nhân, đúng account | Remote/author đúng; kiểm tra commit gắn với profile GitHub |
| GIT-02 | C2 - tiến trình cá nhân và Git, 2.0 điểm | Có phát triển thật từ baseline → chức năng → sửa lỗi/cải tiến; không đặt chỉ tiêu số commit |
| DOC-01 | Bộ docs theo ảnh | Đủ 6 file và evidence; nội dung khớp code, test và commit |

## 5. Màn hình và quyền truy cập

| Khu vực | Khách | Admin (1) | Staff (2) |
|---|---|---|---|
| Tin active công khai | Xem | Xem | Xem |
| Account | Không | CRUD + Search | Không |
| Category | Không | Không | CRUD + Search |
| News/tags | Không | Không | CRUD + Search |
| Hồ sơ | Không | Ngoài phạm vi chức năng Admin | Xem/sửa của mình |
| History | Không | Ngoài phạm vi chức năng Admin | Bài do mình tạo |

Admin không tự động kế thừa quyền Staff. Module news cho Staff quản lý chung; bộ lọc bài của mình chỉ áp dụng cho history.

## 6. Validation và chất lượng

- Form có thông báo lỗi theo field; popup không đóng khi lưu thất bại.
- Role/status/FK và ràng buộc xóa được BE kiểm tra, không chỉ ẩn nút trên FE.
- Build/test chạy được; response không lộ password/hash; cấu hình không chứa secret.
- Người khác chạy được theo README; sinh viên giải thích được luồng request và nghiệp vụ.
- Chi tiết field/giới hạn ở [design](design.md); expected/actual ở [test-matrix](test-matrix.md).

## 7. Ngoài phạm vi

Approval workflow, scheduling và analytics được nhắc trong phần giới thiệu nhưng không thuộc Main Functions. Logo AI, `Admin/Admin` và mock localStorage của Assignment 01 không phải yêu cầu Assignment 02. JWT là lựa chọn thiết kế, không phải yêu cầu bắt buộc của đề.

Kế hoạch chung ở [development-plan](development-plan.md); tiến độ thực tế xem README, không cập nhật lại yêu cầu sau mỗi milestone.
