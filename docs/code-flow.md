Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Code Flow Guide

Tài liệu dùng để mở đúng file và giải thích theo khung: **Purpose → Input → Trigger → Transform → Output**. Mục 2 mô tả luồng đã có ở M1; các luồng nghiệp vụ còn lại là dự kiến.

## 1. Ranh giới trách nhiệm

```text
React Page/Form → axios → Spring Security
  → REST Controller → Service → Repository/JPA → SQL Server
  → response DTO → React cập nhật UI
```

- Page giữ keyword, selected item, trạng thái dialog/loading và kết quả API.
- Form giữ input/error, gọi validator; axios thực hiện HTTP request.
- Controller nhận DTO và xử lý HTTP; Service giữ nghiệp vụ/transaction.
- Repository truy vấn DB; SQL Server là nguồn dữ liệu chính.

Các lớp nghiệp vụ/React này chưa tồn tại. Hiện chỉ có application, datasource/JPA và test kết nối.

## 2. Startup và database flow đã có

```text
Application.main()
  → SpringApplication.run()
  → đọc application.properties và biến môi trường
  → khởi tạo Tomcat, DataSource và JPA
  → JDBC kết nối SQL Server
```

| Thành phần giải thích | Nội dung thực tế |
|---|---|
| Purpose | Khởi động backend và xác nhận SQL Server sẵn sàng |
| Input | Biến SQLSERVER_*; database A2PhanTuanThanh_SE1910; native DLL nếu dùng Windows auth |
| Trigger | Chạy main, `java -jar` hoặc `mvnw clean verify` |
| Transform | Boot nạp cấu hình, mở JDBC connection; test kiểm tra JPA factory và `SELECT DB_NAME(), 1` |
| Output | Hai tests pass; JAR/Tomcat chạy. GET `/` trả 404 vì chưa có controller |

`create-database.sql` tạo DB rỗng nếu thiếu, chạy trước app/test. `ddl-auto=none`; M1 có 0 bảng nghiệp vụ. Code thuộc commit `db2e8e6`; [evidence M1](evidence/2026-10-28-m1/inspection.md).

## 3. Login flow dự kiến

```text
Login form submit → validate email/password → axios POST /api/auth/login
  → Auth service kiểm tra account/BCrypt
  → trả token và role hoặc lỗi
  → React cập nhật auth state và route
```

Protected request được BE kiểm tra token/role. Logout xóa phiên FE; 401 đưa người dùng về trạng thái chưa đăng nhập. Không dùng credential mock `Admin/Admin` của bài trước.

## 4. Create và Update flow dự kiến

```text
Add/Edit → popup rỗng hoặc prefill bản copy item
  → submit → FE validate → axios POST/PUT
  → Controller validate DTO → Service kiểm tra rule/FK
  → Repository lưu trong transaction
  → response → đóng popup và tải lại danh sách
```

News lưu bài cùng links tag trong một transaction; lỗi tag/FK phải rollback toàn bộ. Creator/updater/date do server gán. Update giữ creator/ngày tạo; FE không được giả mạo các field này.

## 5. Delete flow dự kiến

```text
Delete(item) → ConfirmDialog
  → Cancel: đóng dialog, không gửi DELETE
  → Confirm: axios DELETE /{id}
  → BE kiểm tra role, record và quan hệ
  → bị chặn: 409 + lý do; hợp lệ: xóa, trả 204
  → React cập nhật danh sách
```

- Account đã tạo bài hoặc category có bài tham chiếu không được xóa, kể cả bài inactive.
- Category có con/account chỉ có updater reference xử lý theo DEC-05/06 trong [requirements](requirements.md).
- Xóa news chỉ xóa bài và NewsTag links; giữ account/category/Tag. FK vẫn bảo vệ dữ liệu khi request đồng thời.

## 6. Search, public và history flow dự kiến

```text
Search input → keyword riêng → axios GET ?q=...
  → Service/Repository tìm trong phạm vi có quyền
  → trả danh sách → React render kết quả hoặc no-result
```

Query trống trả toàn bộ danh sách được phép xem. Public list/detail lọc NewsStatus = 1 ở BE; detail inactive trả 404. History lấy CreatedByID từ principal và gồm cả hai status; quản lý news chung không bị bộ lọc history giới hạn.

## 7. Profile và persistence flow dự kiến

Profile lấy account từ principal, chỉ sửa tên/email/password; không nhận ID người khác hoặc role để sửa. Sau lưu/reload, React lấy lại dữ liệu qua API từ SQL Server. Không dùng localStorage làm nguồn CRUD thứ hai.

## 8. File cần mở khi giải thích code

| Câu hỏi | File hiện có / hướng cập nhật |
|---|---|
| Backend khởi động ở đâu? | [Application](../src/main/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910Application.java) |
| Cấu hình Boot/dependency? | [pom.xml](../pom.xml) |
| Kết nối SQL Server thế nào? | [application.properties](../src/main/resources/application.properties), [.env.example](../.env.example) |
| Database rỗng được tạo ra sao? | [create-database.sql](../scripts/create-database.sql) |
| Test đang kiểm tra gì? | [ApplicationTests](../src/test/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910ApplicationTests.java) |
| Auth, CRUD, tags, profile/history? | Chưa triển khai; bổ sung file/method thật ở M2–M7 |

Khi code từng module, cập nhật luồng và file thực tế kèm mã test trong [test-matrix](test-matrix.md).
