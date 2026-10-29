Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Code Flow Guide

## 1. Cách giải thích bài làm

Với mỗi chức năng, trình bày: **làm gì → nhận dữ liệu gì → ai gọi → xử lý ra sao → trả kết quả gì**. Mở các file ở cuối tài liệu để minh họa.

Đã có startup, persistence và M3 login/JWT, quyền backend, public news. CRUD quản trị, profile/history và React chưa triển khai.

## 2. Khởi động backend

```text
Application.main()
  → Spring Boot đọc cấu hình/biến môi trường
  → kết nối SQL Server
  → JPA đọc entity và tạo bảng/quan hệ còn thiếu
  → Tomcat sẵn sàng nhận request
```

Database rỗng được tạo bằng `scripts/create-database.sql`. Entity mô tả bảng; repository đọc/ghi dữ liệu. M3 có API public tại `/api/public/news`; GET `/` hiện trả 401 theo security mặc định.

## 3. Lưu và đọc dữ liệu đã có

```text
Dữ liệu test → repository.save()/saveAndFlush()
  → JPA validate entity và thực hiện SQL
  → SQL Server kiểm tra khóa/ràng buộc
  → repository đọc lại entity và quan hệ
```

- Mỗi news có một category và creator; updater có thể trống.
- Category có thể có category cha.
- NewsTag nối news với tag bằng hai ID; theo lựa chọn hiện tại mỗi tag thuộc tối đa một bài.
- Không tự xóa bài khi xóa account/category. Các query kiểm tra tham chiếu đã có; quy trình xóa và thông báo nghiệp vụ sẽ nằm trong Service sau này.
- Repository hỗ trợ tìm theo email, creator, status, parent và tag. History theo creator có cả active/inactive.

## 4. Luồng UI/API dự kiến

```text
Người dùng thao tác trên React → axios gửi request
  → BE kiểm tra đăng nhập/quyền
  → Controller nhận DTO → Service xử lý nghiệp vụ
  → Repository lưu/đọc SQL Server
  → trả response → React hiển thị kết quả
```

Create/Update mở popup; lỗi giữ form để sửa. Delete mở confirmation: Cancel không gửi request, Confirm mới xóa. Public chỉ trả news active; profile/history dùng tài khoản đăng nhập, không lấy ID tùy ý từ client.

Chỉ bổ sung tên method/route thật khi triển khai chức năng đó; không viết lại toàn bộ luồng sau mỗi milestone.

## 5. Kiểm tra dữ liệu

Fixture test lưu account/category/news/tag bằng repository vào DB test riêng. Kiểm tra đọc tiếng Việt, quan hệ, constraint, rollback khi lỗi và dữ liệu còn sau restart. Kết quả M2 đã lưu: 9 tests pass; không phải kết quả test mới của lần sửa docs này.

Để chạy lại, làm theo [README](../README.md). Fixture M2 không dùng để login; demo M3 chỉ seed khi bật cấu hình.

## 6. File cần mở khi giảng viên hỏi

| Câu hỏi | File |
|---|---|
| Chương trình bắt đầu ở đâu? | [Application](../src/main/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910Application.java) |
| Kết nối DB và JPA cấu hình ở đâu? | [application.properties](../src/main/resources/application.properties) |
| Quan hệ bài viết với category/account? | [NewsArticle](../src/main/java/fpt/edu/a2phantuanthanh_se1910/entity/NewsArticle.java) |
| Vì sao bảng NewsTag có hai ID? | [NewsTag](../src/main/java/fpt/edu/a2phantuanthanh_se1910/entity/NewsTag.java), [NewsTagId](../src/main/java/fpt/edu/a2phantuanthanh_se1910/entity/NewsTagId.java) |
| Kiểm tra account/category có bài như thế nào? | [NewsArticleRepository](../src/main/java/fpt/edu/a2phantuanthanh_se1910/repository/NewsArticleRepository.java) |
| Dữ liệu test được tạo ở đâu? | [PersistenceFixture](../src/test/java/fpt/edu/a2phantuanthanh_se1910/persistence/PersistenceFixture.java) |
| Các test đang xác nhận gì? | [NewsPersistenceTests](../src/test/java/fpt/edu/a2phantuanthanh_se1910/persistence/NewsPersistenceTests.java), [test-matrix](test-matrix.md) |


## M3 đã triển khai: login → bearer → public news

- `AuthController.login(LoginRequest)` nhận email/password, Jakarta Validation kiểm tra payload. `AuthService.login()` chuẩn hóa email, repository tra account, BCrypt kiểm tra password không trim; sai credential trả cùng lỗi 401. Nimbus ký JWT có subject=AccountID, issuer, issuedAt, expiresAt; response DTO không chứa password/hash.
- `JwtConfiguration` đọc khóa Base64 từ môi trường và cấu hình HS256, issuer, thời hạn. Spring Security xác minh bearer; `CurrentAccountJwtConverter` gọi `AuthService.findCurrentAccount()` đọc account/role hiện tại. Account đã xóa trả 401; đổi role áp quyền mới. `SecurityConfiguration` tách ADMIN và STAFF, không kế thừa role; `SecurityErrorHandler` trả JSON 401/403.
- `PublicNewsController.search(q)` → `PublicNewsService.search()` trim/giới hạn 200 ký tự, escape wildcard → `NewsArticleRepository.searchActiveNews()` bind tham số và lọc status=1 → mapping summary DTO. Dùng native SQL vì Hibernate coi nội dung `nvarchar(max)` là NCLOB, không chấp nhận `lower()` trong JPQL.
- `detail(id)` → service tìm ID+status=1 trong transaction đọc → map content, category, authorName và tags. Bài inactive/không tồn tại cùng trả 404; category inactive vẫn cho đọc bài active. Không serialize entity/account ra response.
- `DemoDataInitializer` chỉ chạy khi bật `DEMO_ENABLED`; transaction thêm account BCrypt và hai bài demo còn thiếu, giữ dữ liệu cũ. Password demo lấy từ môi trường. Mặc định không seed.

Mở lần lượt `controller/AuthController.java`, `service/AuthService.java`, `security/SecurityConfiguration.java`, `security/CurrentAccountJwtConverter.java`, `controller/PublicNewsController.java`, `service/PublicNewsService.java`, `repository/NewsArticleRepository.java` dưới `src/main/java/fpt/edu/a2phantuanthanh_se1910/`. Kiểm tra thật ở `src/test/java/fpt/edu/a2phantuanthanh_se1910/api/M3ApiTests.java`; chạy bằng `scripts/test-m3.ps1`. Chưa có CRUD quản trị, profile/history hoặc React.

Tham khảo: [Spring Security JWT](https://docs.spring.io/spring-security/reference/6.5/servlet/oauth2/resource-server/jwt.html), [BCrypt/PasswordEncoder](https://docs.spring.io/spring-security/reference/6.5/features/authentication/password-storage.html).
