# Nhật ký lỗi và vấn đề thực tế

Không ghi lỗi giả để tạo commit sửa lỗi. Mỗi mục chỉ ghi điều đã quan sát; giả thuyết phải được phân biệt với nguyên nhân đã kiểm chứng. Các rủi ro chưa xảy ra thuộc design/test-matrix, không phải bug đã phát hiện.

## DBG-001: Sai major version Spring Boot so với đề

| Thuộc tính | Ghi nhận |
|---|---|
| Ngày phát hiện | 28/10/2026 |
| Trạng thái | **OPEN** |
| Liên quan | REQ-02, M1, TC-SETUP-01 |
| Cách tái hiện | Mở `pom.xml`, kiểm tra version của spring-boot-starter-parent; chạy `./mvnw.cmd test` |
| Expected | Spring Boot 3 theo PDF tr.1,2,5 |
| Actual | POM và log test dùng Spring Boot 4.1.2; test context vẫn thành công |
| Nguyên nhân đã xác định | Skeleton hiện khai báo parent 4.1.2. Chưa biết vì sao lúc tạo project đã chọn version này |
| Tác động | Build pass nhưng không đáp ứng ràng buộc công nghệ của assignment |
| Cách sửa dự kiến | Chọn version ổn định thuộc 3.x, kiểm tra dependency tương thích, chạy lại build/test và cập nhật docs |
| Đã sửa? | Chưa. Phiên này chỉ phân tích và tạo docs |
| Retest | Chưa chạy với Boot 3 |
| Evidence | [Baseline inspection](evidence/2026-10-28-baseline/inspection.md) |
| Commit | Chưa có Git repository/commit |

## OBS-001: Cảnh báo mạng và test tooling khi chạy baseline

- **Thời điểm:** lần chạy kết thúc 28/10/2026 13:37:48 +07:00.
- **Quan sát:** Maven cảnh báo timeout metadata từ `repository.apache.org/snapshots`; sau đó tiếp tục và kết thúc BUILD SUCCESS. Mockito cảnh báo tự attach Java agent; JVM cũng có cảnh báo dynamic agent/CDS.
- **Ảnh hưởng quan sát:** không làm thất bại lần chạy này; 1 test pass, 0 skipped. Chưa có bằng chứng đây là lỗi ứng dụng.
- **Trạng thái:** ghi nhận, chưa thay cấu hình để xử lý. Chỉ điều tra thêm nếu cảnh báo gây lỗi/chậm lặp lại hoặc ảnh hưởng chạy lại trong môi trường bàn giao.
- **Không kết luận:** không ghi “đã fix dependency/network” khi chưa sửa và chưa xác minh.

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
