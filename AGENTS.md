Author: Phan Tuan Thanh - CE190595

# Project conventions

- Read `docs/requirements.md`, `docs/design.md` and `docs/development-plan.md` before implementing a milestone.
- Add `Author: Phan Tuan Thanh - CE190595` at the top of every new or edited project-authored source, configuration, SQL and documentation file. Use the file format's comment syntax where needed. Preserve an XML declaration or shebang as the first line when required.
- Keep generated output, recorded evidence and third-party Maven Wrapper files intact; document their provenance through `docs/evidence/README.md`.
- Follow Spring Boot 3 with Controller → Service → Repository, SQL Server, ReactJS and axios as required by Assignment 02.
- Update the relevant docs and evidence with the implementation. Only mark a test as passed after actually running it.

## Quy ước viết code

- Ưu tiên code dễ đọc, tường minh và dễ giải thích theo luồng Purpose → Input → Trigger → Transform → Output.
- Đặt tên class, method và biến theo chức năng/nghiệp vụ. Mỗi method có một nhiệm vụ rõ ràng; chia bước kiểm tra, xử lý và trả kết quả để đọc được từ trên xuống.
- Ưu tiên `if/else`, vòng lặp và biến trung gian có tên rõ khi chúng dễ hiểu hơn biểu thức gộp. Chỉ dùng Stream/lambda/Optional khi ngắn và rõ; tránh chuỗi xử lý dài, lồng nhiều tầng hoặc toán tử ba ngôi lồng nhau.
- Dùng constructor injection, mapping DTO và thao tác dữ liệu tường minh. Không thêm thư viện sinh code, reflection hoặc cơ chế tự động chỉ để tiết kiệm vài dòng nếu làm khó việc đọc/debug.
- Giữ kiến trúc Controller → Service → Repository. Chỉ tạo helper, abstraction hoặc thành phần dùng chung khi có nhu cầu thực tế; tránh generic CRUD engine, nhiều lớp kế thừa và design pattern không cần thiết.
- Với React, giữ form/UI state ở nơi sử dụng; dùng component và event handler rõ trách nhiệm. Chỉ thêm Context, custom hook hoặc tối ưu hóa khi giải quyết được vấn đề cụ thể.
- Comment giải thích quy tắc nghiệp vụ hoặc lý do lựa chọn chưa hiển nhiên; không mô tả lại từng dòng code. Khi cần cách làm phức tạp hơn, ghi ngắn gọn lý do và cách kiểm chứng trong docs.
- Sự đơn giản vẫn phải đáp ứng yêu cầu: validation, phân quyền, transaction, ràng buộc dữ liệu và xử lý lỗi không được bỏ qua.

<!-- postman-cli:installation:start -->
Postman CLI is installed on this device, and can be used for API Engineering work.
<!-- postman-cli:installation:end -->
