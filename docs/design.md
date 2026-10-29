Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Solution Design

## 1. Mục tiêu thiết kế

Ứng dụng ưu tiên đúng core requirements, rõ trách nhiệm và dễ giải thích khi đánh giá. Backend ba lớp giữ nghiệp vụ và dữ liệu; React quản lý tương tác và gọi API bằng axios. Tránh generic CRUD engine hoặc global state khi chưa cần.

Entity/repository và persistence đã triển khai; API, auth và React bên dưới còn là thiết kế dự kiến. Tiến độ thực tế và cách chạy xem [README](../README.md).

### 1.1 Quy ước code dễ giải thích

- Tên thể hiện nghiệp vụ; method làm một việc rõ ràng, các bước kiểm tra → xử lý → trả kết quả đọc được từ trên xuống.
- Ưu tiên `if/else`, vòng lặp, biến trung gian và mapping DTO tường minh khi dễ hiểu hơn cách viết gộp. Stream/lambda/helper chỉ dùng khi giúp code rõ hơn; không thêm abstraction/thư viện chỉ để rút ngắn code.
- Giữ đầy đủ ba lớp, validation, quyền, transaction và xử lý lỗi. Cách làm phức tạp chỉ chọn khi có lý do cụ thể và giải thích được.

Quy ước chi tiết áp dụng cho các chat tiếp theo nằm trong [AGENTS.md](../AGENTS.md).

## 2. Định hướng giao diện

- Public news list/detail và Login tách khỏi khu vực quản lý.
- Header có tên hệ thống, current account và Logout; menu theo role.
- Account, Category, News dùng table, toolbar Search/Add và status/role có nhãn chữ.
- Create/Update dùng popup của từng module; Delete có confirmation riêng.
- Form có loading/error; Cancel không lưu; lỗi API giữ form để sửa.
- Màn hình hẹp cho table cuộn trong vùng riêng; input có label, button có type và focus rõ.

```text
Header: FUNewsManagementSystem | current account | Logout
Menu theo role                | Page title
                              | [Search........] [Add]
                              | Table / Empty / No-result
                              | Edit → Form dialog
                              | Delete → Confirm dialog
```

Màu sắc/layout có thể theo phong cách Assignment 01; Dashboard, Settings và logo AI không được thêm thành chức năng bắt buộc của A2.

## 3. Kiến trúc và trách nhiệm

```text
React Page/Form → axios API client → HTTP/JSON
  → Spring Security → REST Controller → Service → Repository/JPA → SQL Server
```

| Thành phần | Trách nhiệm |
|---|---|
| React Page | Ghép UI, giữ keyword/selected item/dialog/loading; nhận kết quả API |
| Form dialog | Controlled inputs, local error, submit dữ liệu đã validate |
| axios client | URL, HTTP method, bearer token và xử lý lỗi response |
| Controller | Nhận DTO, Jakarta Validation, trả đúng HTTP status |
| Service | Nghiệp vụ, principal/quyền theo dữ liệu, mapping DTO và transaction |
| Repository / Entity | Truy vấn và mapping bảng/quan hệ bằng JPA |
| Exception handler | Response lỗi thống nhất, không lộ stacktrace hoặc secret |

Dùng constructor injection. Controller không gọi repository trực tiếp; không trả entity/password hash ra API. Đặt package dưới root `fpt.edu.a2phantuanthanh_se1910` để Spring quét component/entity.

## 4. Cấu trúc code dự kiến

```text
src/main/java/fpt/edu/a2phantuanthanh_se1910/
  A2PhanTuanThanhSe1910Application.java
  controller/
  service/impl/
  repository/
  entity/
  dto/request/
  dto/response/
  mapper/
  security/
  config/
  exception/
  bootstrap/
frontend/A2PhanTuanThanh_SE1910/src/
  api/
  auth/
  pages/
  components/
  routes/
```

Frontend là project riêng trong cùng repo; package npm dự kiến `a2phantuanthanh-se1910`. Security/JWT sẽ thêm ở M3. M2 mới tạo `entity/`, `repository/` và test persistence; các package nghiệp vụ khác chưa có.

Component tree mức khái niệm:

```text
BrowserRouter → AuthProvider → App/Routes
  PublicNewsPage / PublicNewsDetailPage / LoginPage
  ProtectedRoute (kiểm tra role)
    ManagementLayout → Header / Menu / Outlet
      AccountsPage (Admin)
      CategoriesPage / NewsPage / ProfilePage / HistoryPage (Staff)
```

## 5. Data model và persistence

Đã đối chiếu trực tiếp hình Database Design và phần chữ trang 3 PDF Assignment 02. Giữ đủ tên bảng/cột theo PDF, schema `dbo`. Kiểu/giới hạn dưới đây đã dùng trong entity theo DEC-02, không phải thông số đề đã quy định.

| Entity | Các trường |
|---|---|
| SystemAccount | AccountID, AccountName, AccountEmail, AccountRole, AccountPassword |
| Category | CategoryID, CategoryName, CategoryDescription, ParentCategoryID, IsActive |
| NewsArticle | NewsArticleID, NewsTitle, Headline, CreatedDate, NewsContent, NewsSource, CategoryID, NewsStatus, CreatedByID, UpdatedByID, ModifiedDate |
| Tag | TagID, TagName, Note |
| NewsTag | NewsArticleID, TagID |

| Dữ liệu | Kiểu/giới hạn đã triển khai |
|---|---|
| ID | Long identity; NewsTag dùng khóa ghép hai FK |
| Tên account/category/tag | String, tối đa 100 ký tự |
| Email / password | Unicode tối đa 254 / 255, email unique bằng UQ_SystemAccount_AccountEmail; @Email/@NotBlank. Chuẩn hóa email và BCrypt do M3/M4 xử lý, M2 chưa hash/login |
| Role / status | Integer; role 1/2, NewsStatus và IsActive 0/1 |
| Title / headline / content | String tối đa 200 / 300 / 20.000; content dùng Unicode large text |
| Description / source / note | Tối đa 1000 / 500 / 500; có thể trống |
| CreatedDate / ModifiedDate | LocalDateTime → datetime2(6); caller cung cấp giá trị UTC, ModifiedDate nullable. Chưa có service tự gán audit |

Quan hệ và cách lưu:

- Một news thuộc một category và creator; updater có thể null. Category có parent nullable; entity chặn tự làm parent, vòng nhiều category sẽ do Service kiểm tra.
- NewsTag dùng hai ID làm khóa ghép; `@MapsId` gắn hai ID này với quan hệ news/tag. Unique TagID giữ mỗi tag thuộc tối đa một bài theo DEC-01 đang áp dụng tạm.
- Quan hệ lazy, không cascade xóa. FK, NOT NULL, unique email/tag và CHECK role/status giữ dữ liệu hợp lệ; Bean Validation kiểm tra field khi lưu bằng JPA.
- Text dùng Unicode; NewsContent là nvarchar(max), bắt buộc nhập, giới hạn 20.000 ký tự ở entity. Ngày là datetime2(6), caller cung cấp UTC; Service gán audit ở mốc nghiệp vụ sau.
- `create-database.sql` tạo DB rỗng; JPA `update` tạo bảng/FK thiếu, không tự seed/reset. Test dùng DB riêng và kiểm tra lại sau restart. Cách hiểu tạo DB/bảng theo DEC-03 vẫn chưa được giảng viên xác nhận.

Repository cung cấp CRUD cơ bản và query theo email, creator, status, parent, tag hoặc kiểm tra tham chiếu. Quyền người dùng, xử lý xóa và transaction nghiệp vụ sẽ nằm ở Service, không đặt trong Repository. File cần mở xem [code-flow](code-flow.md).

## 6. State ownership và API

| State | Owner | Lý do |
|---|---|---|
| Token/current account | AuthProvider | Header, route và request được bảo vệ |
| Danh sách/kết quả API, keyword, selected item, dialog, loading | Page tương ứng | Phục vụ một module, chưa cần Context dữ liệu toàn ứng dụng |
| Form input/error | Form dialog | Gắn với vòng đời form, là bản copy khi Edit |

Search gửi `q` tới backend; clear keyword tải danh sách đầy đủ trong phạm vi có quyền. Không mutate props/state trực tiếp; không lấy bộ lọc trên FE thay cho kiểm tra quyền ở BE.

| Route dự kiến | Thao tác | Quyền |
|---|---|---|
| `/api/auth/login` | POST login email/password | Public |
| `/api/public/news`, `/api/public/news/{id}` | GET list/search (`?q=`) và detail, NewsStatus = 1 | Public |
| `/api/accounts`, `/api/accounts/{id}` | GET list/search/detail; POST tạo; PUT sửa; DELETE xóa | Admin |
| `/api/categories`, `/api/categories/{id}` | GET list/search/detail; POST tạo; PUT sửa; DELETE xóa | Staff |
| `/api/news`, `/api/news/{id}` | GET list/search/detail; POST/PUT lưu bài kèm tags; DELETE xóa | Staff |
| `/api/tags?q=` | GET tag tự do hoặc thuộc bài đang sửa | Staff |
| `/api/profile` | GET/PUT hồ sơ account đang đăng nhập | Staff |
| `/api/profile/news?q=` | GET history theo principal | Staff |

POST tạo trên collection; PUT/DELETE trên route có ID. HTTP: đọc/sửa 200, tạo 201 kèm Location, xóa 204; validation 400, auth 401, sai quyền 403, không thấy 404, xung đột 409. Lỗi trả `status`, `code`, `message`, `fieldErrors`, `path`, `timestamp`.

## 7. Authentication và quyền

- Dự kiến Spring Security + JWT bearer, BCrypt; khóa ký qua môi trường, token có hạn.
- BE kiểm tra account/role hiện tại để đổi role/xóa account làm mất quyền cũ.
- Axios gửi token; logout hoặc 401 xóa phiên FE; chỉ cho CORS origin được cấu hình.
- Admin quản lý account; Staff quản lý category/news, hồ sơ và history của mình. Admin không mặc định có quyền Staff.
- History query CreatedByID từ principal, gồm active/inactive. Profile chỉ sửa tên/email/password, không nhận ID người khác hoặc role.
- Public lọc NewsStatus ở BE; detail inactive/không tồn tại trả 404. Không trả password/hash/stacktrace.

## 8. CRUD dialog và delete policy

```text
Add/Edit → popup rỗng/prefill → validate → axios POST/PUT
         → BE kiểm tra và lưu → response → close/reset + reload list
Delete → ConfirmDialog → Cancel: close; Confirm: axios DELETE → reload list
```

| Thao tác | Policy |
|---|---|
| Lưu bài/tags | Một transaction; kiểm tra category/tag, ID không lặp; tag lỗi rollback cả bài |
| Quản lý tag trong news dialog | Thêm tag mới, sửa name/note, gắn tag tự do và bỏ gắn; tag bài khác bị chặn theo DEC-01 |
| Xóa account đã tạo bài | Chặn cho cả bài active/inactive; không cascade xóa bài |
| Xóa category đang có bài | Chặn cho cả hai status; không cascade xóa bài |
| Category có con / account chỉ có updater reference | Theo DEC-05/06 trong requirements |
| Xóa news | Xóa bài và links NewsTag, giữ account/category/Tag; tag có thể thuộc 0 bài |

FK/unique bảo vệ dữ liệu khi request đồng thời; chuyển constraint conflict thành 409. UI hiển thị lý do, giữ form khi lỗi, chặn submit kép. Quan hệ được BE kiểm tra lại dù FE đã validate.

## 9. Validation strategy

- ID là số nguyên dương, FK tồn tại; role/status đúng miền. Chặn JSON sai kiểu hoặc số thập phân cho integer.
- Tên/email/title/headline/content không blank; text được trim phù hợp và giới hạn theo bảng dữ liệu. Email đúng format và unique.
- Tạo/đổi password dự kiến tối thiểu 8 ký tự, tối đa 72 byte UTF-8; không trim. Sửa không gửi password thì giữ hash cũ; login không áp chính sách tối thiểu mới lên account có sẵn.
- Query search trim, tối đa 200 ký tự; dùng parameter binding. NewsSource không bắt buộc URL vì đề không quy định.
- Parent không tự tham chiếu/chu kỳ; tags có thể rỗng theo cách hiểu hiện tại, ID không trùng và field tag mới được validate.
- Audit fields chỉ do server quản lý, không có trong DTO ghi. Thông báo theo field, form không đóng nếu chưa hợp lệ.

## 10. Definition of Done

Một tính năng xong khi đúng yêu cầu, đã kiểm tra normal/negative case và sinh viên giải thích được luồng xử lý. Với UI, chuẩn bị màn hình/dữ liệu chạy được để sinh viên tự chụp theo evidence index. Chỉ cập nhật design khi thiết kế đổi; kế hoạch chung ở [development-plan](development-plan.md).

Tham khảo: [Spring Boot code structure](https://docs.spring.io/spring-boot/3.5/reference/using/structuring-your-code.html), [JPA initialization](https://docs.spring.io/spring-boot/3.5/how-to/data-initialization.html).
