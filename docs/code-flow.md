# Luồng code và truy vết

Trạng thái: baseline 28/10/2026. Phần 1 mô tả code có thật; các phần còn lại là thiết kế chờ triển khai.

## 1. Luồng hiện có

`A2PhanTuanThanhSe1910Application.main()` gọi `SpringApplication.run(...)` để tạo Spring application context. `application.properties` mới có tên ứng dụng. Chưa có controller, service, repository, entity hoặc datasource.

Code hiện tại:

- [Main application](../src/main/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910Application.java).
- [Cấu hình ứng dụng](../src/main/resources/application.properties).
- [POM](../pom.xml): Boot 4.1.2, Java 21; starter cơ bản và test.
- [Context test](../src/test/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910ApplicationTests.java): `@SpringBootTest` và `contextLoads()` rỗng; xác nhận context khởi tạo không lỗi, không xác nhận nghiệp vụ.

## 2. Luồng request dự kiến

1. React page/dialog thu thập dữ liệu và validate trước khi gửi.
2. Hàm trong `api/` gọi axios với URL, method và payload; bearer token được thêm cho request được bảo vệ.
3. Spring Security xác thực và kiểm tra role; từ chối trước khi vào nghiệp vụ khi thiếu quyền.
4. Controller bind DTO và chạy `@Valid`; không nhận entity trực tiếp.
5. Service kiểm tra nghiệp vụ, lấy account từ principal và bắt đầu transaction cho thao tác ghi.
6. Repository đọc/ghi bằng JPA tới SQL Server; FK/unique ngăn dữ liệu không hợp lệ.
7. Service map response DTO; controller trả HTTP status đúng hợp đồng.
8. FE cập nhật UI hoặc hiển thị lỗi/fieldErrors. `RestControllerAdvice` xử lý lỗi ứng dụng nhất quán.

## 3. Xóa category và account

```mermaid
sequenceDiagram
    actor Staff
    participant UI as Category dialog
    participant API as CategoryController
    participant SVC as CategoryService
    participant DB as Repository và SQL Server
    Staff->>UI: Chọn Delete
    UI->>Staff: Yêu cầu xác nhận
    Staff->>UI: Confirm
    UI->>API: DELETE /api/categories/{id}
    API->>SVC: delete(id)
    SVC->>DB: Tìm category và kiểm tra bài tham chiếu
    alt Có bất kỳ bài tham chiếu
        SVC-->>API: Conflict
        API-->>UI: 409 + lý do
    else Không có bài tham chiếu
        SVC->>DB: Gỡ parent của con nếu cần; xóa category trong transaction
        API-->>UI: 204
        UI->>API: Tải lại danh sách
    end
```

Đây là luồng dự kiến theo DEC-05. Cancel không tạo request DELETE. Query kiểm tra tham chiếu không lọc NewsStatus. Nếu có race giữa kiểm tra và xóa, FK vẫn bảo vệ dữ liệu và lỗi constraint chuyển thành 409.

Account delete tương tự nhưng yêu cầu Admin, kiểm tra `CreatedByID` và xử lý `UpdatedByID` theo DEC-06. Không dùng cascade để vượt qua điều kiện cấm xóa.

## 4. Tạo/sửa bài và tags

`NewsDialog → newsApi → NewsController → NewsArticleServiceImpl → repositories`.

- DTO có title/headline/content/source/category/status/tags; không có creator/updater/date do người dùng đặt.
- Service validate category và tag, kiểm tra cardinality DEC-01, rồi lưu NewsArticle và NewsTag trong cùng transaction.
- Tạo mới: lấy CreatedByID từ principal Staff, gán CreatedDate; UpdatedByID/ModifiedDate ban đầu null.
- Cập nhật: giữ nguyên CreatedByID/CreatedDate, gán updater/time; đồng bộ thêm/bỏ links. Nếu một tag không hợp lệ thì rollback toàn bộ.
- Xóa bài: xóa NewsTag của bài và bài; không xóa account, category hoặc các Tag không cần xóa.
- Sau thành công, dialog đóng, danh sách được tải lại; public chỉ thấy bài nếu NewsStatus=1.

## 5. Public, profile và history

- Public list/detail lọc NewsStatus trong query/service, không tải hết rồi để React lọc. URL trực tiếp của bài inactive trả 404.
- Profile dùng principal; không cho client truyền account ID khác để cập nhật. DTO cập nhật hồ sơ không chứa role.
- History gọi `/api/profile/news`, service tự lấy ID và query CreatedByID. Một Staff khác dù đoán ID bài vẫn không làm history trả bài không do mình tạo.
- Quản lý news của Staff và history cá nhân là hai phạm vi khác nhau; không dùng bộ lọc history để vô tình giới hạn module quản lý chung.

## 6. Bảng truy vết dự kiến

Tên lớp/file ngoài phần 1 dưới đây **chưa tồn tại**; sẽ thay bằng đường dẫn và method chính xác khi code được tạo. Cột SHA chỉ điền khi có commit thật.

| Yêu cầu | Điểm code dự kiến | Test | Mốc | Implementation/SHA |
|---|---|---|---|---|
| REQ-01,02,19 | pom.xml, main package, tên React/DB, IntelliJ | TC-SETUP-01..03 | M1 | Chưa thực hiện / chưa có |
| REQ-03,05 | entity/, repository/, datasource/bootstrap | TC-DB-01..03 | M2 | Chưa thực hiện / chưa có |
| REQ-04 | React api client và các page | TC-E2E-01 | M4-M7 | Chưa thực hiện / chưa có |
| REQ-06 | PublicNewsController/Service, PublicNewsPage | TC-PUB-01..03 | M3,M6 | Chưa thực hiện / chưa có |
| REQ-07 | AuthController/Service, SecurityConfig, JWT | TC-AUTH-01..05 | M3 | Chưa thực hiện / chưa có |
| REQ-08,09 | AccountController/Service, AccountRepository, AccountDialog | TC-ACC-01..05 | M4 | Chưa thực hiện / chưa có |
| REQ-10,11 | CategoryController/Service, CategoryRepository, CategoryDialog | TC-CAT-01..05 | M5 | Chưa thực hiện / chưa có |
| REQ-12 | NewsController, NewsArticleService, Tag/NewsTag, NewsDialog | TC-NEWS-01..05 | M6 | Chưa thực hiện / chưa có |
| REQ-13,14 | ProfileController/Service, ProfilePage, HistoryPage | TC-SELF-01..03 | M7 | Chưa thực hiện / chưa có |
| REQ-15,16 | Các dialog create/update và ConfirmDialog | TC-UI-01..03 | M4-M6 | Chưa thực hiện / chưa có |
| REQ-17 | DTO constraints, service rules, GlobalExceptionHandler | TC-VAL-01..03 và negative tests module | M2-M7 | Chưa thực hiện / chưa có |
| REQ-18 | Full FE/BE trên SQL Server | TC-E2E-01..02 | M8 | Chưa thực hiện / chưa có |
| GIT-01,02,DOC-01 | Git remote/log và docs/evidence | TC-PROC-01..02 | M0-M8 | Chưa hoàn tất / chưa có |

Khi đổi route, signature, validation hoặc quyền, cập nhật mục luồng liên quan và test tương ứng trong cùng commit. Không giữ mô tả “service kiểm tra…” nếu code thực tế chưa có kiểm tra đó.
