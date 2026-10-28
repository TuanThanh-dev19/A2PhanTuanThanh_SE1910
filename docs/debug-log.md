Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Debug Log

Tài liệu ghi lỗi thật, nguyên nhân, cách sửa và kết quả kiểm tra lại. Không tạo lỗi để tăng số commit.

| Ngày | Milestone / ID | Hiện tượng | Nguyên nhân | Cách sửa | Kiểm chứng |
|---|---|---|---|---|---|
| 2026-10-28 | M1 / DBG-001 | POM/test chạy Boot 4.1.2 trong khi đề yêu cầu Boot 3 | Parent version trong skeleton là 4.1.2; chưa rõ lý do chọn lúc tạo | Đổi Boot 3.5.16/JDK 21, thêm Web/JPA/Validation/JDBC và cấu hình datasource | **VERIFIED** bởi AI/tool: `clean verify` lúc 14:09:48 +07:00 có 2 tests pass; JAR chạy Boot 3 và kết nối SQL thật. Commit `db2e8e6`; [evidence M1](evidence/2026-10-28-m1/inspection.md) |
| 2026-10-28 | Baseline, M1 / OBS-001 | Timeout metadata apache.snapshots, cảnh báo Mockito agent/JVM CDS; một số metadata Hibernate unknown | Timeout được quan sát; chưa điều tra nguyên nhân mạng/tooling đầy đủ | Chưa sửa; tiếp tục theo dõi nếu ảnh hưởng chạy/test | Hai build vẫn thành công; JDBC assertions và SQL session xác nhận kết nối. [Build M1](evidence/2026-10-28-m1/build-test.txt) |
| 2026-10-28 | M2 / DBG-002 | Run-01 compile test thất bại, `Tag is ambiguous` | Wildcard import JUnit đưa annotation Tag vào cùng tên entity Tag | Đổi toàn bộ import entity/repository/JUnit/persistence/validation sang tên tường minh | Run-02 compile thành công. [Log lỗi](evidence/2026-10-28-m2/run-01/build-test.txt) |
| 2026-10-28 | M2 / DBG-003 | Run-02 có 2 assertion fail: đếm 9 identity thay vì 4; rollback FK nhận ConstraintViolationException | sys.identity_columns chứa cả bảng hệ thống; ID âm bị @Positive chặn trước SQL FK | Join sys.tables với is_ms_shipped=0; dùng Long.MAX_VALUE dương, không tồn tại để đi tới FK | Run-03 có 9 tests pass; [report lỗi](evidence/2026-10-28-m2/run-02/fpt.edu.a2phantuanthanh_se1910.persistence.NewsPersistenceTests.txt), [identity diagnostic](evidence/2026-10-28-m2/run-02/identity-diagnostic.txt) |
| 2026-10-28 | M2 / DBG-004 | Run-03 build/test pass nhưng export SQL snapshot thất bại, lỗi 156 gần RowCount | Alias RowCount trùng keyword SQL Server | Đổi alias TotalRows trong inspect-persistence.sql | Run-04 xuất được schema/data và smoke/restart logs; [lỗi SQL thật](evidence/2026-10-28-m2/run-03/isolated-schema-data.txt) |
| 2026-10-28 | M2 / DBG-005 | Review snapshot run-04 phát hiện NewsContent nullable=1 sau restart dù @Column(nullable=false); 9 tests ban đầu không bắt được | columnDefinition nvarchar(max) nhưng length mặc định 255 khác JDBC COLUMN_SIZE=2147483647; Hibernate update re-ALTER type, SQL Server bỏ NOT NULL khi ALTER không chỉ định nullability | Dùng @Nationalized + length=Integer.MAX_VALUE, giữ @Size(20000); assert toàn schema sau mỗi restart và thử SQL null content. Khôi phục NOT NULL ở DB bài tập bằng ALTER có guard dữ liệu null, 0 dòng trước/sau; giữ DB test lịch sử | **VERIFIED run-05:** 9/9 tests, schema NOT NULL đúng sau context/hai JVM restart; retest cuối ở [inspection M2](evidence/2026-10-28-m2/inspection.md). [Snapshot lỗi](evidence/2026-10-28-m2/run-04/isolated-schema-data.txt), [metadata JDBC](evidence/2026-10-28-m2/run-04/content-jdbc-metadata.txt), [repair script](evidence/2026-10-28-m2/repair-content-nullability.sql), [repair output](evidence/2026-10-28-m2/repair-content-nullability.txt) |

DBG-001 chỉ đóng lỗi major version. M1 vẫn thiếu minh chứng sinh viên chạy IntelliJ. Trạng thái baseline trước sửa xem [baseline inspection](evidence/2026-10-28-baseline/inspection.md).

OBS-001 tiếp tục được quan sát trong M2: timeout apache.snapshots không làm build thất bại; cảnh báo Mockito/CDS và Hibernate metadata unknown vẫn còn. Lỗi SQL 547 do negative test chủ động gửi FK sai là kết quả mong đợi, không phải lỗi startup. Run-03/04 có build pass nhưng không dùng làm nghiệm thu cuối vì DBG-004/005; mọi log cũ được giữ nguyên.

## Cách bổ sung

Thêm hàng khi gặp lỗi mới; nêu bước tái hiện/expected/actual, nguyên nhân đã xác định, fix và retest. Nếu cần log dài, liên kết tới thư mục evidence.

- **OPEN:** còn lỗi hoặc chưa sửa.
- **FIXED:** đã có thay đổi code, chưa kiểm tra lại đủ.
- **VERIFIED:** đã chạy lại và xác nhận hết lỗi.

## DBG-006 — M3, 28-10-2026

Startup/test lỗi khi JPQL gọi `lower(NewsContent)`: Hibernate coi `nvarchar(max)` là NCLOB. Đổi search sang native SQL Server có bind tham số và escape wildcard, giữ nguyên schema. Retest `clean verify`: 20 tests pass; HTTP JAR list/detail/search hoạt động.
