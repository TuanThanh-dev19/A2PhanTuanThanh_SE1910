Author: Phan Tuan Thanh - CE190595

# Project conventions

- Đây là dự án bài tập SBA301: ưu tiên đúng đề, chạy được, dễ đọc và dễ giải thích. Chọn giải pháp vừa đủ cho bài tập; không mở rộng thành quy trình hoặc hệ thống phức tạp hơn nhu cầu.
- Khi triển khai code, chỉ cập nhật `docs/test-matrix.md` với kết quả kiểm tra ngắn. Không tự viết/cập nhật các docs khác hoặc tạo evidence, checklist, báo cáo.
- Tiến trình và commit theo phần việc thực tế; không chia nhỏ công việc chỉ để đủ milestone/commit. Kiểm tra tập trung vào chức năng và ràng buộc quan trọng, phù hợp mức thay đổi.
- Read `docs/requirements.md`, `docs/design.md` and `docs/development-plan.md` before implementing a milestone.
- Add `Author: Phan Tuan Thanh - CE190595` at the top of every new or edited project-authored source, configuration, SQL and documentation file. Use the file format's comment syntax where needed. Preserve an XML declaration or shebang as the first line when required.
- Keep existing recorded evidence and third-party Maven Wrapper files intact. Do not add new evidence or commit generated test/build output.
- Follow Spring Boot 3 with Controller → Service → Repository, SQL Server, ReactJS and axios as required by Assignment 02.
- Only update `docs/test-matrix.md` alongside implementation. Only mark a test as passed after actually running it.

## Phạm vi mỗi lượt và commit để dễ học

- Mặc định mỗi lượt làm một luồng nhỏ có thể chạy thử và giải thích, không tự triển khai cả milestone trong một lần. Milestone là nhóm công việc, không bắt buộc tương ứng một commit. Nếu người dùng yêu cầu làm trọn mốc, vẫn triển khai và kiểm tra từng bước có ý nghĩa.
- Chia phần việc trước khi viết code, ví dụ API danh sách account, màn hình đọc account, tạo account, sửa account, xóa account. Không làm xong cả mốc rồi chia ngược thành lịch sử giả.
- Mỗi commit tập trung một thay đổi rõ ràng; tránh gộp nhiều chức năng, dữ liệu demo, test harness và viết lại docs vào cùng một diff lớn. Không ép số dòng bằng cách nhồi code hoặc bỏ kiểm tra cần thiết.
- Chỉ viết test cần cho phần đang làm; ưu tiên thao tác chạy thử/Postman phù hợp bài tập. Không tự thêm script kiểm thử tổng quát, log hay manifest để làm tiến trình nặng hơn.
- Cuối mỗi phần, giải thích ngắn file nào thay đổi, input → xử lý → output và cách sinh viên tự thử. Giữ lịch sử hiện có; tiếp tục tiến trình thật từ trạng thái hiện tại.

## Quy ước viết code

- Ưu tiên code dễ đọc, tường minh và dễ giải thích theo luồng Purpose → Input → Trigger → Transform → Output.
- Đặt tên class, method và biến theo chức năng/nghiệp vụ. Mỗi method có một nhiệm vụ rõ ràng; chia bước kiểm tra, xử lý và trả kết quả để đọc được từ trên xuống.
- Ưu tiên `if/else`, vòng lặp và biến trung gian có tên rõ khi chúng dễ hiểu hơn biểu thức gộp. Chỉ dùng Stream/lambda/Optional khi ngắn và rõ; tránh chuỗi xử lý dài, lồng nhiều tầng hoặc toán tử ba ngôi lồng nhau.
- Dùng constructor injection, mapping DTO và thao tác dữ liệu tường minh. Không thêm thư viện sinh code, reflection hoặc cơ chế tự động chỉ để tiết kiệm vài dòng nếu làm khó việc đọc/debug.
- Giữ kiến trúc Controller → Service → Repository. Chỉ tạo helper, abstraction hoặc thành phần dùng chung khi có nhu cầu thực tế; tránh generic CRUD engine, nhiều lớp kế thừa và design pattern không cần thiết.
- Với React, giữ form/UI state ở nơi sử dụng; dùng component và event handler rõ trách nhiệm. Chỉ thêm Context, custom hook hoặc tối ưu hóa khi giải quyết được vấn đề cụ thể.
- Comment giải thích quy tắc nghiệp vụ hoặc lý do lựa chọn chưa hiển nhiên; không mô tả lại từng dòng code. Khi cần cách làm phức tạp hơn, giải thích ngắn trong phản hồi chat, không tự bổ sung tài liệu.
- Sự đơn giản vẫn phải đáp ứng yêu cầu: validation, phân quyền, transaction, ràng buộc dữ liệu và xử lý lỗi không được bỏ qua.

## Quy ước tài liệu

- Chỉ cập nhật `docs/test-matrix.md`: trường hợp kiểm tra, Expected, Actual và trạng thái; viết ngắn và đúng kết quả thực chạy.
- Không tự cập nhật README, requirements, design, code-flow, development-plan, debug-log hoặc AI log khi làm tính năng. Chỉ sửa các file này nếu người dùng yêu cầu trực tiếp.
- Không tạo evidence, ảnh, log lưu riêng, manifest/checksum hay báo cáo bổ sung. Sinh viên tự chụp khi cần; hướng dẫn thao tác ngắn trong chat.
- Giữ nguyên tài liệu/evidence đã có, không xóa hoặc viết lại lịch sử. Tập trung thời gian vào code, kiểm tra vừa đủ và giải thích phần vừa làm.

<!-- postman-cli:installation:start -->
Postman CLI is installed on this device, and can be used for API Engineering work.
<!-- postman-cli:installation:end -->
