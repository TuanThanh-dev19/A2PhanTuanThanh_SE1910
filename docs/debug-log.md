# Nhật ký lỗi và vấn đề thực tế

Không ghi lỗi giả để tạo commit sửa lỗi. Mỗi mục chỉ ghi điều đã quan sát; giả thuyết phải được phân biệt với nguyên nhân đã kiểm chứng. Các rủi ro chưa xảy ra thuộc design/test-matrix, không phải bug đã phát hiện.

## DBG-001: Sai major version Spring Boot so với đề

| Thuộc tính | Ghi nhận |
|---|---|
| Ngày phát hiện | 28/10/2026 |
| Trạng thái | **VERIFIED** ngày 28/10/2026 bằng AI/tool, phạm vi sai major version |
| Liên quan | REQ-02, M1, TC-SETUP-01 |
| Cách tái hiện | Mở `pom.xml`, kiểm tra version của spring-boot-starter-parent; chạy `./mvnw.cmd test` |
| Expected | Spring Boot 3 theo PDF tr.1,2,5 |
| Actual lúc phát hiện | POM và log test dùng Spring Boot 4.1.2; test context vẫn thành công |
| Nguyên nhân đã xác định | Skeleton lúc phát hiện khai báo parent 4.1.2. Chưa biết vì sao lúc tạo project đã chọn version này |
| Tác động | Build pass nhưng không đáp ứng ràng buộc công nghệ của assignment |
| Cách sửa đã thực hiện | Parent 3.5.16, Java 21; Web/JPA/Validation/JDBC do Boot quản lý; SQL datasource qua biến môi trường, DB đúng tên |
| Đã sửa? | Có; không thêm nghiệp vụ. Baseline Boot 4 được giữ nguyên để đối chiếu |
| Retest | `mvnw -B clean verify` kết thúc 14:09:48 +07:00, exit 0; Boot 3.5.16, 2 tests, 0 failures/errors/skipped. JAR chạy Tomcat và session JDBC thật tới SQL Server được xác nhận tiếp lúc 14:10 +07:00 |
| Evidence | [Baseline inspection](evidence/2026-10-28-baseline/inspection.md), [M1 inspection](evidence/2026-10-28-m1/inspection.md), [build](evidence/2026-10-28-m1/build-test.txt), [runtime](evidence/2026-10-28-m1/application-start.txt) |
| Commit | Base `6a26a99` + working tree khi test; sửa lỗi và evidence thuộc **cùng commit này** (`fix: align backend with Spring Boot 3 assignment requirement`) |

VERIFIED ở đây chỉ đóng lỗi version; không đồng nghĩa M1 đã đủ điều kiện IntelliJ. TC-SETUP-03 còn NOT RUN, sinh viên chưa cung cấp bằng chứng tự chạy IDE.

## OBS-001: Cảnh báo mạng và test tooling khi chạy baseline

- **Thời điểm:** lần chạy kết thúc 28/10/2026 13:37:48 +07:00.
- **Quan sát:** Maven cảnh báo timeout metadata từ `repository.apache.org/snapshots`; sau đó tiếp tục và kết thúc BUILD SUCCESS. Mockito cảnh báo tự attach Java agent; JVM cũng có cảnh báo dynamic agent/CDS.
- **Ảnh hưởng quan sát:** không làm thất bại lần chạy này; 1 test pass, 0 skipped. Chưa có bằng chứng đây là lỗi ứng dụng.
- **Trạng thái:** ghi nhận, chưa thay cấu hình để xử lý. Chỉ điều tra thêm nếu cảnh báo gây lỗi/chậm lặp lại hoặc ảnh hưởng chạy lại trong môi trường bàn giao.
- **Không kết luận:** không ghi “đã fix dependency/network” khi chưa sửa và chưa xác minh.

Cập nhật M1: timeout metadata `apache.snapshots` lặp lại trong dependency tree/clean verify; Mockito dynamic agent/CDS vẫn cảnh báo. Lần build Boot 3 thành công; chưa sửa cấu hình mạng/test tooling. Xem [build-test.txt](evidence/2026-10-28-m1/build-test.txt). Hibernate log có một số trường pool/driver `undefined/unknown`; không dùng dòng này để kết luận mất kết nối: test JDBC có assertion và SQL metadata xác nhận session thật.

## Mẫu thêm một lỗi thực sự phát sinh

```text
ID:
Thời điểm và người/công cụ phát hiện:
REQ / test ID / code revision:
Môi trường và dữ liệu đầu vào:
Các bước tái hiện:
Expected:
Actual và evidence:
Nguyên nhân (đã xác minh hay giả thuyết):
Thay đổi sửa lỗi:
Kiểm tra hồi quy và kết quả:
Commit chứa sửa lỗi:
Trạng thái: OPEN / FIXED / VERIFIED
```

`FIXED` nghĩa là đã có thay đổi code, `VERIFIED` nghĩa là đã chạy lại kiểm tra phù hợp và xác nhận hết lỗi. Không đóng bug chỉ dựa vào việc AI nói rằng code đã đúng.
