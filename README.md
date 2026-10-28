# A2PhanTuanThanh_SE1910

Phân tích Assignment 02 SBA301: FUNewsManagementSystem dùng Spring Boot 3, SQL Server và ReactJS/axios.

## Trạng thái ngày 28/10/2026

- Đã đọc và đối chiếu đủ 5 trang đề, sơ đồ database, ảnh cấu trúc `docs` và tiêu chí C2 do sinh viên cung cấp.
- Code hiện có là skeleton Spring Initializr: application, cấu hình tên ứng dụng và một test `contextLoads`.
- `pom.xml` hiện dùng **Spring Boot 4.1.2**, chưa đáp ứng yêu cầu **Spring Boot 3** của đề. Chưa thay đổi code/dependency trong giai đoạn phân tích này.
- Đã chạy `./mvnw.cmd test`: 1 test thành công, 0 thất bại, 0 lỗi. Kết quả này chỉ xác nhận skeleton khởi tạo được; chưa kiểm tra chức năng, SQL Server hay FE/BE.
- Chưa có Git repository local, remote, commit hoặc frontend trong thư mục dự án tại thời điểm khảo sát.
- Bộ tài liệu dưới đây là baseline phân tích/thiết kế. Các mục dự kiến không được xem là chức năng đã hoàn thành.

## Đọc tài liệu theo thứ tự

1. [Yêu cầu và phạm vi](docs/requirements.md): yêu cầu có mã, nguồn, tiêu chí nghiệm thu và điểm cần làm rõ.
2. [Thiết kế](docs/design.md): kiến trúc ba lớp, database, API, phân quyền và validation.
3. [Quy trình cá nhân và Git](docs/development-plan.md): mốc triển khai, điều kiện hoàn thành, commit và tài liệu đi kèm.
4. [Luồng code](docs/code-flow.md): phân biệt code hiện có với luồng dự kiến.
5. [Ma trận kiểm thử](docs/test-matrix.md): test theo yêu cầu, kết quả thực tế và việc chưa chạy.
6. [Nhật ký lỗi](docs/debug-log.md) và [nhật ký dùng AI](docs/ai-usage-log.md).
7. [Quy ước minh chứng](docs/evidence/README.md).

## Kiểm tra skeleton hiện tại trên Windows

Yêu cầu: JDK 21 và kết nối tải dependency Maven khi cần.

```powershell
.\mvnw.cmd test
```

Hướng dẫn chạy SQL Server, backend và frontend sẽ được bổ sung cùng các mốc triển khai đã kiểm thử. Hiện chưa có API nghiệp vụ để demo.

## Nguyên tắc theo dõi

Mỗi thay đổi chức năng phải nối được chuỗi `REQ → thiết kế → code → test → evidence → commit`. Chỉ ghi PASS khi thực sự chạy; chỉ ghi FIXED khi đã sửa và kiểm tra lại. C2 được chứng minh bằng sự phát triển có thể giải thích, không bằng chỉ tiêu số lượng commit.
