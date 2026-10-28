# M1: nền tảng Boot 3 và SQL Server thật

Ngày: 28/10/2026, Asia/Bangkok (UTC+07:00). Người chạy: **AI/tool**, không phải bằng chứng sinh viên thao tác IntelliJ.

## Revision và phạm vi

- Base `6a26a997173085c5e6eaeb3dbdf7fc44eefc0be9`, nhánh `main`, working tree sạch khi bắt đầu. Test chạy trên **base + working tree**, không phải một SHA sạch đã commit. Implementation/evidence thuộc **cùng commit này**: `fix: align backend with Spring Boot 3 assignment requirement`.
- Inputs thay đổi khi build: `pom.xml`, `src/main/resources/application.properties`, `src/test/java/fpt/edu/a2phantuanthanh_se1910/A2PhanTuanThanhSe1910ApplicationTests.java`. Main và Maven wrapper giữ nguyên. `.gitignore`, `.env.example`, `scripts/create-database.sql` hỗ trợ cấu hình; docs cập nhật sau khi chạy. Manifest ghi checksum code/config và evidence; `git-status.txt` là snapshot lúc review, không giả là status ngay khi test bắt đầu.
- REQ-01/02/03 phần nền tảng; TC-SETUP-01, TC-SETUP-02-BE-DB, TC-M1-DB/HTTP/SCHEMA; DBG-001. Chưa nghiệm thu các REQ đầy đủ.
- Đã đọc README, toàn bộ bộ docs/evidence và code/Git thực tế. Không thấy AGENTS.md trên đĩa trong repo và các thư mục cha tới D:\; hướng dẫn AGENTS trong chat cho phép dùng Postman CLI. Không cần collection/API nghiệp vụ ở M1.

## Môi trường và cấu hình

- [environment.txt](environment.txt): JDK 21.0.8 x64, Maven wrapper 3.10.0, Windows 11. [dependencies.txt](dependencies.txt): Boot 3.5.16, Web/Data JPA/Validation/Test, JDBC 12.10.2.jre11. Hibernate runtime 6.6.53.Final trong log.
- Git local đã có origin `https://github.com/TuanThanh-dev19/A2PhanTuanThanh_SE1910.git`; baseline author Phan Tuan Thanh, email phanthanh19112005@gmail.com. Đây không phải xác minh attribution/quyền push trên GitHub; phiên M1 chưa push.
- SQL service MSSQL$SQL2022 Running. Quan sát SQL metadata: server `LAPTOP-JED9V0CH\SQL2022`, SQL Server 16.0.1000.6; TCP listener 1433. `sqlcmd -S '.\SQL2022' -E -C` kết nối được bằng Windows identity; DB yêu cầu chưa tồn tại khi khảo sát.
- Sinh viên trả lời trong chat chọn **Windows Authentication hiện tại**. Không đoán hay tạo SQL login/password. Không có biến SQL/JDBC ban đầu; tool export các biến sau trong tiến trình PowerShell chạy build/app:

```text
SQLSERVER_HOST=localhost
SQLSERVER_PORT=1433
SQLSERVER_ENCRYPT=true
SQLSERVER_TRUST_SERVER_CERTIFICATE=true
SQLSERVER_INTEGRATED_SECURITY=true
PATH=<repo>/tmp/mssql-jdbc-12.10.2/x64;<PATH có sẵn>
```

Username/password không được cấu hình; trust certificate chỉ bật cho dev local, default source vẫn false. [native-driver.txt](native-driver.txt) ghi URL Microsoft GitHub, SHA-256, chữ ký Authenticode Valid của DLL 12.10.2 x64. Binary/ZIP ở `tmp/` bị ignore; không commit. Máy khác phải tải/cấu hình theo README.

## Lệnh, expected và actual

1. `sqlcmd -S '.\SQL2022' -E -C -l 5 -b -i scripts/create-database.sql`: exit 0, tạo DB rỗng nếu chưa có. [database-create.txt](database-create.txt). Tạo database bằng SQL Server identity, **không phải Hibernate tạo schema**; DEC-03 chưa được giảng viên chốt.
2. `sqlcmd -S 'tcp:localhost,1433' -E -C -l 5 -b -d A2PhanTuanThanh_SE1910` với query metadata: đúng DB, TCP 1433, NTLM; 0 user tables. [sqlserver-before.txt](sqlserver-before.txt).
3. `mvnw.cmd -B dependency:tree -Dincludes=org.springframework.boot:*,com.microsoft.sqlserver:mssql-jdbc -DoutputFile=tmp/m1-dependencies.txt`: exit 0, output sao sang [dependencies.txt](dependencies.txt). Có timeout metadata apache.snapshots nhưng goal vẫn thành công.
4. `mvnw.cmd -B clean verify`: **exit 0, BUILD SUCCESS**, kết thúc **14:09:48 +07:00**, tổng 39.832 s. Hai tests, 0 failures/errors/skipped. `contextLoads` assert EntityManagerFactory mở; test JDBC assert product Microsoft SQL Server, `SELECT DB_NAME(), 1` trả đúng DB và 1. [build-test.txt](build-test.txt), [Surefire report](context-test.txt). Driver/pool được tạo thật, không mock/H2/skip test.
5. `java -jar target/A2PhanTuanThanh_SE1910-0.0.1-SNAPSHOT.jar --server.port=0`: Boot 3.5.16, Hikari kết nối, JPA factory initialized, Tomcat chạy cổng **53663**, PID **44728**. HTTP GET `/` trả **404** như expected vì chưa controller. [application-start.txt](application-start.txt), [application-stderr.txt](application-stderr.txt), [runtime-smoke.txt](runtime-smoke.txt). Đây là smoke HTTP startup, không phải API nghiệp vụ.
6. Trong khi JAR chạy: join `sys.dm_exec_sessions` với `sys.dm_exec_connections`, lọc `program_name=N'A2PhanTuanThanh_SE1910' AND host_process_id=44728`, kiểm tra tồn tại session đúng DB; exit 0. Actual: DB A2PhanTuanThanh_SE1910, TCP/1433, NTLM, encrypt TRUE. Có 10 connection trong pool, 0 user tables. [sqlserver-application-session.txt](sqlserver-application-session.txt). Sau đó tool dừng đúng process smoke; không test graceful shutdown.

Query xác minh session (PID thay theo lần chạy):

```sql
SELECT s.program_name, s.host_process_id, DB_NAME(s.database_id) AS database_name,
       c.net_transport, c.auth_scheme, c.encrypt_option, c.local_tcp_port
FROM sys.dm_exec_sessions s
JOIN sys.dm_exec_connections c ON s.session_id = c.session_id
WHERE s.program_name = N'A2PhanTuanThanh_SE1910' AND s.host_process_id = 44728;
SELECT COUNT(*) AS user_table_count FROM sys.tables WHERE is_ms_shipped = 0;
```

## Kết luận và giới hạn

- TC-SETUP-01, TC-SETUP-02 phần BE/DB, TC-M1-DB/HTTP/SCHEMA: **PASS** trong phạm vi đã nêu. DBG-001 **VERIFIED** bằng build/test/runtime Boot 3, không chỉ sửa version trong POM.
- **M1 chưa đầy đủ:** TC-SETUP-03 sinh viên mở/chạy trong IntelliJ và evidence IDE **NOT RUN**. Chưa có bằng chứng sinh viên tự build/demo/giải thích diff. Hướng dẫn IntelliJ trong README chưa phải minh chứng thực hiện.
- Chưa entity/repository nghiệp vụ, schema JPA, seed, CRUD, auth hay React; M2-M8 và DEC-01/03 vẫn chờ. Chưa test SQL login, TLS với chứng chỉ tin cậy, restart persistence, checkout sạch trên máy khác hoặc GitHub attribution/push.
- OBS-001: Maven snapshots timeout, Mockito dynamic agent và JVM CDS warnings còn thấy, chưa sửa. Hibernate hiển thị một số metadata pool/driver unknown không thay thế assertions JDBC và session SQL thật.
- Baseline Boot 4 trong `../2026-10-28-baseline/` giữ nguyên. Code checksums trong manifest cố định working tree đã test; SHA của commit M1 lấy bằng Git sau commit, không nhét SHA dự đoán vào docs.
- Review M1: diff code/docs đối chiếu log, baseline không có diff, mẫu `.env.example` được giữ trong Git còn `.env*` local/`tmp`/`target` bị ignore. Quét mẫu secret chỉ gặp placeholder cấu hình và kiểu trường AccountPassword dự kiến, không có credential thật. Liên kết Markdown local và checksum được kiểm tra trước commit. Text stdout/evidence mới được chuẩn hóa bỏ khoảng trắng cuối dòng để qua `git diff --check`, không bỏ dòng nội dung; baseline không chuẩn hóa lại.

Nguồn kỹ thuật: [Boot 3.5 system requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html), [Boot 3.5.16 release](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/), [Microsoft JDBC Windows integrated authentication](https://learn.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-ver17#connecting-with-integrated-authentication-on-windows), [Microsoft JDBC release notes](https://learn.microsoft.com/en-us/sql/connect/jdbc/release-notes-for-the-jdbc-driver?view=sql-server-ver17#1210).
