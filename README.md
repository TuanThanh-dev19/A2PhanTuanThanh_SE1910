Author: Phan Tuan Thanh - CE190595

# A2PhanTuanThanh_SE1910

Assignment 02 SBA301 — FUNewsManagementSystem, dùng Spring Boot 3, SQL Server và ReactJS/axios.

## Tiến độ

| Mốc | Kết quả đã có | Commit |
|---|---|---|
| M0 | Skeleton và tài liệu phân tích | `6a26a99` |
| M1 | Boot 3.5.16/JDK 21, kết nối SQL Server; 2 tests pass | `db2e8e6` |
| Chuẩn bị M2 | Docs, header Author và quy ước code | `ea84544` |
| M2 | 5 entity/repository, JPA tạo bảng; 9 tests pass, Unicode/constraint/rollback/restart đạt | `ec46274` |
| M3 | Login BCrypt/JWT, quyền backend, public news, seed demo; 20 tests pass | Commit M3 |

Còn bổ sung ảnh sinh viên chạy IntelliJ. M3 đã có login/JWT, phân quyền và API tin công khai; chưa có CRUD quản trị hoặc React. DEC-01/03 đã áp dụng tạm, chưa được giảng viên xác nhận.

Kế hoạch toàn bài ở [development-plan](docs/development-plan.md); kết quả kiểm tra ở [test-matrix](docs/test-matrix.md). Màn hình/ảnh cần chuẩn bị nằm trong [evidence index](docs/evidence/README.md); sinh viên tự chụp khi thao tác thực tế.

## Chạy backend trên Windows

Cần JDK 21 x64 và SQL Server chạy được qua TCP. Máy đã kiểm tra dùng instance `.\SQL2022`, cổng 1433 và Windows Authentication; máy khác thay bằng cấu hình thực tế.

**1. Tạo database rỗng** nếu chưa có:

```powershell
sqlcmd -S '.\SQL2022' -E -C -b -i scripts/create-database.sql
```

**2. Chuẩn bị Windows Authentication:** tải [native auth DLL 12.10.2 từ Microsoft](https://github.com/microsoft/mssql-jdbc/releases/download/v12.10.2/mssql-jdbc_auth.zip), giải nén để file `mssql-jdbc_auth-12.10.2.x64.dll` nằm trong `tmp/mssql-jdbc-12.10.2/x64/`. Thêm thư mục đó vào PATH của phiên chạy Java:

```powershell
$nativePath = (Resolve-Path tmp/mssql-jdbc-12.10.2/x64).Path
$env:PATH = "$nativePath;$env:PATH"
```

**3. Đặt biến môi trường** trong cùng phiên PowerShell:

```powershell
$env:SQLSERVER_HOST = 'localhost'
$env:SQLSERVER_PORT = '1433'
$env:SQLSERVER_INTEGRATED_SECURITY = 'true'
$env:SQLSERVER_ENCRYPT = 'true'
$env:SQLSERVER_TRUST_SERVER_CERTIFICATE = 'true' # Chỉ dev local với chứng chỉ tự ký
Remove-Item Env:SQLSERVER_USERNAME,Env:SQLSERVER_PASSWORD -ErrorAction SilentlyContinue
```

Tên DB đã cố định là `A2PhanTuanThanh_SE1910`. Mặc định source vẫn kiểm tra chứng chỉ (`trustServerCertificate=false`). Nếu dùng SQL login, đặt integrated security = false và username/password trong môi trường local; nhánh này chưa được kiểm tra trong M1.

[.env.example](.env.example) là mẫu tham khảo; Spring Boot không tự nạp file `.env`. Không đưa secret hoặc DLL vào Git.

**4. Tạo khóa JWT trong cùng phiên PowerShell 7** (lưu local nếu muốn dùng lại sau restart):

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$env:DEMO_ENABLED = 'false'
```

**5. Build/test rồi chạy:**

```powershell
.\scripts\test-m3.ps1 -SqlServerInstance '.\SQL2022'
java -jar target/A2PhanTuanThanh_SE1910-0.0.1-SNAPSHOT.jar
```

Chạy script bằng **PowerShell 7** (`pwsh`). `test-m3.ps1` tạo DB M3 riêng và gọi runner persistence chạy `clean verify`: 2 tests M1 trên DB bài tập, 7 M2 và 11 M3 trên DB test riêng. Cần quyền CREATE DATABASE qua Windows Authentication. DB test/log local được giữ trong `tmp/m2-results/`, không drop/reset. Report Maven ở `target/surefire-reports`. Nếu chạy Maven trực tiếp, đặt cả `SQLSERVER_TEST_DATABASE` (prefix M2Test) và `SQLSERVER_M3_TEST_DATABASE` (prefix M3Test); hai DB phải khác nhau, DB M2 cần mới/rỗng. `test-persistence.ps1` vẫn chạy riêng 9 tests cũ khi không đặt biến DB M3.

Backend dùng cổng 8080; GET `/api/public/news` trả 200; GET `/` không có quyền public, trả 401. Ctrl+C để dừng. `ddl-auto=update` tạo bảng/FK thiếu, chỉ seed demo khi bật `DEMO_ENABLED`; dùng `--spring.jpa.hibernate.ddl-auto=validate` để kiểm tra schema đã có. `update` phục vụ dev theo DEC-03, không thay thế migration khi cần thay đổi schema/dữ liệu phức tạp.

Fixture nằm trong test source: 3 account, 3 category, 3 news, 3 tag và 2 links; có tiếng Việt, active/inactive, parent/updater nullable và tag tự do. Password là chuỗi ngẫu nhiên dành riêng cho test, không phải credential thật hoặc dữ liệu đăng nhập M3. Demo login M3 dùng seeder riêng, bật có chủ đích theo hướng dẫn bên dưới.

**IntelliJ:** mở `pom.xml`, reload Maven, chọn JDK 21 và đặt các biến trên trong Run Configuration. Cấu hình PATH chứa DLL hoặc VM option `-Djava.library.path=<thư mục x64>`. Sinh viên chạy main và lưu ảnh IDE/log làm minh chứng `TC-SETUP-03`.

## Tài liệu

Bộ docs dùng bố cục báo cáo của Assignment 01: requirement/design, hướng dẫn giải thích code, test Expected/Actual và nhật ký theo milestone; nội dung và trạng thái theo Assignment 02.

| File | Nội dung |
|---|---|
| [requirements](docs/requirements.md) | Bài yêu cầu gì và những điểm cần chốt |
| [design](docs/design.md) | Kiến trúc, dữ liệu, API và validation dự kiến |
| [development-plan](docs/development-plan.md) | Kế hoạch cố định M0–M8 và cách làm Git |
| [code-flow](docs/code-flow.md) | Code hiện có hoạt động thế nào |
| [test-matrix](docs/test-matrix.md) | Các ca cần kiểm tra và kết quả đã chạy |
| [debug-log](docs/debug-log.md) | Lỗi thật, cách sửa và kiểm tra lại |
| [ai-usage-log](docs/ai-usage-log.md) | Phần AI hỗ trợ và người đã kiểm tra |
| [evidence](docs/evidence/README.md) | Log/report của từng lần chạy |

Cập nhật kết quả và hướng dẫn khi cần; giữ kế hoạch tổng thể ổn định. Chỉ ghi PASS khi đã chạy. Hướng dẫn Microsoft: [JDBC Windows Authentication](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver17#connecting-with-integrated-authentication-on-windows).


## M3: đăng nhập và tin công khai

JWT dùng HS256 qua Spring Security/Nimbus, hạn 1 giờ. Khóa bắt buộc lấy từ `JWT_SECRET`; thiếu/sai Base64 hoặc dưới 32 byte thì startup thất bại. Giữ cùng khóa giữa các lần chạy để token chưa hết hạn còn dùng được. CORS mặc định `http://localhost:5173`, đổi bằng `CORS_ALLOWED_ORIGIN`.

Để thêm demo vào DB bài tập, đặt biến sau rồi chạy JAR ở trên:

```powershell
$env:DEMO_ENABLED = 'true'
$env:DEMO_ADMIN_PASSWORD = Read-Host 'Password demo Admin (8 ký tự trở lên)' -MaskInput
$env:DEMO_STAFF_PASSWORD = Read-Host 'Password demo Staff (8 ký tự trở lên)' -MaskInput
java -jar target/A2PhanTuanThanh_SE1910-0.0.1-SNAPSHOT.jar
```

Email mặc định: `admin.m3@example.com` (role 1), `staff.m3@example.com` (role 2); có thể đổi qua `DEMO_ADMIN_EMAIL`/`DEMO_STAFF_EMAIL`. Password tối đa 72 byte UTF-8, không trim. Seeder chỉ thêm account/category/bài demo còn thiếu; chạy lại không đổi password/hash/nội dung đã có. Email đã có role khác thì dừng, không sửa account đó. Sau lần seed đầu có thể đặt `DEMO_ENABLED=false`; không reset hoặc xóa dữ liệu.

| Request | Kết quả |
|---|---|
| `POST /api/auth/login` với JSON `{"email":"...","password":"..."}` | 200: accessToken, tokenType, expiresAt, account (ID/name/email/role); không có hash |
| `GET /api/public/news?q=` | Danh sách active, mới nhất trước; tìm title/headline/content, không phân biệt hoa thường |
| `GET /api/public/news/{id}` | 200 kèm content/category/authorName/tags; inactive/không tồn tại: 404 |

API quản trị gửi `Authorization: Bearer <accessToken>`. Thiếu/sai/hết hạn token: 401; sai role: 403. Backend đọc lại role/account từ DB mỗi request. Admin chỉ được `/api/accounts/**`; Staff được category/news/tags/profile (history ở `/api/profile/news`). Route CRUD/profile/history chưa có controller nên đúng role hiện nhận 404; không coi đây là CRUD đã làm. Hướng dẫn request/ảnh tự chụp ở [evidence](docs/evidence/README.md#m3--postman-và-sql-server).
