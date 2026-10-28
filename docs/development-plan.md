# Tiến trình cá nhân và Git

Ngày lập: 28/10/2026. Đây là kế hoạch triển khai, không phải lịch sử đã thực hiện. Các mốc không có hạn ngày hoặc số commit bắt buộc do đề không cung cấp các tiêu chí đó.

## 1. Baseline đã kiểm tra

| Hạng mục | Kết quả thực tế |
|---|---|
| Thư mục | `D:/FA2026/SBA301/A2PhanTuanThanh_SE1910` |
| Code có sẵn | Main application, application.properties, một context test, Maven wrapper, POM |
| Công nghệ hiện tại | Java 21, Spring Boot 4.1.2; chưa có Web/JPA/SQL Server/Security dependencies |
| Kiểm thử | `./mvnw.cmd test` thành công: 1 test, 0 failures, 0 errors, 0 skipped |
| Git local | `git status` báo không phải Git repository; chưa có `.git`, remote hay commit |
| Git author config đọc từ máy | `Phan Tuan Thanh`, `phanthanh19112005@gmail.com` |
| GitHub kết nối được kiểm tra | `TuanThanh-dev19`, email khớp author config; chưa kiểm tra owner remote vì chưa có remote |
| Docs | Tạo trong phiên phân tích này; code nghiệp vụ vẫn chưa triển khai |

Thông tin account ở trên là kết quả đọc, không chứng minh tài khoản dùng để push trong terminal đã được cấu hình. Sau khi có remote cần kiểm tra owner và commit attribution trên GitHub. GitHub liên kết commit bằng author email; đặt `user.name` giống tên GitHub chưa đủ. [GitHub: Setting your commit email address](https://docs.github.com/en/account-and-profile/how-tos/email-preferences/setting-your-commit-email-address?platform=windows).

## 2. Cách đáp ứng C2 bằng công việc thật

1. Lưu baseline hiện có và phân tích yêu cầu trước khi triển khai nghiệp vụ. Ghi rõ skeleton đã có trước phiên phân tích.
2. Mỗi lần triển khai chọn một phần có thể giải thích và kiểm tra: ví dụ xóa category có ràng buộc FK, hoặc news CRUD cùng tags.
3. Thực hiện → chạy kiểm tra phù hợp → ghi kết quả → xem diff → commit phần hoàn chỉnh. Một mốc có thể cần một hoặc nhiều commit tùy việc thực tế.
4. Lỗi thật được ghi ngay khi gặp, có cách tái hiện, nguyên nhân, cách sửa và test lại. Có cải tiến thật thì ghi lý do và trước/sau.
5. Sinh viên đọc lại diff, tự chạy/demo và giải thích quyết định. Nhật ký AI ghi đúng phần AI hỗ trợ và phần sinh viên đã tự xác minh.
6. Push khi đạt mốc ổn định để bảo toàn tiến trình; không dồn toàn bộ project hoàn chỉnh rồi chia ngược thành lịch sử giả. Không đổi timestamp để tạo cảm giác làm nhiều ngày, không cố ý thêm bug hay commit rỗng để tăng số lượng.

C2 đánh giá tính hợp lý và khả năng truy vết. Một chuỗi commit ít nhưng có nội dung rõ vẫn có giá trị hơn nhiều commit đổi tên hoặc sửa khoảng trắng không giải thích được.

## 3. Lộ trình đề xuất

Tên commit chỉ là ví dụ cho nội dung tương ứng khi thật sự hoàn thành, không phải danh sách lệnh cần chạy ngay.

| Mốc | Công việc/REQ | Điều kiện hoàn thành | Docs và evidence đi cùng | Ví dụ commit |
|---|---|---|---|---|
| M0: baseline | Khảo sát skeleton, phân tích đề, khởi tạo repo đúng account; GIT-01, DOC-01 | Review baseline; xác minh owner/author; lưu snapshot thực tế | requirements, design, plan, AI log, kết quả context test | `chore: establish initial scaffold and assignment baseline` |
| M1: đúng nền tảng | Chuyển Boot 4 → Boot 3, thêm Web/JPA/Validation/SQL Server, cấu hình môi trường; REQ-01,02,03,19 | Build/test Boot 3 pass; mở project trong IntelliJ; có SQL Server kết nối được | debug-log DBG-001, design, README, version/runtime evidence | `fix: align backend with Spring Boot 3 assignment requirement` |
| M2: persistence | Chốt DEC-01/03, đủ 5 entity, repository, FK, seed demo qua JPA; REQ-03,05,17 | JPA tạo bảng trên DB rỗng; đọc/ghi tiếng Việt; ràng buộc đúng và dữ liệu còn sau restart | schema thực tế, design, DB test và ảnh/table metadata | `feat: persist news domain with JPA and SQL Server` |
| M3: auth và public news | Login cả hai role, Spring Security, public active list/detail; REQ-06,07 | Test 401/403, role mapping, active/inactive; không lộ password | code-flow, test matrix, API result | `feat: add role-based access and public active news` |
| M4: accounts trọn luồng | Admin account CRUD/search, khởi tạo React/axios, popup/confirm; REQ-04,08,09,15,16,17 | API/React CRUD/search chạy; delete account có bài bị chặn; Staff không gọi được | account flow, tests, ảnh popup/confirm và response 409 | `feat: deliver admin account management` |
| M5: categories trọn luồng | Staff category CRUD/search, parent, inactive, guard delete; REQ-10,11,15,16,17 | FE/BE chạy; FK/parent cycle, Cancel, search, delete guard được test | category flow, test cases, evidence | `feat: deliver category management with deletion rules` |
| M6: news và tags | Staff news CRUD/search, tags, status; React public news; REQ-06,12,15,16,17 | Tạo/sửa/xóa bài cùng tags nhất quán; author/date do BE đặt; public không thấy inactive | news flow, design DEC-01 cuối cùng, API/UI/DB evidence | `feat: deliver news and tag management` |
| M7: cá nhân | Profile và history theo principal; REQ-13,14 | Hai staff kiểm tra chéo; không sửa người khác/tự đổi role; history chỉ của mình | profile/history flow, ownership tests, ảnh kết quả | `feat: add staff profile and personal news history` |
| M8: tích hợp và bàn giao | Full regression, sửa lỗi gặp thật, usability và docs; REQ-18, GIT-02, DOC-01 | Tất cả bắt buộc pass hoặc ghi rõ chưa đạt; chạy lại từ checkout sạch trên SQL Server | debug-log, test matrix, AI log, README demo, manifest evidence, Git history | `fix: <lỗi thực tế>` / `docs: record verified demo and test results` |

M4-M7 có thể tách BE, FE và test thành các commit tự nhiên nếu mỗi phần có diff rõ và trạng thái được ghi đúng. Không cần đợi hết M8 mới test. Các test xóa, phân quyền và validation phải xuất hiện cùng lúc chức năng được thêm; sửa lỗi được ghi tại mốc phát hiện, không phải chờ “giai đoạn sửa lỗi”.

Hiện M0 mới hoàn thành phần đọc/khảo sát/tài liệu; **chưa hoàn tất phần Git local/remote**. M1-M8 chưa thực hiện.

## 4. Khởi tạo Git ở bước triển khai

Các lệnh dưới đây là hướng dẫn chưa được thực thi trong phiên phân tích này. Chỉ thực hiện khi đã kiểm tra đúng thư mục và tài khoản. Không đưa ảnh render tạm, build output, `.env`, token hoặc mật khẩu vào staging.

```powershell
git init -b main
git config --local user.name "Phan Tuan Thanh"
git config --local user.email "phanthanh19112005@gmail.com"
git config --show-origin --get user.email
git status --short
```

Trước commit đầu, bổ sung ignore cho `tmp/`, `node_modules/`, `dist/`, file môi trường bí mật và log runtime; giữ file cấu hình mẫu không chứa secret và `docs/evidence/` có chủ đích. Kiểm tra `.gitignore` hiện tại vì mới là mẫu Java/IDE.

Stage các file đã review bằng đường dẫn cụ thể. Ví dụ baseline gồm `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn`, `.gitignore`, `.gitattributes`, `src`, `README.md`, `docs`. Sau đó:

```powershell
git diff --cached --stat
git diff --cached
git commit -m "chore: establish initial scaffold and assignment baseline"
git show --no-patch --format=fuller HEAD
```

Tạo/chọn repository cá nhân của `TuanThanh-dev19`, kiểm tra quyền và URL trước khi thêm `origin`. Chưa có URL đã xác minh nên tài liệu không đưa lệnh push tới một repository suy đoán. Sau khi thêm remote thật, kiểm tra `git remote -v`, push `main`, mở commit trên GitHub để kiểm tra author gắn đúng profile. Không ghi token vào URL remote hoặc docs.

Một người làm có thể dùng `main` với commit nhỏ đã kiểm tra. Với thay đổi lớn, dùng nhánh ngắn như `feat/news-management`, merge giữ các commit có giá trị minh chứng. Không cần GitFlow nhiều nhánh. Tránh squash toàn bộ tiến trình thành một commit trước khi nộp, hoặc rewrite các commit đã được dùng làm evidence.

## 5. Quy trình cho mỗi phần việc

| Bước | Việc làm | Dấu vết cần giữ |
|---|---|---|
| Chọn việc | Gắn REQ và test ID; kiểm tra trạng thái hiện tại | TODO/đoạn plan tương ứng |
| Thiết kế | Chốt endpoint, DTO, rule, edge case | design và DEC nếu thay đổi |
| Code | Controller → Service → Repository; FE qua axios | Diff đủ lớp liên quan, không chứa file sinh tự động |
| Kiểm tra | Test service/API; integration SQL Server; React thao tác trực tiếp khi có UI | Kết quả thật, input/expected/actual, môi trường |
| Đồng bộ docs | Cập nhật code-flow và matrix; debug-log/AI log khi áp dụng | Không còn câu mô tả route đã bị đổi hoặc test chưa chạy nhưng ghi PASS |
| Review và commit | Xem diff/staging, đặt message theo tác động | Commit giải thích được thay đổi và lý do |
| Tự trình bày | Sinh viên mở code, demo và giải thích một case biên | Chỉ ghi đã xác minh khi sinh viên thực sự làm |

## 6. Truy vết không tạo vòng lặp commit hash

Trong commit chức năng, ghi REQ/test/evidence và dùng nhãn **“cùng commit này”**; không đoán SHA của commit chưa tạo. Sau commit, lấy SHA thật bằng `git log --oneline -- <path>` hoặc `git show HEAD`. Ở cập nhật docs tiếp theo, thêm SHA nếu cần. Không amend lặp lại chỉ để cố nhét SHA của chính commit đó vào file.

Nếu test chạy trên working tree, ghi `base commit + danh sách file thay đổi` và kết quả `git status`; đừng ghi rằng đã test một SHA sạch. Khi tag bàn giao, chạy lại kiểm tra trên trạng thái code chính xác rồi ghi SHA của code đã kiểm thử trong evidence ở commit tài liệu sau đó. Timestamp test dùng UTC+07:00 thực tế.

Mẫu hàng truy vết sau khi có implementation:

```text
REQ-11 | CategoryServiceImpl.delete + CategoryController + CategoryPage
        | TC-CAT-03 | evidence/<run thực tế>/category-delete.json
        | <SHA thực tế hoặc cùng commit này> | PASS/FAIL theo lần chạy
```

## 7. Checklist trước nộp

- [ ] Remote thuộc repository cá nhân; author email/account đúng; push các commit cần nộp.
- [ ] POM Boot 3; tên BE/FE/database đúng; cấu trúc ba lớp rõ.
- [ ] Có đủ module và thao tác bắt buộc, đặc biệt Search, popup create/update và confirmation delete.
- [ ] DB thật là SQL Server; kiểm tra FK, Unicode, persist qua restart và delete guard với cả bài inactive.
- [ ] Test trực tiếp API khi không có quyền, thay vì chỉ chứng minh FE ẩn menu.
- [ ] Docs mô tả đúng code hiện tại; mọi PASS có run/evidence; lỗi chưa sửa được ghi OPEN.
- [ ] Sinh viên tự chạy từ hướng dẫn, giải thích service rule, JPA mapping, axios và các quyết định DEC.
- [ ] AI log đúng phạm vi sử dụng; không tự nhận đã kiểm thử hoặc tự viết phần chưa thực sự làm.
- [ ] Demo từ checkout sạch; không phụ thuộc dữ liệu/mật khẩu chỉ có trên máy mà thiếu hướng dẫn.
