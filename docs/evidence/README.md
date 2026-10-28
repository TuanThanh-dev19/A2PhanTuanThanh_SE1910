Author: Phan Tuan Thanh - CE190595

# FUNewsManagementSystem - Evidence Index

Thư mục lưu log/report và ảnh thực tế cho Assignment 02. Bổ sung screenshot khi đã thao tác đúng test case; file đề xuất chưa phải evidence đã có.

## Quy tắc đặt tên và ghi nhận

- Log theo thư mục ngày/milestone; ảnh dùng PNG, video MP4/WebM nếu được yêu cầu.
- Mỗi lần chạy ghi ngày/môi trường, người chạy, code revision (hoặc base + working tree), test ID, expected/actual và kết quả.
- Ảnh có ngữ cảnh trang, dữ liệu và kết quả; có thể chứng minh nhiều case nếu quan sát rõ.
- Không để lộ password, token/cookie; giữ nguyên artifact cũ khi có lần chạy mới.

## E01 - Baseline và nền tảng backend

| Evidence | Test ID | Vị trí | Trạng thái |
|---|---|---|---|
| Baseline Boot 4, một context test | TC-BASE-01 | [inspection](2026-10-28-baseline/inspection.md), [report](2026-10-28-baseline/context-test.txt), [manifest](2026-10-28-baseline/manifest.json) | Có, AI/tool chạy |
| Boot 3 build/test, hai tests pass | TC-SETUP-01 | [M1 inspection](2026-10-28-m1/inspection.md), [build](2026-10-28-m1/build-test.txt), [manifest](2026-10-28-m1/manifest.json) | Có, AI/tool chạy |
| SQL Server session và tên DB | TC-SETUP-02-BE-DB, TC-M1-DB, TC-M1-SCHEMA | [SQL session](2026-10-28-m1/sqlserver-application-session.txt) | Có, AI/tool chạy |
| JAR/Tomcat/HTTP startup | TC-M1-HTTP | [runtime smoke](2026-10-28-m1/runtime-smoke.txt) | Có, AI/tool chạy |
| `e01-intellij-boot3.png` (đề xuất) | TC-SETUP-03 | Ảnh IDE/main/log Boot 3 và kết nối SQL | Chưa bổ sung |

M1 chưa có bảng nghiệp vụ. Chi tiết lệnh, môi trường và phạm vi ở inspection; log trên không thay thế minh chứng sinh viên chạy IntelliJ.

## E02 - Login, quyền và public news

| File đề xuất | Test ID | Nội dung cần chụp | Trạng thái |
|---|---|---|---|
| `e02-login-validation.png` | TC-AUTH-02 | Form lỗi khi rỗng/sai credential, không lộ secret | Chưa bổ sung |
| `e02-login-role.png` | TC-AUTH-01, TC-AUTH-04 | Login hai role, menu/quyền và response 403 đúng | Chưa bổ sung |
| `e02-public-active.png` | TC-PUB-01, TC-PUB-02 | Khách chỉ xem được active, URL inactive bị từ chối | Chưa bổ sung |

## E03 - CRUD và Search

| File đề xuất | Test ID | Nội dung cần chụp | Trạng thái |
|---|---|---|---|
| `e03-account-crud-search.png` | TC-ACC-01, TC-ACC-02 | Account Create/Update popup, list và Search | Chưa bổ sung |
| `e03-account-delete.png` | TC-ACC-03, TC-ACC-04, TC-UI-02 | Confirmation/Cancel, xóa hợp lệ và case có bài bị chặn | Chưa bổ sung |
| `e03-category-crud-search.png` | TC-CAT-01, TC-CAT-02 | Category popup, list và Search | Chưa bổ sung |
| `e03-category-delete.png` | TC-CAT-03, TC-CAT-05, TC-UI-02 | Confirmation và rule chặn xóa category có bài | Chưa bổ sung |
| `e03-news-tags.png` | TC-NEWS-01, TC-NEWS-02 | Popup bài có category/status/tags và kết quả lưu | Chưa bổ sung |
| `e03-news-delete-search.png` | TC-NEWS-04, TC-NEWS-05, TC-UI-02 | Search, confirmation và bài/links sau xóa | Chưa bổ sung |
| `e03-profile-history.png` | TC-SELF-01, TC-SELF-03 | Hồ sơ và history riêng của hai Staff | Chưa bổ sung |

## Evidence tài liệu và kiểm tra trước nộp

| Evidence | Vị trí | Trạng thái |
|---|---|---|
| E04 - Test matrix | [test-matrix](../test-matrix.md) | Có; mới có kết quả baseline/M1 |
| E05 - Debug log | [debug-log](../debug-log.md) | Có lỗi version đã sửa và quan sát warning |
| E06 - Thiết kế và code-flow | [design](../design.md), [code-flow](../code-flow.md) | Có; nghiệp vụ còn dự kiến |
| E07 - AI usage log | [ai-usage-log](../ai-usage-log.md) | Có |
| E08 - Git/tiến độ | [development-plan](../development-plan.md) | Có baseline/M1; còn kiểm tra attribution/toàn tiến trình |
| E09 - Hướng dẫn chạy | [README](../../README.md) | Có backend; frontend chưa triển khai |

- [ ] Bổ sung IntelliJ, login và ảnh từng module khi đã kiểm tra.
- [ ] Có cả normal và negative case, popup/confirmation và request axios.
- [ ] Tên file trong index khớp file thật; Actual/Pass chỉ ghi khi đã chạy.
- [ ] Kiểm tra đúng repo/account, test lại từ checkout sạch và loại secret khỏi ảnh/log.
