# A2PhanTuanThanh_SE1910

Assignment 02 SBA301: FUNewsManagementSystem dùng Spring Boot 3, SQL Server và ReactJS/axios. Hiện mới triển khai nền tảng backend M1.

## Trạng thái ngày 28/10/2026

- Đã đọc và đối chiếu đủ 5 trang đề, sơ đồ database, ảnh cấu trúc `docs` và tiêu chí C2 do sinh viên cung cấp.
- Baseline Git: `6a26a99`; nay đã có Git local và origin `https://github.com/TuanThanh-dev19/A2PhanTuanThanh_SE1910.git`. Phiên M1 chưa push, chưa xác minh attribution trên GitHub.
- Backend: **Spring Boot 3.5.16**, Java 21, Web, Data JPA, Validation và Microsoft JDBC `12.10.2.jre11` do Boot quản lý. Đây là bản phát hành ổn định thuộc 3.x phù hợp yêu cầu đề; [Spring xác nhận tương thích Java 17–25](https://docs.spring.io/spring-boot/3.5/system-requirements.html). [Release notes](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/) ghi đây là bản OSS cuối của nhánh 3.5, không đồng nghĩa còn hỗ trợ OSS hiện tại.
- AI/tool đã chạy `./mvnw.cmd -B clean verify`: **2 tests pass, 0 failures/errors/skipped**; đã chạy JAR và xác minh session SQL Server thật bằng PID Java. [Evidence M1](docs/evidence/2026-10-28-m1/inspection.md).
- Database `A2PhanTuanThanh_SE1910` đã được tạo trên instance local `LAPTOP-JED9V0CH\SQL2022`, TCP 1433, Windows Authentication theo lựa chọn sinh viên. Chưa có entity, repository nghiệp vụ, CRUD, auth hay React; `ddl-auto=none`, 0 bảng nghiệp vụ.
- **M1 còn thiếu sinh viên mở/chạy project trong IntelliJ và cung cấp evidence TC-SETUP-03**. CLI pass không nghiệm thu yêu cầu IDE.
- Khảo sát ban đầu Boot 4/1 test/chưa Git được giữ tại [baseline inspection](docs/evidence/2026-10-28-baseline/inspection.md) và phần 1 development-plan; không dùng evidence Boot 4 để nghiệm thu Boot 3. Các thiết kế nghiệp vụ bên dưới vẫn là dự kiến.

## Đọc tài liệu theo thứ tự

1. [Yêu cầu và phạm vi](docs/requirements.md): yêu cầu có mã, nguồn, tiêu chí nghiệm thu và điểm cần làm rõ.
2. [Thiết kế](docs/design.md): kiến trúc ba lớp, database, API, phân quyền và validation.
3. [Quy trình cá nhân và Git](docs/development-plan.md): mốc triển khai, điều kiện hoàn thành, commit và tài liệu đi kèm.
4. [Luồng code](docs/code-flow.md): phân biệt code hiện có với luồng dự kiến.
5. [Ma trận kiểm thử](docs/test-matrix.md): test theo yêu cầu, kết quả thực tế và việc chưa chạy.
6. [Nhật ký lỗi](docs/debug-log.md) và [nhật ký dùng AI](docs/ai-usage-log.md).
7. [Quy ước minh chứng](docs/evidence/README.md).

## Cấu hình và chạy backend trên Windows

Yêu cầu: JDK 21 x64, SQL Server đang chạy/TCP khả dụng, Maven wrapper và mạng tải dependencies. Tests hiện kết nối DB thật; phải cấu hình môi trường trước cả `test`/`verify`. Không có H2 thay thế. [.env.example](.env.example) chỉ là bảng mẫu, **Spring Boot không tự đọc `.env`**; export biến trong PowerShell hoặc cấu hình môi trường của IntelliJ.

1. Kiểm tra đúng instance và tạo DB rỗng bằng danh tính có quyền (script chỉ tạo khi chưa tồn tại, không tạo bảng):

```powershell
# Instance đã xác minh trên máy này; máy khác thay bằng instance thật.
sqlcmd -S '.\SQL2022' -E -C -b -i scripts/create-database.sql
```

2. Windows Authentication cần native DLL đúng version/kiến trúc JVM. Tải [gói auth 12.10.2 từ Microsoft](https://github.com/microsoft/mssql-jdbc/releases/download/v12.10.2/mssql-jdbc_auth.zip), giải nén vào thư mục local bị ignore, rồi thêm thư mục `x64` vào PATH **trước khi mở Maven/Java/IntelliJ**. JAR JDBC được Maven tải riêng, không commit DLL. [Hướng dẫn Microsoft](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver17#connecting-with-integrated-authentication-on-windows).

```powershell
New-Item -ItemType Directory -Force tmp/mssql-jdbc-12.10.2 | Out-Null
Invoke-WebRequest -Uri 'https://github.com/microsoft/mssql-jdbc/releases/download/v12.10.2/mssql-jdbc_auth.zip' -OutFile tmp/mssql-jdbc-12.10.2/auth.zip
Expand-Archive -LiteralPath tmp/mssql-jdbc-12.10.2/auth.zip -DestinationPath tmp/mssql-jdbc-12.10.2 -Force
$nativePath = (Resolve-Path tmp/mssql-jdbc-12.10.2/x64).Path
Get-AuthenticodeSignature "$nativePath/mssql-jdbc_auth-12.10.2.x64.dll"
$env:PATH = "$nativePath;$env:PATH"
```

3. Export các biến cho phiên PowerShell hiện tại:

```powershell
$env:SQLSERVER_HOST = 'localhost' # Đã xác minh tới SQL2022 qua TCP 1433.
$env:SQLSERVER_PORT = '1433'
$env:SQLSERVER_INTEGRATED_SECURITY = 'true'
$env:SQLSERVER_ENCRYPT = 'true'
$env:SQLSERVER_TRUST_SERVER_CERTIFICATE = 'true' # Chỉ dev local với chứng chỉ tự ký.
Remove-Item Env:SQLSERVER_USERNAME,Env:SQLSERVER_PASSWORD -ErrorAction SilentlyContinue
```

Mặc định `encrypt=true`, `trustServerCertificate=false`; ngoài dev local dùng chứng chỉ tin cậy và giữ kiểm tra chứng chỉ. Database được cố định đúng tên đề trong `application.properties`. Nếu dùng SQL login thay Windows: đặt `SQLSERVER_INTEGRATED_SECURITY=false`, `SQLSERVER_USERNAME` và `SQLSERVER_PASSWORD` bằng thông tin được cấp trong môi trường local; không đặt secret trong JDBC URL, source, evidence hoặc commit. Nhánh SQL login chưa được chạy trong M1.

4. Build/test và chạy trong cùng phiên đã cấu hình:

```powershell
.\mvnw.cmd -B clean verify
java -jar target/A2PhanTuanThanh_SE1910-0.0.1-SNAPSHOT.jar
```

Backend mặc định dùng cổng 8080; có thể thêm `--server.port=0` để chọn cổng trống (đã smoke test cách này). GET `/` trả 404 là expected vì chưa có controller nghiệp vụ. Dừng bằng Ctrl+C sau khi chạy thủ công.

IntelliJ (sinh viên cần tự làm, **chưa được xác minh**): mở `pom.xml`, reload Maven, chọn Project SDK/Maven runner JDK 21, cấu hình các biến trên trong Run Configuration và native DLL qua PATH hoặc VM option `-Djava.library.path=<thư mục x64>`. Chạy main, lưu ảnh IDE/log Boot 3 và kết nối SQL Server. Không chụp secret. Frontend sẽ có hướng dẫn khi được triển khai.

## Nguyên tắc theo dõi

Mỗi thay đổi chức năng phải nối được chuỗi `REQ → thiết kế → code → test → evidence → commit`. Chỉ ghi PASS khi thực sự chạy; chỉ ghi FIXED khi đã sửa và kiểm tra lại. C2 được chứng minh bằng sự phát triển có thể giải thích, không bằng chỉ tiêu số lượng commit.
