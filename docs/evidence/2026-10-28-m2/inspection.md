Author: Phan Tuan Thanh - CE190595

# M2 — data model and persistence inspection

## Môi trường và revision

- Người chạy: Codex/terminal tool ngày 28/10/2026, UTC+07:00; không phải minh chứng sinh viên chạy IntelliJ.
- Đã đọc AGENTS.md, README, toàn bộ docs; đọc đủ 5 trang PDF Assignment 02 và render/xem trực tiếp hình Database Design trang 3. PDF nguồn nằm ở thư mục cha SBA301; ảnh đọc tạm trong `tmp/assignment02-database.png`, không đưa đề/ảnh vào Git.
- Trước M2: `db2e8e6` + docs/header/quy ước chưa commit. Review phần này, giữ source behavior/evidence/Wrapper, lưu riêng tại `ea845447eeefdad733e77f106394cbe4c4ed4df0` (`docs: record author headers and readable code conventions`).
- Test chạy trên base `ea84544` + working tree M2; source, docs và evidence được lưu cùng commit tính năng này. `manifest.json` ghi SHA256 source/evidence, không suy diễn code revision từ commit chưa tồn tại lúc chạy.
- Java 21.0.8 x64, Maven Wrapper 3.10.0, Boot 3.5.16, Hibernate 6.6.53.Final, JDBC 12.10.2.jre11; SQL Server `.\SQL2022`, TCP localhost:1433, version 16.0.1000.6. Windows Authentication, native x64 DLL sẵn ở tmp; không đoán username/password.
- DB bài tập được kiểm tra trước triển khai: ONLINE, 0 user tables. Run-02 bắt đầu sinh schema; sau cùng vẫn 0 record ở mọi bảng. Collation `SQL_Latin1_General_CP1_CI_AS`; không tự đổi collation. [Trạng thái cuối](final-sql-state.txt).
- Mỗi run tạo DB test mới, không drop/reset; DB run cuối: `A2PhanTuanThanh_SE1910_M2Test_20261028_182815_765a698b`. Giữ cả DB test lỗi để đối chiếu nếu cần, không dùng lại fixture vào DB có dữ liệu.

## Lệnh tái hiện

PowerShell 7, JDK 21 và SQL Server thật; đặt SQLSERVER_* và PATH DLL theo README. Với cấu hình Windows Authentication đã kiểm chứng:

```powershell
.\scripts\test-persistence.ps1 -SqlServerInstance '.\SQL2022' -EvidenceDirectory <thu-muc-evidence-moi>
```

Runner tạo DB test rỗng tên duy nhất và đặt SQLSERVER_TEST_DATABASE chỉ trong phiên chạy; chạy `mvnw.cmd -B clean verify`, copy reports dạng text, dùng `sqlcmd -E -C -b -f 65001` xuất metadata/data và chạy packaged JAR với `--server.port=0`. M1 test/JAR dùng DB bài tập; test lỗi và fixture dùng DB biệt lập. Runner khôi phục biến test DB và dừng đúng process mình đã mở; không push.

Tests M2 chỉ nhận database name đúng prefix test; fixture kiểm tra tất cả bảng rỗng trước khi tạo. Script từ chối evidence directory có sẵn nội dung. Kết quả script run-05 exit code **0**. Không dùng H2/mock để thay kết quả SQL Server.

## Entity, repository và quyết định

- 5 bảng/26 cột trong schema dbo: SystemAccount (5), Category (5), NewsArticle (11), Tag (3), NewsTag (2). ID Long/bigint identity cho 4 entity, PK NewsTag là NewsArticleID+TagID, `@EmbeddedId/@MapsId` và key Serializable/equals/hashCode.
- 6 FK: parent category; article category/creator/updater; NewsTag article/tag. Category/creator của news bắt buộc; parent/updater nullable; mọi FK trusted/enabled và NO_ACTION, không cascade/orphan removal.
- Text nvarchar, content nvarchar(max) NOT NULL, datetime2(6); mapping/nullable/độ dài tường minh. Bean Validation cho field, SQL CHECK role 1/2, status 0/1 và category không tự parent; unique email và unique TagID.
- 5 JpaRepository: email ignore-case, category children, references creator/category/updater, history theo creator (cả hai status), lọc news theo status, links theo article/tag; tất cả query mới được thực chạy trong fixture assertions.
- DEC-01 áp dụng tạm unique TagID theo yêu cầu sinh viên: một tag tối đa một bài, bài có nhiều tags. Chưa có xác nhận giảng viên.
- DEC-03 giữ script tạo DB rỗng; JPA update tạo bảng/FK, không create/create-drop/reset/seed startup. Chưa có xác nhận giảng viên về cách hiểu yêu cầu. DEC-02 (type/length/nullability) là lựa chọn triển khai theo design, không phải kiểu dữ liệu do PDF bắt buộc.
- Fixture test-only lưu qua repository trong transaction: 3 accounts (admin/hai staff), 3 categories (parent/child inactive/unused), 3 articles (active/inactive, hai creator), 3 tags (2 linked/1 free) và 2 links. Password là chuỗi UUID ngẫu nhiên, không credential thật, chưa phải password hash/login M3. Không có bean startup seeder.

## Expected / actual cuối (run-05)

| Test | Expected | Actual / kết quả |
|---|---|---|
| Build + M1 regression | Boot 3/JDK 21, test context/JDBC đúng DB/product | `clean verify` BUILD SUCCESS, 9 tests, 0 failures/errors/skipped, 56.971 s; M1 gốc 2/2 và M2 7/7 pass |
| TC-DB-01 | JPA tạo đúng schema trên DB rỗng, giữ schema qua restart | Assert đúng từng tên/kiểu/length/nullability của 26 cột, 5 tables/PKs, 6 FK, 2 unique, 3 CHECK, 4 user identities. Assert lại toàn schema sau mỗi restart và sau negative tests. Content NOT NULL trong snapshot trước/sau JVM |
| TC-DB-02 Unicode/relations | Lưu/đọc đúng tiếng Việt, FK/parent/audit/nullables | Nội dung 5.800 ký tự (>4000) round-trip chính xác; tên/title/headline/source/note/description/quan hệ đúng; audit giữ giá trị; account/category/news/tag counts 3/3/3/3 và links 2 |
| TC-DB-02 restart context | Đóng context/EMF/pool, tạo context mới và đọc lại dữ liệu commit | Đạt khi mở `validate`, sau đó `update`; không dùng transaction test tự rollback toàn fixture |
| TC-DB-02 restart process | Packaged JAR chạy trong JVM mới, DB/data/schema không đổi | Hai process riêng PID 16416 và 35396, GET `/`=404; snapshot trước/sau cùng SHA256 `71A47FFAB8FD13D9507B54FD34E6B7841DAC5CBC6D51317FCDF4CB7108368A98`, bao gồm Unicode content byte lengths/hash |
| TC-DB-03 SQL constraints | Sai mỗi FK, delete referenced rows, duplicate link/email/tag reuse, role/status/self-parent/null sai đều bị chặn | Error code 547/2627/515 đúng như assert. Cả 6 FK được thử; tag tự do tồn tại, tag đã gắn không thể thuộc bài thứ hai; content/category/creator null bị chặn tại SQL |
| TC-DB-03 entity validation | Blank, invalid email/role, oversize tag, status/null relation sai bị chặn | ConstraintViolationException đúng; fixture không đổi sau rollback |
| TC-DB-03 atomicity | Thất bại sau khi đã flush title và Tag mới không để lại cập nhật một phần | Hai transaction lỗi: reuse tag và FK article ID dương không tồn tại. Cả title và Tag mới rollback; toàn bộ query/quan hệ/count/schema còn đúng sau mỗi ca |
| M1 packaged JAR/session | Startup được; không controller nên 404; kết nối DB bài tập | Đạt; session TCP/NTLM/encrypt TRUE đúng DB; process đã dừng, cuối cùng 0 application sessions |

Minh chứng chính: [build](run-05/build-test.txt), [M1 report](run-05/fpt.edu.a2phantuanthanh_se1910.A2PhanTuanThanhSe1910ApplicationTests.txt), [M2 report](run-05/fpt.edu.a2phantuanthanh_se1910.persistence.NewsPersistenceTests.txt), [schema/data](run-05/isolated-schema-data.txt), [process restart proof](run-05/process-restart-verification.txt), [snapshot sau restart](run-05/isolated-after-process-restarts.txt), [JAR smoke](run-05/application-smoke.txt), [SQL sessions](run-05/sqlserver-application-session.txt), [DB bài tập cuối](run-05/assignment-schema-data.txt). Startup/stderr của từng JVM nằm cùng run-05, stderr rỗng là output thật.

## Lỗi thật và retest

| Lần chạy | Actual | Sửa / cách sử dụng evidence |
|---|---|---|
| run-01 | Test compile fail vì JUnit Tag và entity Tag wildcard import trùng tên; chưa chạy test | Import tường minh. Giữ build/environment log, không ghi Pass |
| run-02 | M1 2 pass; M2 7 chạy, 2 fail: identity đếm cả system tables; ID âm bị @Positive chặn trước FK | Lọc user tables; dùng Long.MAX_VALUE dương cho FK case. Giữ failed report và identity diagnostic |
| run-03 | 9 tests pass, build success; SQL snapshot lỗi 156 do alias RowCount | Đổi alias TotalRows; giữ nguyên SQL output lỗi. Chưa đạt toàn runner |
| run-04 | 9 tests pass, runner/JAR restart pass, nhưng review SQL thấy NewsContent nullable=1 sau update | Không lấy run-04 nghiệm thu schema. JDBC metadata xác nhận COLUMN_SIZE=2147483647; length mặc định khác metadata làm update ALTER type và mất NOT NULL. Sửa length=Integer.MAX_VALUE, giữ @Size(20000), thêm assertion toàn schema sau restart và SQL null content |
| repair trước run-05 | Khôi phục NOT NULL ở DB bài tập do các lần test M2 tạo ra; không xử lý DB test lịch sử | [Script có guard](repair-content-nullability.sql) từ chối khi có content null; [output](repair-content-nullability.txt) xác nhận 0 dòng trước/sau, is_nullable=0. Không xóa/reset bảng hoặc record |
| run-05 | 9 tests pass, build/harness exit 0; nullability và metadata/data giữ đúng sau context + hai JVM restart | Đạt TC-DB-01..03 và regression M1 trong phạm vi kỹ thuật M2 |

Run-04 có một lệnh JShell đầu tiên lỗi quoting PowerShell trước khi đọc metadata (`content-jdbc-diagnostic.txt`); lệnh đã sửa ở `content-jdbc-metadata.txt` trả đúng COLUMN_SIZE/NULLABLE. Không coi lỗi công cụ này là lỗi ứng dụng. Lỗi SQL 547 trong build cuối là negative test mong đợi, đã được assert; không phải build thất bại.

## Giới hạn và provenance

- M2 đạt phạm vi kỹ thuật đã yêu cầu; DEC-01/03 vẫn cần xác nhận giảng viên. Chưa có Controller/REST CRUD/Service nghiệp vụ/auth/JWT/React, DTO mapping/validation, kiểm tra creator phải Staff, chuẩn hóa email, audit tự gán hay chống chu kỳ category nhiều cấp; các mục này thuộc M3–M7.
- Chưa chạy SQL Authentication, TLS với chứng chỉ tin cậy, máy khác hoặc IntelliJ do sinh viên thao tác. Không có evidence FE/API và không ghi Pass cho các mục đó. OBS-001 còn timeout metadata apache.snapshots, cảnh báo Mockito/CDS và Hibernate metadata unknown; build/test/session vẫn đạt.
- Giữ baseline/M1/Wrapper nguyên vẹn. Logs/sqlcmd reports/checksum manifests là output ghi lại/sinh tự động, không thêm Author vào log cũ. Markdown và SQL repair do project viết có header; scripts nguồn ở `scripts/` có header.
- `manifest.json` ghi checksum source và evidence để đối chiếu; QA cuối kiểm tra author, link, diff, không lộ secret. Không push. Sinh viên vẫn cần đọc, tự chạy và giải thích code trước khi nộp.
