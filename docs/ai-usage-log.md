# Nhật ký sử dụng AI

Mục đích: ghi minh bạch AI đã hỗ trợ gì, đầu ra nào được dùng, ai đã kiểm tra và phần nào sinh viên còn phải tự xác minh. Nhật ký này không thay thế việc sinh viên hiểu, chạy và giải thích code.

## AI-001: Phân tích đề, thiết kế và lập kế hoạch

| Thuộc tính | Ghi nhận thực tế |
|---|---|
| Ngày | 28/10/2026, UTC+07:00 |
| Công cụ | Codex trong phiên làm việc cùng sinh viên |
| Yêu cầu của sinh viên | Đọc PDF; phân tích yêu cầu/quy trình; đáp ứng C2 tiến trình cá nhân và Git; đủ docs theo ảnh; code/tài liệu/commit tương ứng; tuân thủ Spring Boot SBA301 |
| Đầu vào | PDF 5 trang, ảnh cấu trúc docs, tiêu chí C2 được trích trong chat và skeleton ở workspace |
| AI thực hiện | Trích xuất và xem đủ trang PDF; đọc source/POM; kiểm tra Git local và GitHub profile kết nối; chạy test skeleton; lập baseline requirements/design/flows/test plan/Git plan |
| Đầu ra đã ghi | README, sáu file docs theo ảnh, development-plan, evidence baseline và quy ước evidence |
| Phát hiện | POM Boot 4.1.2 khác yêu cầu Boot 3; chưa có Git repo; mâu thuẫn cardinality tag; chưa có chức năng nghiệp vụ/React/SQL datasource |
| Kiểm chứng bởi công cụ | Test Maven thật: 1 pass; đối chiếu source và PDF; profile GitHub có email khớp Git config; kiểm tra liên kết/file docs và mã yêu cầu/test |
| Nguồn kỹ thuật tham khảo | Spring Boot 3.5 docs về cấu trúc package và khởi tạo database; GitHub Docs về author email; liên kết trong design/development-plan |
| Phạm vi chấp nhận hiện tại | Dùng làm baseline phân tích và thiết kế dự kiến; chưa có xác nhận của sinh viên/giảng viên về DEC-01/03 |
| Không có bằng chứng đã làm | Sinh viên tự chạy test/demo; sinh viên tự viết nghiệp vụ; SQL Server integration; frontend; commit/push; nghiệm thu chức năng |
| Code được AI sửa trong phiên | Không sửa Java, POM hoặc cấu hình ứng dụng; chỉ tạo tài liệu/minh chứng |
| Việc sinh viên cần kiểm tra tiếp | Đọc từng REQ/DEC; xác nhận với giảng viên các điểm mơ hồ; tự chạy skeleton; thiết lập repo cá nhân; tự giải thích và thực hiện các mốc tiếp theo |
| Commit | Chưa có Git repository/commit; không ghi SHA giả |

## AI-002: Triển khai nền tảng M1

| Thuộc tính | Ghi nhận thực tế |
|---|---|
| Ngày / công cụ | 28/10/2026, UTC+07:00; Codex và terminal tools |
| Yêu cầu sinh viên | Triển khai M1 từ baseline 6a26a99; Boot 3/JDK 21, Web/JPA/Validation/JDBC, DB đúng tên, config môi trường, kết nối SQL thật, docs/evidence và commit; chưa push/nghiệp vụ |
| Đầu vào đã đọc | AGENTS do chat cung cấp (Postman CLI); không thấy AGENTS.md trên đĩa ở repo/ancestor đã kiểm tra; README, toàn bộ docs/evidence, Java/POM/config, Git thực tế |
| Quyết định có sinh viên trả lời | Sau kiểm tra SQL2022/Windows auth hoạt động và thiếu cấu hình JDBC, AI hỏi phương thức auth. Sinh viên chọn **Windows Authentication hiện tại**; không đoán username/password SQL |
| AI sửa/tạo | POM 3.5.16 và dependencies; application.properties; test context + JDBC assertions; .gitignore, .env.example; scripts/create-database.sql; README, requirements, design, code-flow, development-plan, test-matrix, debug-log, AI log và evidence M1 |
| Thao tác máy/DB | Khảo sát instance/TCP/listener; tạo DB rỗng đúng tên bằng Windows identity; tải native DLL 12.10.2 x64 từ Microsoft GitHub vào tmp bị ignore, kiểm tra chữ ký Valid; không commit DLL/secret |
| Nguồn kiểm tra | Spring Boot 3.5 system requirements/release notes; Microsoft JDBC connection URL/native DLL và release notes 12.10.2 (liên kết ở README/design/evidence) |
| AI/tool đã chạy | `mvnw -version`, dependency tree, `mvnw -B clean verify` (2 pass), `java -jar ... --server.port=0`, HTTP GET / = 404 expected; SQL metadata đối chiếu PID Java, DB đúng tên, TCP/NTLM/encrypt TRUE, 0 bảng nghiệp vụ; review diff/secret/baseline checks trước commit |
| Phạm vi kết quả | DBG-001 VERIFIED do build/runtime Boot 3 thật; nền tảng CLI/SQL pass. Giữ nguyên baseline Boot 4, AI-001 và khảo sát ban đầu; không dùng report cũ làm evidence mới |
| Sinh viên tự thực hiện / chưa có evidence | Đã lựa chọn auth trong chat. **Chưa có bằng chứng sinh viên tự chạy build, IntelliJ, demo hoặc giải thích diff**; TC-SETUP-03 NOT RUN, M1 còn điều kiện này |
| Chưa kiểm chứng | SQL login, TLS với chứng chỉ tin cậy, graceful shutdown, checkout sạch trên máy khác; toàn bộ schema JPA nghiệp vụ/CRUD/auth/React. DEC-01/03 chưa chốt |
| Evidence / commit | [M1 inspection](evidence/2026-10-28-m1/inspection.md), manifest/logs cùng thư mục; tests chạy trên base `6a26a99` + file thay đổi, không giả là checkout sạch. Thuộc **cùng commit này**: `fix: align backend with Spring Boot 3 assignment requirement`; chưa push |

## Nguyên tắc cập nhật

- Mỗi lần AI hỗ trợ code, ghi module/file liên quan, yêu cầu đã hỏi, đề xuất dùng/không dùng và lý do.
- Ghi riêng “AI/tool chạy” và “sinh viên tự chạy”. Không chuyển tự động kết quả của AI thành bằng chứng sinh viên đã hiểu.
- Khi sửa gợi ý của AI, ghi vấn đề, cách sửa và test chứng minh; liên kết debug-log nếu có lỗi thật.
- Không đưa mật khẩu, token, cookie hoặc chuỗi kết nối có secret vào prompt được lưu trong repo.
- Tên commit/author phải phản ánh workflow thật; AI log bổ sung minh bạch về hỗ trợ, không tạo một lịch sử phát triển khác với code.

## Mẫu cho lần sử dụng tiếp theo

```text
AI-ID / thời gian:
Mục tiêu và REQ liên quan:
Prompt hoặc tóm tắt yêu cầu:
AI gợi ý hoặc tạo phần nào:
File và diff đã dùng:
Phần không dùng / đã sửa và lý do:
Kiểm chứng do AI/tool thực hiện:
Kiểm chứng sinh viên tự thực hiện:
Kết quả và evidence:
Điểm chưa hiểu/chưa kiểm chứng:
Commit thực tế:
```
