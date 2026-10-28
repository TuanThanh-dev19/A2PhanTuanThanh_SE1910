Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Debug Log

Tài liệu ghi lỗi thật, nguyên nhân, cách sửa và kết quả kiểm tra lại. Không tạo lỗi để tăng số commit.

| Ngày | Milestone / ID | Hiện tượng | Nguyên nhân | Cách sửa | Kiểm chứng |
|---|---|---|---|---|---|
| 2026-10-28 | M1 / DBG-001 | POM/test chạy Boot 4.1.2 trong khi đề yêu cầu Boot 3 | Parent version trong skeleton là 4.1.2; chưa rõ lý do chọn lúc tạo | Đổi Boot 3.5.16/JDK 21, thêm Web/JPA/Validation/JDBC và cấu hình datasource | **VERIFIED** bởi AI/tool: `clean verify` lúc 14:09:48 +07:00 có 2 tests pass; JAR chạy Boot 3 và kết nối SQL thật. Commit `db2e8e6`; [evidence M1](evidence/2026-10-28-m1/inspection.md) |
| 2026-10-28 | Baseline, M1 / OBS-001 | Timeout metadata apache.snapshots, cảnh báo Mockito agent/JVM CDS; một số metadata Hibernate unknown | Timeout được quan sát; chưa điều tra nguyên nhân mạng/tooling đầy đủ | Chưa sửa; tiếp tục theo dõi nếu ảnh hưởng chạy/test | Hai build vẫn thành công; JDBC assertions và SQL session xác nhận kết nối. [Build M1](evidence/2026-10-28-m1/build-test.txt) |

DBG-001 chỉ đóng lỗi major version. M1 vẫn thiếu minh chứng sinh viên chạy IntelliJ. Trạng thái baseline trước sửa xem [baseline inspection](evidence/2026-10-28-baseline/inspection.md).

## Cách bổ sung

Thêm hàng khi gặp lỗi mới; nêu bước tái hiện/expected/actual, nguyên nhân đã xác định, fix và retest. Nếu cần log dài, liên kết tới thư mục evidence.

- **OPEN:** còn lỗi hoặc chưa sửa.
- **FIXED:** đã có thay đổi code, chưa kiểm tra lại đủ.
- **VERIFIED:** đã chạy lại và xác nhận hết lỗi.
