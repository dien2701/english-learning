package vn.enlearning.backend.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import vn.enlearning.backend.entity.enums.StudySkill;

/**
 * Bản giả: trả lời mẫu theo từ khoá, từ chối câu ngoài phạm vi học tiếng Anh. Tin chứa {@link #FAIL_MARKER}
 * làm bản giả lỗi để thử luồng báo lỗi.
 */
@Component
@Conditional(OpenAiKeyMissing.class)
@RequiredArgsConstructor
class FakeChatAssistant implements ChatAssistant {

	static final String FAIL_MARKER = "[fail]";
	static final String MODEL_NAME = "fake-chat-assistant";

	private static final Pattern LISTENING = words("nghe", "listening");
	private static final Pattern GRAMMAR = words("ngữ pháp", "grammar", "thì", "tense", "present", "past", "future",
			"perfect", "passive", "bị động", "câu điều kiện", "conditional", "preposition", "giới từ");
	private static final Pattern SPEAKING = words("phát âm", "pronunciation", "pronounce", "nói", "speaking");
	private static final Pattern WRITING = words("viết", "writing", "email", "essay");
	private static final Pattern READING = words("đọc", "reading");
	private static final Pattern VOCABULARY = words("từ vựng", "vocabulary", "từ mới", "toeic", "word", "words",
			"phrase", "idiom");
	private static final Pattern IN_SCOPE = words("english", "tiếng anh", "ielts", "sentence", "verb", "noun",
			"adjective", "động từ", "danh từ", "tính từ", "dịch", "translate", "nghĩa", "meaning", "flashcard",
			"bài học", "học", "luyện", "sửa lỗi", "lỗi sai", "bài kiểm tra", "exam", "lesson");

	private final AiProperties properties;

	@Override
	public Reply reply(List<Turn> history, String message) {
		sleep();
		String lower = message.toLowerCase();
		if (lower.contains(FAIL_MARKER)) {
			throw new AiUnavailableException("Bản giả bị ép lỗi bằng " + FAIL_MARKER);
		}
		int promptTokens = tokens(message) + history.stream().mapToInt(t -> tokens(t.content())).sum();
		boolean inScope = IN_SCOPE.matcher(lower).find() || LISTENING.matcher(lower).find()
				|| GRAMMAR.matcher(lower).find() || SPEAKING.matcher(lower).find() || WRITING.matcher(lower).find()
				|| READING.matcher(lower).find() || VOCABULARY.matcher(lower).find();
		if (!inScope) {
			String refusal = "Mình chỉ hỗ trợ các câu hỏi liên quan tới việc học tiếng Anh thôi nhé. Bạn có thể hỏi "
					+ "mình về từ vựng, ngữ pháp, phát âm, hoặc nhờ mình gợi ý bài luyện tập phù hợp.";
			return new Reply(refusal, List.of(), true, MODEL_NAME, promptTokens, tokens(refusal));
		}

		String content;
		List<StudySkill> skills = new ArrayList<>();
		if (LISTENING.matcher(lower).find()) {
			content = "Để cải thiện kỹ năng nghe, hãy nghe cùng một đoạn ba lần: lần đầu nắm ý chính, lần hai bắt chi "
					+ "tiết như số liệu và tên riêng, lần ba vừa nghe vừa đọc transcript để đối chiếu.";
			skills.add(StudySkill.LISTENING);
		} else if (GRAMMAR.matcher(lower).find()) {
			content = "Bạn nên học ngữ pháp theo tình huống thay vì học rời từng công thức. Ví dụ: quá khứ đơn dùng "
					+ "cho việc đã xong và có mốc thời gian rõ, hiện tại hoàn thành dùng khi việc đó còn liên quan "
					+ "tới hiện tại. So sánh: \"I lost my keys yesterday\" và \"I have lost my keys\".";
			skills.add(StudySkill.WRITING);
			skills.add(StudySkill.READING);
		} else if (SPEAKING.matcher(lower).find()) {
			content = "Người Việt thường bỏ âm cuối, nên hãy tập trung vào các âm /s/, /z/, /t/ và /d/ ở cuối từ. "
					+ "Thu âm lại giọng mình rồi nghe đối chiếu với bản gốc là cách nhanh nhất để nhận ra khác biệt.";
			skills.add(StudySkill.SPEAKING);
		} else if (WRITING.matcher(lower).find()) {
			content = "Với bài viết, giữ ba phần rõ ràng: nêu mục đích ngay câu đầu, trình bày chi tiết ở giữa, và "
					+ "kết bằng một yêu cầu hoặc bước tiếp theo cụ thể. Tránh cách nói quá thân mật khi viết cho cấp "
					+ "trên hoặc khách hàng.";
			skills.add(StudySkill.WRITING);
		} else if (READING.matcher(lower).find()) {
			content = "Khi đọc, hãy lướt tiêu đề và câu đầu mỗi đoạn để nắm ý chính trước, rồi mới đọc kỹ để trả lời "
					+ "câu hỏi chi tiết.";
			skills.add(StudySkill.READING);
		} else if (VOCABULARY.matcher(lower).find()) {
			content = "Học từ vựng theo cụm sẽ nhớ lâu hơn học từ đơn lẻ. Thay vì học riêng \"decision\", hãy học "
					+ "luôn \"make a decision\", \"a tough decision\", \"reach a decision\".";
			skills.add(StudySkill.VOCABULARY);
		} else {
			content = "Mình hiểu câu hỏi của bạn. Bạn có thể nói rõ hơn một chút được không: bạn đang gặp khó ở kỹ "
					+ "năng nào, hay muốn mình giải thích một điểm ngữ pháp, một từ cụ thể?";
		}
		return new Reply(content, skills, false, MODEL_NAME, promptTokens, tokens(content));
	}

	private void sleep() {
		long millis = properties.fakeLatency().toMillis();
		if (millis <= 0) {
			return;
		}
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AiUnavailableException("Bị ngắt khi đang trả lời", e);
		}
	}

	/** Ước lượng thô (4 ký tự một token) để bản giả cũng có số token cho đối soát. */
	private static int tokens(String text) {
		return Math.max(1, text.length() / 4);
	}

	/** Khớp nguyên từ, không phân biệt hoa thường, dùng được cho cả cụm tiếng Việt có dấu. */
	private static Pattern words(String... terms) {
		return Pattern.compile("(?<![\\p{L}])(" + String.join("|", terms) + ")(?![\\p{L}])",
				Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}
}
