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
