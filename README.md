Author: Phan Tuan Thanh - CE190595

# A2PhanTuanThanh_SE1910

Assignment 02 SBA301 — FUNewsManagementSystem, dùng Spring Boot 3, SQL Server và ReactJS/axios.

## Tiến độ

- Baseline: `6a26a99`. Nền tảng M1: `db2e8e6`.
- Đã có Boot **3.5.16**, Java **21**, Web/JPA/Validation/JDBC và kết nối SQL Server bằng Windows Authentication. Kết quả đã lưu: **2 tests pass**, JAR chạy được. [Evidence M1](docs/evidence/2026-10-28-m1/inspection.md).
- M1 còn thiếu minh chứng sinh viên chạy trong IntelliJ. Chưa có entity, bảng nghiệp vụ, CRUD, auth hoặc React. Bước tiếp theo: M2.

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

**4. Build/test rồi chạy:**

```powershell
.\mvnw.cmd -B clean verify
java -jar target/A2PhanTuanThanh_SE1910-0.0.1-SNAPSHOT.jar
```

Tests hiện cần SQL Server thật. Backend dùng cổng 8080; GET `/` trả 404 vì chưa có controller. Ctrl+C để dừng.

**IntelliJ:** mở `pom.xml`, reload Maven, chọn JDK 21 và đặt các biến trên trong Run Configuration. Cấu hình PATH chứa DLL hoặc VM option `-Djava.library.path=<thư mục x64>`. Sinh viên chạy main và lưu ảnh IDE/log làm minh chứng `TC-SETUP-03`.

## Tài liệu

Bộ docs dùng bố cục báo cáo của Assignment 01: requirement/design, hướng dẫn giải thích code, test Expected/Actual và nhật ký theo milestone; nội dung và trạng thái theo Assignment 02.

| File | Nội dung |
|---|---|
| [requirements](docs/requirements.md) | Bài yêu cầu gì và những điểm cần chốt |
| [design](docs/design.md) | Kiến trúc, dữ liệu, API và validation dự kiến |
| [development-plan](docs/development-plan.md) | Đã làm đến đâu, làm tiếp gì và cách commit |
| [code-flow](docs/code-flow.md) | Code hiện có hoạt động thế nào |
| [test-matrix](docs/test-matrix.md) | Các ca cần kiểm tra và kết quả đã chạy |
| [debug-log](docs/debug-log.md) | Lỗi thật, cách sửa và kiểm tra lại |
| [ai-usage-log](docs/ai-usage-log.md) | Phần AI hỗ trợ và người đã kiểm tra |
| [evidence](docs/evidence/README.md) | Log/report của từng lần chạy |

Cập nhật docs và evidence cùng tính năng; chỉ ghi PASS khi đã chạy. Hướng dẫn Microsoft: [JDBC Windows Authentication](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver17#connecting-with-integrated-authentication-on-windows).
