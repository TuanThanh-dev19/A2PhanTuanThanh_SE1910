# Baseline inspection

Ngày khảo sát: 28/10/2026 (Asia/Bangkok, UTC+07:00).
Người thực hiện các thao tác bên dưới: Codex/tool trong phiên phân tích, chưa có xác nhận sinh viên tự thực hiện.

## 1. Đọc nguồn

- Đã đọc PDF 5 trang, trích xuất text và xem render đầy đủ bằng pypdf/pypdfium2.
- Trang 3 có 5 bảng SystemAccount, Category, NewsArticle, Tag, NewsTag; phần chữ giới hạn tag thuộc tối đa một bài, cần xử lý DEC-01.
- Đã xem ảnh cấu trúc docs do sinh viên gửi trong chat.

## 2. Workspace và công cụ

```text
Workspace: D:/FA2026/SBA301/A2PhanTuanThanh_SE1910
Java: 21.0.8
Node: v24.16.0
POM Spring Boot parent: 4.1.2
Maven wrapper distribution: apache-maven-3.10.0-bin.zip
Application name: A2PhanTuanThanh_SE1910
```

Source có một application class, một file cấu hình và một context test. Chưa có business controller/service/repository/entity; chưa có React project và datasource configuration.

## 3. Git/account

Lệnh `git status --short`, `git log -8 --oneline` và `git remote -v` đều trả thông báo:

```text
fatal: not a git repository (or any of the parent directories): .git
```

Giá trị `git config --get user.name`: `Phan Tuan Thanh`.
Giá trị `git config --get user.email`: `phanthanh19112005@gmail.com`.

GitHub connector profile trả account `TuanThanh-dev19` và cùng email. Đây là kiểm tra profile kết nối; chưa có remote để kiểm tra owner, cũng chưa chứng minh credential của terminal dùng để push là account đó.

## 4. Test thực tế

Lệnh chạy tại workspace:

```powershell
.\mvnw.cmd test
```

Trích phần kết quả từ stdout đã quan sát, không phải toàn bộ log:

```text
Spring Boot :: (v4.1.2)
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 31.235 s
Finished at: 2026-10-28T13:37:48+07:00
```

Exit code: 0. [context-test.txt](context-test.txt) là bản sao file text Surefire sau lần chạy, giữ nguyên nội dung report.

Có cảnh báo timeout metadata Apache snapshots và cảnh báo Mockito/JVM agent; build vẫn thành công. Các quan sát được ghi OBS-001 trong debug-log. Không có phép thử kết nối SQL Server, endpoint HTTP, React hoặc nghiệp vụ trong lần chạy này.

## 5. Giới hạn minh chứng

Không có commit SHA vì thư mục chưa là Git repository. `manifest.json` ghi SHA-256 của PDF và các file code/config đã kiểm tra để cố định baseline; checksum không thay thế Git history.

Docs được tạo sau khảo sát; file Java, POM và application.properties không được sửa trong phiên phân tích. Các bước tiếp theo phải tạo evidence mới khi implementation thay đổi, không dùng lại report Boot 4 này để chứng minh Boot 3 hoặc chức năng hoàn tất.
