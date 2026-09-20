**# Ý TƯỞNG: LUYỆN ĐỌC**

* ****Số màn cần thiết:**** 05 màn: Danh mục bài luyện đọc, Danh sách bài theo danh mục, Màn hình làm bài, Trạng thái đang chấm bài và Màn hình kết quả.

**1. Thông tin chung (Meta Info)**

* ****Dự án:**** En-Learning.

* ****Tính năng:**** Luyện đọc tiếng Anh.

* ****Mục đích:**** Giúp người học rèn kỹ năng đọc hiểu theo nhóm mục tiêu, hoàn thành bài trong thời gian quy định và biết rõ kết quả của từng câu trả lời.

**2. Đối tượng & Trải nghiệm (Target & UX)**

* ****Người dùng chính:**** Sinh viên và người đi làm tự học tiếng Anh, cần luyện đọc theo trình độ, mục tiêu thi hoặc công việc.

* ****Hành động chính:**** Chọn danh mục, tìm bài luyện, đọc nội dung, trả lời câu hỏi, nộp bài và xem kết quả.

* ****Cảm xúc mang lại:**** Tập trung khi làm bài, dễ tìm nội dung phù hợp và hiểu rõ điểm cần cải thiện sau mỗi lần luyện.

**3. Đặc tả Thiết kế (Design Specs)**

* ****Phong cách UI:**** Tối giản, chuyên nghiệp, nền sáng, nhiều khoảng trắng; sử dụng nền trang `#F7F9FF`, card trắng, viền nhẹ và màu xanh `#008FD5` cho CTA chính.

* ****Layout dùng chung:**** Cả 05 màn giữ Header, Sidebar và Footer của En-Learning – Dashboard; menu **Luyện đọc** được làm nổi bật.

* ****Màn 1 – Danh mục bài luyện đọc:****

  * Hiển thị theo chiều dọc 04 danh mục: IELTS, TOEIC, Nền tảng và Công việc.

  * Mỗi danh mục là một card gồm tên, mô tả ngắn, số lượng bài luyện và nút **“Xem thêm”**.

  * Mỗi lần chỉ có một CTA chính rõ ràng, tránh hiển thị quá nhiều thông tin trên card.

* ****Màn 2 – Danh sách bài luyện theo danh mục:****

  * Hiển thị tên danh mục, mô tả ngắn và breadcrumb quay lại trang danh mục.

  * Có thanh tìm kiếm theo tên bài và bộ lọc theo chủ đề, trình độ hoặc thời lượng.

  * Mỗi bài luyện hiển thị tên bài, chủ đề, trình độ, thời gian làm bài, số câu hỏi, trạng thái đã làm và nút **“Bắt đầu làm”**.

  * Có trạng thái Loading, không tìm thấy bài và lỗi tải dữ liệu.

* ****Màn 3 – Làm bài luyện đọc:****

  * Phần đầu hiển thị tên bài, chủ đề, số câu đã trả lời và đồng hồ đếm ngược thời gian.

  * Trên máy tính, bố cục ưu tiên hai cột: bài đọc bên trái; danh sách câu hỏi và ô nhập đáp án bên phải.

  * Trên điện thoại, bài đọc và câu hỏi xếp dọc để dễ theo dõi.

  * Nội dung bài đọc rõ ràng, cỡ chữ dễ đọc; câu hỏi được đánh số và có trạng thái đã/chưa trả lời.

  * Nút chính **“Nộp bài để chấm điểm”** cố định dễ thấy; khi còn câu chưa trả lời, hiển thị xác nhận trước khi nộp.

  * Không hiển thị đáp án hoặc gợi ý đáp án trước khi người dùng nộp bài.
  * Câu hỏi bao gồm cả câu trắc nghiệm và câu điền từ vào chỗ trống

* ****Màn 4 – Đang chấm bài:****

  * Hiển thị trạng thái **“Đang chấm bài”**, biểu tượng tải nhẹ và thông báo bài làm đã được lưu.

  * Người dùng có thể chờ tại trang hoặc rời đi; kết quả được lưu trong lịch sử học tập và thông báo khi hoàn tất.

  * Không hiển thị điểm hoặc đáp án khi quá trình chấm chưa hoàn thành.

* ****Màn 5 – Kết quả bài luyện đọc:****

  * Hiển thị điểm tổng, số câu đúng/tổng số câu, thời gian đã sử dụng và trạng thái hoàn thành.

  * Danh sách từng câu hiển thị đáp án người học, đáp án đúng, trạng thái đúng/sai và giải thích ngắn nếu có.

  * Có CTA **“Làm lại bài này”** hoặc **“Bài luyện tiếp theo”** để người học tiếp tục.

**4. Luồng chính (User Flow)**

* Người dùng vào **Luyện đọc** → chọn danh mục → nhấn **“Xem thêm”**.

* Người dùng tìm kiếm hoặc lọc bài luyện → nhấn **“Bắt đầu làm”**.

* Người dùng đọc bài, trả lời câu hỏi → nhấn **“Nộp bài để chấm điểm”** hoặc hết thời gian.

* Hệ thống lưu bài làm → hiển thị trạng thái **“Đang chấm bài”** → chuyển đến màn hình kết quả.

**5. Dữ liệu cốt lõi (Mock Data)**

* ****Danh mục:**** Tên, mô tả, ảnh/icon nhẹ, số lượng bài luyện.

* ****Bài luyện:**** Tên bài, chủ đề, trình độ, thời lượng, số câu hỏi, trạng thái hoàn thành.

* ****Bài đọc:**** Tiêu đề, nội dung, danh sách câu hỏi, loại đáp án và thời gian giới hạn.

* ****Kết quả:**** Điểm tổng, số câu đúng/sai, câu trả lời của người học, đáp án đúng, giải thích và thời gian làm bài.
