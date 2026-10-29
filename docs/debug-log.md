Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Debug Log

Chỉ ghi lỗi thực tế gặp khi build/chạy hoặc chức năng sai. Mỗi lỗi ghi ngắn hiện tượng, cách sửa và kiểm tra lại; không thêm mục chỉ vì vừa hoàn thành một milestone.

| ID / ngày | Lỗi đã gặp | Cách sửa | Kết quả kiểm tra lại |
|---|---|---|---|
| DBG-001 / 28-10-2026 | Project chạy Boot 4, sai yêu cầu Boot 3 | Đổi parent/dependency sang Boot 3.5.16 | Build và chạy backend thành công |
| DBG-002 / 28-10-2026 | Test không compile vì tên Tag bị trùng | Thay wildcard import bằng import tường minh | Test compile được |
| DBG-003 / 28-10-2026 | Test đếm thừa cột identity và chưa chạm tới FK cần kiểm tra | Chỉ đếm bảng người dùng; dùng ID dương không tồn tại | Các kiểm tra tương ứng đạt |
| DBG-004 / 28-10-2026 | Script kiểm tra SQL lỗi cú pháp ở alias RowCount | Đổi alias thành TotalRows | Script chạy được |
| DBG-005 / 28-10-2026 | Cột NewsContent mất NOT NULL sau Hibernate update | Sửa mapping độ dài Unicode và khôi phục NOT NULL | Cột vẫn bắt buộc sau restart; 9 tests đạt |

Các lỗi trên đã được kiểm tra lại bởi AI/tool. Log gốc giữ tại [M1](evidence/2026-10-28-m1/inspection.md) và [M2](evidence/2026-10-28-m2/inspection.md) để tra cứu khi cần. Không ghi cảnh báo không ảnh hưởng hoặc lỗi chủ động tạo trong negative test như bug của ứng dụng.

## DBG-006 — M3, 28-10-2026

Startup/test lỗi khi JPQL gọi `lower(NewsContent)`: Hibernate coi `nvarchar(max)` là NCLOB. Đổi search sang native SQL Server có bind tham số và escape wildcard, giữ nguyên schema. Retest `clean verify`: 20 tests pass; HTTP JAR list/detail/search hoạt động.
