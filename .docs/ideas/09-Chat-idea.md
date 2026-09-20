**# Ý TƯỞNG: CHAT VỚI TRỢ LÝ AI**

* ****1. Thông tin chung (Meta Info):****

  * ****Dự án:**** En-Learning.

  * ****Tính năng:**** Chat với Trợ lý AI.

  * ****Mục đích:**** Cho phép người học đặt câu hỏi trong quá trình học tiếng Anh và nhận hỗ trợ nhanh, phù hợp với nội dung trao đổi và kết quả học tập hiện có.

* ****2. Đối tượng & Trải nghiệm (Target & UX):****

  * ****Người dùng chính:**** Sinh viên và người đi làm đang tự học tiếng Anh trên hệ thống.

  * ****Nhu cầu chính:**** Hỏi nhanh về từ vựng, ngữ pháp, cách diễn đạt, cách làm bài hoặc tìm bài luyện phù hợp.

  * ****Cảm xúc mang lại:**** Thuận tiện, được hỗ trợ đúng lúc và không phải rời khỏi bài học đang làm.

* ****3. Số màn hình cần thiết:****

  * ****Tổng số màn chính: 2 màn.****

  * ****Màn 1 – Chat AI trạng thái chào mừng:**** Hiển thị khi người dùng mở Chat lần đầu hoặc chưa có nội dung hội thoại.

  * ****Màn 2 – Chat AI đang hội thoại:**** Hiển thị lịch sử tin nhắn, phản hồi AI và các gợi ý học tập có đường link điều hướng.

  * Các trạng thái AI đang trả lời, mất kết nối, không có dữ liệu học tập hoặc trả lời lỗi là **trạng thái của màn Chat**, không cần tách thành màn hình riêng.

* ****4. Luồng sử dụng chính:****

  * Người dùng chọn mục **Chat với AI** trên Sidebar.

  * Hệ thống mở một cửa sổ chat nhỏ nổi trên giao diện hiện tại; không điều hướng sang trang mới để người dùng vẫn giữ được ngữ cảnh học tập.

  * Người dùng nhập câu hỏi và chọn nút Gửi.

  * AI phân tích nội dung câu hỏi, kết hợp với dữ liệu học tập phù hợp của người dùng để trả lời.

  * AI hiển thị câu trả lời kèm các gợi ý hành động dưới dạng đường link trong khung chat.

  * Người dùng chọn một đường link để chuyển đến bài luyện, Flashcard, bài viết, bài nghe, bài kiểm tra hoặc trang thống kê liên quan.

* ****5. Màn 1 – Chat AI trạng thái chào mừng:****

  * Cửa sổ chat nhỏ được mở từ mục **Chat với AI** trên Sidebar; trên desktop hiển thị dạng hộp nổi ở góc phải dưới, trên điện thoại hiển thị dạng Bottom Sheet hoặc toàn màn hình.

  * Phần đầu cửa sổ gồm avatar AI, tên **Trợ lý học tiếng Anh**, trạng thái trực tuyến và nút thu nhỏ/đóng.

  * Khu vực nội dung hiển thị lời chào ngắn, ví dụ: “Chào bạn, hôm nay bạn cần hỗ trợ phần tiếng Anh nào?”

  * Hiển thị các câu hỏi gợi ý để người dùng chọn nhanh như: “Giải thích ngữ pháp”, “Hỏi nghĩa từ vựng”, “Gợi ý bài luyện cho tôi” và “Tôi nên cải thiện gì?”

  * Nếu có dữ liệu học tập, hiển thị một gợi ý cá nhân hóa ngắn, ví dụ: “Bạn đang cần ôn lại từ vựng của chủ đề Công việc.”

  * Phía dưới luôn có ô nhập tin nhắn, nút gửi và gợi ý có thể nhấn Enter để gửi.

* ****6. Màn 2 – Chat AI đang hội thoại:****

  * Tin nhắn của người dùng và AI được phân biệt rõ bằng vị trí, màu nền và avatar.

  * AI có thể trả lời các nội dung: giải thích nghĩa từ vựng, ngữ pháp, ví dụ câu tiếng Anh, hướng dẫn làm bài và góp ý cách học.

  * Khi cần, AI hiển thị các thẻ gợi ý ngay dưới câu trả lời. Mỗi thẻ gồm tiêu đề, lý do gợi ý ngắn và đường link hành động.

  * Ví dụ đường link: **Ôn Flashcard chủ đề Công việc**, **Luyện viết câu điều kiện**, **Làm bài nghe trình độ cơ bản**, **Xem thống kê học tập**.

  * Khi AI đang xử lý, hiển thị trạng thái “Trợ lý AI đang trả lời…” để người dùng biết hệ thống vẫn hoạt động.

  * Người dùng có thể cuộn để xem lịch sử hội thoại, tiếp tục đặt câu hỏi mới hoặc thu nhỏ cửa sổ chat mà không mất nội dung đang trao đổi.

* ****7. Gợi ý học tập từ AI:****

  * Gợi ý được tạo dựa trên nội dung câu hỏi, bài học đang làm, kết quả luyện tập và các kỹ năng người dùng còn yếu.

  * Mỗi gợi ý cần nêu ngắn gọn lý do, ví dụ: “Bạn đang sai nhiều câu về thì quá khứ đơn, hãy luyện bài này trước.”

  * Đường link phải điều hướng trực tiếp đến đúng nội dung trong hệ thống, không chỉ hiển thị lời khuyên chung chung.

  * Nếu người dùng chưa có đủ dữ liệu học tập, AI vẫn trả lời câu hỏi bình thường và đề xuất bài luyện cơ bản thay vì khẳng định đó là gợi ý cá nhân hóa.

* ****8. Trạng thái cần xử lý:****

  * **Đang tải:** Hiển thị chỉ báo AI đang tạo phản hồi.

  * **Mất kết nối hoặc gửi thất bại:** Giữ lại nội dung người dùng đã nhập, thông báo ngắn gọn và có nút Gửi lại.

  * **AI không thể trả lời:** Thông báo AI chưa thể hỗ trợ chính xác, đề xuất người dùng diễn đạt lại câu hỏi hoặc chọn một câu hỏi gợi ý.

  * **Màn hình nhỏ:** Cửa sổ chat mở rộng gần toàn màn hình để bảo đảm dễ đọc và dễ nhập nội dung.

* ****9. Lưu ý thiết kế:****

  * Chat AI là công cụ hỗ trợ học tập, không thay thế hoàn toàn việc chấm điểm hoặc phản hồi chính thức của các chức năng Luyện viết và Bài kiểm tra.

  * Cửa sổ chat dùng chung Header, Sidebar và phong cách của Master Layout; chỉ phần nội dung chat hiển thị dưới dạng cửa sổ nổi.

  * Nên đổi tên mục Sidebar từ **Chat** thành **Chat với AI** hoặc **Trợ lý AI** để tránh nhầm với chức năng chat giữa người dùng với nhau.

> ****Lưu ý về đặc tả hiện có:**** Tài liệu use case hiện tại đang mô tả UC7 là “Chat với bạn bè” qua tin nhắn 1–1, trong khi yêu cầu này là chat với AI. Đây là hai chức năng khác nhau; cần đổi UC7 thành Chat với Trợ lý AI hoặc tách thành use case riêng để tránh sai phạm vi khi thiết kế và triển khai.
