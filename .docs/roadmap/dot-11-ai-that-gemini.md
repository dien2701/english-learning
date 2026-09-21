# Đợt 11: AI thật bằng Gemini (Viết, Chat, Nói) + đánh giá phát âm từng câu

Phụ thuộc: đợt 4 (khung AI, bản giả). Thay mục 4.4 cũ (OpenAI) bằng Gemini 2.5 Flash cho cả chấm và chat.
Quyết định (21/09/2026): chấm Viết/Nói và Chat dùng Gemini; OpenAI chỉ dùng cho TTS bài nghe (đợt 12). Không lưu bản thu âm.

## Khung chung (11a)
- `GEMINI_API_KEY` trong `.env`/`.env.example`, `app.ai.gemini-api-key`, `app.ai.gemini-model` (mặc định `gemini-2.5-flash`), timeout gọi HTTP.
- Điều kiện `GeminiKeyPresent` / `GeminiKeyMissing` thay cho cặp `OpenAiKey*` ở các cổng Viết/Nói/Chat. Có key: bean `Gemini*`; thiếu key: bản giả như hiện nay. Test tự động luôn dùng bản giả (không đặt key trong test).
- `GeminiClient` dùng `RestClient` gọi REST `generateContent`, `responseMimeType: application/json` + `responseSchema` để ra JSON đúng khuôn; đọc `usageMetadata` lấy số token. Không thêm SDK nặng.
- Lỗi mạng / 429 / 5xx / JSON sai khuôn: ném `AiGradingException` hoặc `AiUnavailableException` (đã có). Log không in key hay nội dung bài.
- Prompt hệ thống để trong `resources/ai/*.txt`, trả lời tiếng Việt cho phần nhận xét.

## 11.1 Luyện viết thật (11a)
- [x] `GeminiWritingGrader`: prompt chấm theo thang 10 (tổng, ngữ pháp, từ vựng, diễn đạt), `summary`, `issues[]` (đoạn gốc, sửa, giải thích). Kẹp điểm về 0-10, làm tròn 0.5.
- [x] Luồng `GRADING` → `GRADED`/`NEEDS_RETRY` giữ nguyên; `modelName` = tên model Gemini.

## 11.2 Chat AI thật + giới hạn 30 tin/ngày (11b, 11c)
- [x] `GeminiChatAssistant`: gửi 10 tin gần nhất làm ngữ cảnh; system prompt chỉ trả lời chủ đề học tiếng Anh (từ vựng, ngữ pháp, phát âm, viết, cách học), từ chối lịch sự câu ngoài phạm vi (`refusal=true`), trả lời theo ngôn ngữ người hỏi (VI/EN). JSON trả về: `content`, `suggestedSkills[]` (enum `StudySkill`), `refusal`. Link cụ thể vẫn do `ChatLinkResolver` chọn từ DB.
- [x] Lưu `promptTokens`, `completionTokens`, `modelName` (cột đã có).
- [x] Hạn mức: tối đa `app.chat.daily-limit` (mặc định 30) tin/ngày/người, ngày tính theo `Asia/Ho_Chi_Minh`. Đếm trong DB = số tin trợ lý trả lời THÀNH CÔNG hôm nay (kể cả `refusal`), nên lượt AI lỗi không bị tính. Kiểm TRƯỚC khi lưu tin người học và gọi AI; hết lượt trả 429 `CHAT_DAILY_LIMIT` (`messageKey` `errors.chatDailyLimit`), không gọi AI.
- [x] `GET /chat/quota` trả `{limit, used, remaining, resetAt}`; `POST .../messages` thành công kèm `remaining` trong phản hồi.
- [x] AI lỗi: 503 `AI_UNAVAILABLE` như cũ, không trừ lượt. FE (11c): hiện thông báo lỗi tạm thời + nút "Thử lại" gửi lại đúng tin đó.
- [x] FE (11c): hiện "Còn X/30 lượt hôm nay"; hết lượt thì khoá ô nhập, hiện "Bạn đã hết lượt nhắn tin hôm nay, quay lại vào ngày mai". Chuỗi VI/EN.

## 11.3 Luyện nói: đánh giá phát âm ngay sau mỗi câu (11d, 11e)
Hiện tại: thu hết các câu rồi mới nộp một lần. Mới: mỗi câu thu xong được chấm ngay; nộp cuối chỉ tổng hợp.
- [x] Migration `V2` (hoặc số kế tiếp): cho phép `speaking_attempts.status` thêm `IN_PROGRESS`; bảng `speaking_prompt_results` (attempt_id, prompt_id, transcript, score, `word_issues` JSON, `tips` JSON, model_name, created_at, unique(attempt_id, prompt_id)). Sửa entity cùng lúc.
- [x] `POST /speaking/lessons/:id/attempts` tạo lượt `IN_PROGRESS` (dùng lại lượt `IN_PROGRESS` của chính người đó nếu có).
- [x] `POST /speaking/attempts/:attemptId/prompts/:promptId/assess` (multipart `audio`, cùng giới hạn định dạng/kích thước cũ): gọi AI ĐỒNG BỘ, lưu/ghi đè kết quả câu đó (thu lại thì ghi đè), bỏ âm thanh. Trả `{promptId, transcript, score, wordIssues:[{word, heardAs, issue, tip}], tips:[]}`. AI lỗi: 503, không lưu, cho thu lại.
- [x] `SpeakingGrader` thêm `assessPrompt(promptText, audio)`: Gemini nhận audio inline (base64) + câu mẫu, so từng từ: từ đọc sai/thiếu/thừa, âm cụ thể (vd /θ/, âm cuối, trọng âm), cách sửa ngắn bằng tiếng Việt. Bản giả trả kết quả cố định (tên tệp chứa `fail` thì lỗi).
- [x] `POST /speaking/attempts/:attemptId/submit` `{durationSeconds}`: yêu cầu đủ mọi câu đã có kết quả (thiếu thì 400 `errors.speakingIncomplete`); tính điểm tổng/thành phần từ kết quả từng câu + một lời gọi AI chỉ văn bản để viết `improvements`; lưu như `GRADED`. Endpoint submit multipart cũ (`/lessons/:id/submit`), `SpeakingGradingService` và `SpeakingGrader.grade` đã xoá ở 11e.
- [x] Job dọn dẹp (đợt 6) xoá lượt `IN_PROGRESS` quá 24 giờ.
- [x] FE (11e): sau mỗi lần dừng thu, gửi assess, hiện thẻ nhận xét ngay dưới câu: điểm câu, câu mẫu với từ lỗi tô đỏ (tooltip `heardAs` + mẹo), danh sách "Cần cải thiện", nút "Thu lại" / "Câu tiếp". Đang chấm hiện loading; lỗi thì nút thử lại. Nút Nộp chỉ bật khi mọi câu có kết quả. Component hiển thị đặt ở `components/practice` (chỉ nhận props).

## Tiêu chí xong
- [x] Không có key: mọi luồng chạy bản giả như cũ, test xanh. Có key: Viết, Chat, Nói gọi Gemini thật.
- [x] `./mvnw -q compile`, test module liên quan, `npm run lint`, `npm run build` sạch.

## Kiểm tra hoàn thành
Folder Postman: "Dot 11 - AI Gemini (Viet, Chat, Noi)". Điền `GEMINI_API_KEY` vào `.env` rồi restart BE để kiểm bản thật; xoá key để kiểm bản giả.
| # | Request / thao tác | Kỳ vọng |
|---|---|---|
| 1 | `POST /writing/prompts/{id}/submit` bài có lỗi ngữ pháp rõ | GRADED sau vài giây, `feedback.issues` chỉ đúng lỗi; SQL `SELECT model_name FROM ai_feedbacks ORDER BY created_at DESC LIMIT 1` chứa `gemini` |
| 2 | `GET /chat/quota` | `{limit:30, used, remaining, resetAt}`, `used` đúng số tin trợ lý hôm nay |
| 3 | Chat hỏi "Khi nào dùng present perfect?" | Trả lời tiếng Việt, có link gợi ý bài có thật trong DB |
| 4 | Chat hỏi "Giá vàng hôm nay?" | `isRefusal` true, lịch sự |
| 5 | SQL chèn đủ 30 tin trợ lý hôm nay, rồi gửi tin | 429 `CHAT_DAILY_LIMIT`; giao diện khoá ô nhập, hiện thông báo hết lượt |
| 6 | Tin chứa `[fail]` (bản giả) | 503 `AI_UNAVAILABLE`, `used` không tăng; giao diện có nút Thử lại |
| 7 | `POST /speaking/lessons/{id}/attempts` | `{attemptId, lessonId, status:"IN_PROGRESS", results:[]}`; gọi lại trả cùng `attemptId` kèm các câu đã chấm |
| 8 | `POST /speaking/attempts/{id}/prompts/{promptId}/assess` (multipart `audio`), cố đọc sai "three" thành "tree" | `{promptId, transcript, score, wordIssues:[{word, heardAs, issue, tip}], tips[]}`; "three" trong `wordIssues` kèm mẹo âm /θ/ |
| 9 | Assess lại cùng câu | Kết quả câu bị ghi đè (DB: vẫn một dòng `speaking_prompt_results` cho cặp attempt+prompt) |
| 10 | Assess với tệp `fail.*` (bản giả) hoặc AI lỗi | 503 `AI_UNAVAILABLE`, không lưu; sai định dạng 415 `AUDIO_UNSUPPORTED`, quá 5MB 413 `AUDIO_TOO_LARGE` |
| 11 | `POST /speaking/attempts/{id}/submit` khi còn câu chưa chấm | 400 `SPEAKING_INCOMPLETE`, `messageKey` `errors.speakingIncomplete` |
| 12 | Đủ câu rồi `submit` `{durationSeconds}` | `status:"GRADED"`, `overallScore` = trung bình điểm các câu, đủ `scores`, có `improvements`; nộp lại lần nữa 409 |
| 13 | SQL: `SELECT prompt_tokens, completion_tokens, model_name FROM chat_messages ORDER BY created_at DESC LIMIT 5` | Có số token với tin trợ lý |
| 14 | SQL: lượt `IN_PROGRESS` có `updated_at` quá 24 giờ, đợi job dọn (phút 15 hằng giờ) | Lượt và các dòng `speaking_prompt_results` của nó bị xoá |
