Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Code Flow Guide

Tài liệu dùng để mở đúng file và giải thích theo khung: **Purpose → Input → Trigger → Transform → Output**. Mục 2/2.1–2.3 mô tả startup và persistence M1/M2 đã có; các luồng API/nghiệp vụ còn lại là dự kiến.

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

Các lớp Controller/Service/nghiệp vụ/React chưa tồn tại. Hiện có application, datasource/JPA, entity/repository và test trên SQL Server.

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

`create-database.sql` tạo DB rỗng nếu thiếu, chạy trước app/test. Lịch sử M1: `ddl-auto=none`, 0 bảng nghiệp vụ, commit `db2e8e6`; [evidence M1](evidence/2026-10-28-m1/inspection.md). M2 chuyển sang `update`, quét 5 entity và 5 repository; không tự tạo fixture trên DB bài tập. Bảng Output “hai tests” ở trên là kết quả M1; lần chạy M2 gồm thêm 7 persistence tests.

### 2.1 Mapping entity → SQL Server (M2)

| Bước | File/method và cách giải thích |
|---|---|
| Purpose | `entity/SystemAccount`, `Category`, `NewsArticle`, `Tag`, `NewsTag` giữ đúng 5 bảng/26 cột PDF và toàn vẹn tham chiếu |
| Input | Entity có field đã validate; NewsArticle nhận category/creator bắt buộc, updater nullable; Category nhận parent nullable |
| Trigger | Startup đọc annotation; repository `saveAndFlush()` hoặc transaction commit kích hoạt persist/update |
| Transform | `@Table/@Column/@JoinColumn` giữ tên SQL; `@Nationalized` sinh nvarchar; JPA kiểm tra @NotBlank/@Size/@NotNull/@Email/@Min/@Max, SQL kiểm tra FK/NOT NULL/unique/CHECK |
| Output | 5 PK (NewsTag là composite), 6 FK NO_ACTION, unique email và TagID, CHECK role/status/self-parent. Lỗi phải rollback transaction chứa các thay đổi liên quan |

Mở `NewsTagId.equals()/hashCode()` để giải thích định danh theo hai ID. `NewsTag(NewsArticle, Tag)` yêu cầu cả hai đã được lưu, tạo embedded key; `@MapsId` dùng cùng cột cho key và association. Không thêm ID phụ hoặc bảng nối ngầm. Quan hệ lazy một chiều, không có cascade: repository không tự xóa dữ liệu liên quan.

### 2.2 Repository queries (M2)

| Purpose | Input → Trigger → Transform → Output |
|---|---|
| Kiểm tra account/category đang được news sử dụng | accountId/categoryId → `NewsArticleRepository.existsByCreatedBy_AccountId()` / `existsByCategory_CategoryId()` → Spring Data bind tham số vào query tồn tại → boolean, không lọc status nên tính cả bài inactive |
| History theo creator | accountId → `findByCreatedBy_AccountIdOrderByCreatedDateDescNewsArticleIdDesc()` → WHERE CreatedByID + ORDER BY ngày/ID DESC → danh sách bài của creator, cả hai status |
| Chuẩn bị DEC-06 | accountId → `existsByUpdatedBy_AccountId()` / `findByUpdatedBy_AccountId()` → lọc updater → boolean/list, chưa gỡ updater hoặc xóa account |
| Chuẩn bị DEC-05 | parentId → `CategoryRepository.findByParentCategory_CategoryId()` → lọc parent → các category con, chưa thay parent/xóa |
| Tra email | email → `SystemAccountRepository.findByAccountEmailIgnoreCase()` / `existsByAccountEmailIgnoreCase()` → query ignore-case → Optional/boolean; chưa login hoặc chuẩn hóa email |
| Lọc status | status hoặc ID+status → `findByNewsStatusOrderByCreatedDateDescNewsArticleIdDesc()` / `findByNewsArticleIdAndNewsStatus()` → SQL lọc → list/Optional; chưa phải API public |
| Tra tag links | newsId/tagId → `NewsTagRepository.findByNewsArticle_NewsArticleId()` / `existsByTag_TagId()` / `findByTag_TagId()` → lọc FK → list/boolean/Optional |

`TagRepository` và các repository kế thừa `JpaRepository` để save/read/delete/flush cơ bản; kiểm tra nghiệp vụ và transaction nhiều repository sẽ nằm ở Service sau này. Caller phải dùng transaction khi đọc thuộc tính của association lazy.

### 2.3 Fixture → constraint → restart/rollback (M2)

| Bước | File/method và cách giải thích |
|---|---|
| Purpose | `persistence/NewsPersistenceTests` chứng minh entity/repository hoạt động trên SQL Server thật; `scripts/test-persistence.ps1` chạy build và packaged JAR |
| Input | Cấu hình SQLSERVER_* đã xác minh; DB mới `A2PhanTuanThanh_SE1910_M2Test_<suffix>`; fixture tiếng Việt, password ngẫu nhiên dành riêng cho test |
| Trigger | PowerShell 7 chạy `scripts/test-persistence.ps1 -SqlServerInstance '.\SQL2022'` |
| Transform | Script tạo DB test mới, không DROP/reset; `startOnExplicitIsolatedDatabase()` chặn tên ngoài prefix. `jpaCreatesExactTablesColumnsKeysAndConstraints()` đối chiếu sys catalog. `PersistenceFixture.create()` lưu qua 5 repo trong TransactionTemplate |
| Output | Fixture có 3 account/category/news/tag và 2 links, tag tự do, article active/inactive với hai creator; logs/report và SQL snapshot thực tế |

`assertFixtureAndQueries()` đọc lại Unicode, nullable, audit, parent/creator/updater/tag và mọi derived query đã thêm. `committedDataSurvivesApplicationRestartAndSchemaValidation()` đóng toàn bộ context/EMF/pool, mở lại với validate rồi update và đọc lại từ DB. Script sau đó chạy JAR trên DB bài tập (HTTP 404, kiểm tra session), chạy hai JVM JAR riêng trên DB test; snapshot metadata/counts/audit/Unicode content SHA256 phải giữ nguyên.

`databaseRejectsEveryInvalidForeignKeyAndReferencedDeletes()` và `databaseRejectsDuplicateLinksTagReuseEmailAndInvalidDomains()` dùng SQL trong transaction để chứng minh DB chặn trực tiếp bằng error 547/2627/515; không chỉ dựa vào Bean Validation. `repositoryValidationRejectsBlankOversizeEmailRoleStatusAndNullRelation()` kiểm tra tầng entity.

`failedLinkRollsBackFlushedArticleAndNewTag()` sửa title và flush, lưu Tag mới, rồi gây lỗi tag đã thuộc bài khác; transaction rollback title/Tag/links. Lặp lại với FK bài có ID dương không tồn tại để tới được SQL FK. Sau mỗi lỗi, `assertFixtureAndQueries()` xác nhận toàn bộ fixture còn đúng và không tăng counts. Đây là test giao dịch repository, chưa tuyên bố Service/API CRUD đã triển khai.

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
| Mapping/constraints News và composite key? | [NewsArticle](../src/main/java/fpt/edu/a2phantuanthanh_se1910/entity/NewsArticle.java), [NewsTag](../src/main/java/fpt/edu/a2phantuanthanh_se1910/entity/NewsTag.java), [NewsTagId](../src/main/java/fpt/edu/a2phantuanthanh_se1910/entity/NewsTagId.java) |
| Kiểm tra references/history/status? | [NewsArticleRepository](../src/main/java/fpt/edu/a2phantuanthanh_se1910/repository/NewsArticleRepository.java) |
| Demo/retest M2? | [NewsPersistenceTests](../src/test/java/fpt/edu/a2phantuanthanh_se1910/persistence/NewsPersistenceTests.java), [PersistenceFixture](../src/test/java/fpt/edu/a2phantuanthanh_se1910/persistence/PersistenceFixture.java), [runner](../scripts/test-persistence.ps1) |
| Auth, CRUD, profile/history API? | Chưa triển khai; bổ sung file/method thật ở M3–M7 |

Khi code từng module, cập nhật luồng và file thực tế kèm mã test trong [test-matrix](test-matrix.md).
